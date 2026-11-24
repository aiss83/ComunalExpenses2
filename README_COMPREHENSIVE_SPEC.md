# ТЕХНИЧЕСКАЯ СПЕЦИФИКАЦИЯ
## Проект: учёт коммунальных показаний (ComunalExpenses2)

**Обновлено:** 23.11.2026
**Статус:** соответствует текущему коду `main`.

---
### 1. Назначение и границы

**Назначение:** мобильное приложение для ведения журнала показаний счётчиков (холодная/горячая
вода, электричество день/ночь), их отправки (share) и просмотра динамики потребления.

**Платформы:** Android (minSdk 26, targetSdk 35) и iOS (deployment target 15.0) на общем
Kotlin Multiplatform ядре и общем Compose UI.

**Что приложение *не* делает:** не считает стоимость по тарифам, не ведёт расчётные периоды и
начисления, не распределяет расходы между жильцами. Домен — только показания и их производные.

---
### 2. Архитектура

Три слоя, зависимости направлены вниз: `ui → domain → data`.

#### 2.1. UI (commonMain)
Compose Multiplatform, единый код для Android и iOS.

| Экран | Файл | Назначение |
| :--- | :--- | :--- |
| `App` | `ui/screens/MainScreen.kt` | тема, навигация, глобальный диалог ошибок |
| `HomeScreen` | `ui/screens/HomeScreen.kt` | список показаний, фильтр по датам, свайп-удаление, share |
| `AddResourcesDataScreen` | `ui/screens/AddResourcesDataScreen.kt` | добавление/правка показаний |
| `SettingsScreen` | `ui/screens/SettingsScreen.kt` | адрес, шаблон сообщения, тема, экспорт/импорт, удаление всех |
| `StatsScreen` | `ui/screens/StatsScreen.kt` | график и сводка по метрикам |

**Навигация:** sealed-класс `AppRoute` (`Home`, `Settings`, `Stats`, `EditResources(record?)`).
Состояние маршрута хранится в `rememberSaveable` через `AppRoute.RouteSaver` (строковое
кодирование), поэтому переживает поворот экрана и восстановление процесса. Системная кнопка
«назад» обрабатывается `PlatformBackHandler` (Android — `BackHandler`, iOS — no-op) на каждом
вложенном экране; экран ввода переиспользует диалог «отменить ввод».

**Состояние экранов:** вводимые значения и фильтры — `rememberSaveable`; выбранные метрики
статистики хранятся битмаской (`Int`), так как `Set` не сохраняется в Bundle.

#### 2.2. Domain
- `ResourcesDataViewModel` — источник истины для списка записей (`StateFlow<List<ResourceData>>`),
  CRUD, `importJson`/`exportJson`, `ImportResult`, синхронизация виджета при каждом изменении.
- `SettingsViewModel` — `SaveSettingsResult` (Success/Error), валидация адреса, смена темы.
- `StatsCalculations` — чистые функции: `metricSeries` (кумулятивный ряд или дельты между
  соседними записями), `summarize` (min/max/avg/total/last), `downsampleLabels` (прореживание
  подписей оси).

#### 2.3. Data
- `ResourceDataDao` — SQLDelight-запросы, `Dispatchers.IO`, массовый импорт в одной транзакции.
- `ResourceDataRepository` — тонкая обёртка над DAO для ViewModel.
- `SettingsManager` / `SettingsManagerImpl` — multiplatform-settings, состояние отдаётся
  синхронным `StateFlow` (UI не стартует с дефолтов).
- `ExportImport` — JSON-модель и `parseImportJson` (`Success(records, skippedInvalid)` /
  `InvalidJson` / `NoRecords`).
- `utils/*` — `expect/actual`: `createDriver`, `createSettings`, `shareText`,
  `exportAndShareJson`, `rememberJsonFilePicker`, `updateWidgetData`/`clearWidgetData`,
  `PlatformBackHandler`, `getAppVersion`.

**DI:** Koin 4.1.0, модули `dataModule` (singletons) и `viewModelModule` (factory). На Android
используется `koinViewModel()` (ViewModel живёт в `ViewModelStore` Activity), на iOS — scope
композиции `ComposeUIViewController`.

---
### 3. Хранение данных

SQLDelight, файл `resources_data_database.db`.

#### 3.1. Таблица `resources_records`

| Поле | Тип | Ограничения | Назначение |
| :--- | :--- | :--- | :--- |
| `recordId` | TEXT | PRIMARY KEY | UUID записи |
| `date` | INTEGER | NOT NULL, UNIQUE | ключ дня: UTC-полночь календарной даты |
| `waterHot` | INTEGER | NOT NULL DEFAULT 0 | горячая вода |
| `waterCold` | INTEGER | NOT NULL DEFAULT 0 | холодная вода |
| `electricityDay` | INTEGER | NOT NULL DEFAULT 0 | электричество, день |
| `electricityNight` | INTEGER | NOT NULL DEFAULT 0 | электричество, ночь |
| `shared` | INTEGER | NOT NULL DEFAULT 0 | 0/1 — отправлено и заблокировано от правки |

Единицы измерения приложением не задаются (целые числа счётчика).

#### 3.2. Операции
`getAllResourceData` (ORDER BY date DESC), `findResourceDataById`, `findResourceDataByDate`,
`insertResourceData`, `insertOrReplaceResourceData` (идемпотентный импорт), `updateResourceData`,
`deleteResourceData`, `deleteAllResourceData`, `markAsShared`.

#### 3.3. Миграции
`migrations/1.sqm` — создание таблицы; `2.sqm` — добавление `shared`; `3.sqm` — переход к
модели «одна карточка на день»:
1. исторические дубликаты одного локального дня схлопываются, остаётся самая поздняя запись
   (по исходной метке времени, при равенстве — по `recordId`);
2. сохранённые метки времени переводятся в ключи дня (UTC-полночь локальной даты);
3. создаётся `UNIQUE INDEX resources_records_date_unique ON resources_records(date)`.

Новая схема = новый `.sqm` + правка `.sq`; SQLDelight проверяет соответствие при сборке,
миграция покрыта тестом `Migration3Test`.

#### 3.4. Ограничения целостности
- **Одна карточка на календарный день**: `date` — ключ дня, на нём UNIQUE-индекс. Добавление
  второй записи за день блокируется БД; импорт использует `INSERT OR REPLACE`, поэтому
  записи одного дня схлопываются в последнюю.
- Ключ дня не зависит от часового пояса: смена зоны не сдвигает дату записи.
- При правке записи значения проверяются с двух сторон: не меньше предыдущей и не больше
  следующей записи (иначе дельта потребления становится недостоверной).
- Отправленная запись (`shared`) блокируется от правки и удаления.

---
### 4. Сценарии

#### 4.1. Ввод показаний
Новая запись получает ключ текущего дня. Если карточка за сегодня уже существует, кнопка «+»
(и deep link из виджета) открывает её на редактирование — вторая карточка за день не создаётся.
Правка сохраняет исходную дату и `id`; запись `shared` нельзя редактировать и удалять (попытка
сохранения показывает диалог о блокировке). Пустые поля трактуются как 0. После сохранения
обновляются данные виджета (Android) из самой свежей записи.

#### 4.2. Фильтр по датам (Home)
`DatePicker` возвращает UTC-полночь выбранного дня — это и есть ключ дня, поэтому значение
используется напрямую. Верхняя граница — эксклюзивная (`dayEndExclusive` = ключ + сутки),
сравнение идёт по ключам дня. Часовой пояс и переходы DST на фильтр не влияют.

#### 4.3. Отправка (share)
Пользователь подтверждает отправку; текст собирается из шаблона настроек с подстановкой
`$day_e`, `$night_e`, `$cold_w`, `$hot_w`, `$flat`. После отправки запись помечается `shared`
и блокируется. На iOS шит презентуется с popover-якорем (обязательно для iPad).

#### 4.4. Экспорт/импорт
Экспорт — JSON в Downloads (Android, через MediaStore) с последующим share; на iOS — в Documents.
Импорт — выбор файла системным пикером, разбор через `parseImportJson`, запись в одной
транзакции через `INSERT OR REPLACE`. Даты из старых бэкапов (реальные метки времени)
нормализуются к ключу дня, записи одного дня схлопываются в последнюю. Результат импорта всегда
один: `Success(imported, skipped)`, `InvalidFile`, `NoValidRecords` или `Failed`;
прогресс-индикатор снимается при любом исходе.

#### 4.5. Удаление
Свайп влево или кнопка → диалог подтверждения → удаление и snackbar с «Отменить» (повторная
вставка записи). «Удалить все» — отдельный диалог в настройках. Обе операции обновляют виджет.

#### 4.6. Статистика
Данные сортируются по дате; для каждой метрики ряд строится один раз
(`remember(sortedData, deltaMode, cutoff)`). В режиме «Потребление» значение = разница с
предыдущей записью (первая запись и «сброс» счётчика дают 0). Периоды 3/6/12 месяцев —
отсечка по 30 дням. Ось X — до 6 подписей дат; сводка: min/max/avg и «Всего» в режиме
потребления либо «Последнее» в кумулятивном.

#### 4.7. Виджет (Android)
`QuickInputWidgetProvider` читает `widget_data` из SharedPreferences. Запись данных и рассылка
package-scoped broadcast `WIDGET_REFRESH` выполняются в `utils/WidgetData.android.kt`; провайдер
по этому action перерисовывает все экземпляры виджета. Клик открывает экран добавления.

---
### 5. Локализация

Общие строки — Compose Resources (`values`, `values-ru`), наборы ключей должны совпадать.
Строки Android-виджета и имени приложения — `androidApp/src/main/res/values{,-ru}`.
Ошибки доменного слоя, показываемые пользователю, вынесены в ресурсы; сырые тексты исключений
не показываются.

---
### 6. Сборка, подпись, версия

- JDK 17+ (проверено на JBR из Android Studio), Android SDK 36, Gradle Wrapper 9.6.0.
- `versionCode` = число коммитов `HEAD`; `versionName` = `<база>.<коммитов>`, база из ближайшего
  тега (`1.0` → `1.0.42`), иначе `1.0`.
- Подпись release: `keystore.properties` (git-ignored). Без файла release-APK неподписанный —
  использование debug-ключа запрещено намеренно.
- Release: R8 + shrinkResources, имена APK `ComunalExpenses-v<versionName>.apk`.
- iOS: `iosApp.xcodeproj`, framework собирается задачей
  `:shared:embedAndSignAppleFrameworkForXcode`.

---
### 7. Тестирование

`shared/src/commonTest` (JVM, `testDebugUnitTest`):
- `ExportImportTest` — round-trip, невалидные id, «не JSON» vs «нет записей»;
- `ResourceDataDaoTest` — реальная SQLite в памяти: идемпотентный повторный импорт, замена
  записи по id, порядок выдачи по дате, отказ второй записи за тот же день;
- `Migration3Test` — миграция `3.sqm` на реальной БД: схлопывание дублей дня, нормализация дат,
  работа UNIQUE-индекса;
- `SettingsDataTest` — подстановка шаблона, форматирование адреса;
- `SettingsViewModelTest` — валидация настроек;
- `DateUtilsTest` — ключи дня, отрицательные смещения, DST, границы диапазона;
- `AppRouteTest` — сохранение/восстановление маршрута;
- `StatsCalculationsTest` — ряды, дельты, сводка, прореживание подписей.

CI (GitHub Actions): тесты, debug APK, lint, компиляция iOS-таргета.

**КОНЕЦ ДОКУМЕНТА.**
