# Коммунальные расходы (ComunalExpenses2)

Мобильное приложение для учёта показаний счётчиков воды и электричества в квартире.
Kotlin Multiplatform + Compose Multiplatform: один общий UI и слой данных для Android и iOS.

## Возможности

- Ввод показаний: холодная/горячая вода, электричество день/ночь.
- Список записей с фильтром по диапазону дат, свайп-удаление с отменой, «поделиться» показаниями.
- Статистика: линейный график по четырём метрикам, режим «потребление» (дельты между показаниями), периоды 3/6/12 месяцев, мин/макс/среднее/итого.
- Настройки: адрес, шаблон текста для отправки (подстановка `$day_e $night_e $cold_w $hot_w $flat`), светлая/тёмная/системная тема.
- Экспорт и импорт резервной копии в JSON, «удалить все записи».
- Виджет Android с последними показаниями и быстрым переходом на ввод.
- Локализация: русский и английский.

## Стек

| Слой | Технологии |
| :--- | :--- |
| Язык / платформа | Kotlin 2.3.21, Kotlin Multiplatform (Android + iOS) |
| UI | Compose Multiplatform 1.10.0, Material 3, koalaplot (графики) |
| Архитектура | ViewModel + StateFlow, Koin 4.1.0 (KMP) |
| Данные | SQLDelight 2.1.0 (Android/iOS native driver), multiplatform-settings |
| Сериализация | kotlinx-serialization-json 1.8.0, kotlinx-datetime 0.6.2 |
| Сборка | Gradle 9.6.0, AGP 9.3.1, JDK 17 target |

## Структура репозитория

```
androidApp/            Android-оболочка: MainActivity, Application, виджет, ресурсы
shared/                Общий код (commonMain / androidMain / iosMain / commonTest)
  src/commonMain/kotlin/ru/aiss83/comunalexpenses2/
    data/              доменная модель, SQLDelight DAO, репозиторий, настройки, экспорт/импорт
    domain/            ViewModel'и и чистые расчёты статистики
    ui/                тема, маршруты, экраны (Home, Add, Settings, Stats), MainScreen (App)
    utils/             expect/actual: файлы, шаринг, виджет, back-навигация, даты
  src/commonMain/sqldelight/   схема .sq и миграции .sqm
  src/commonMain/composeResources/values{,-ru}/strings.xml
iosApp/                Xcode-проект, подключает собранный framework Shared
tools-скрипты          gen_test_data.py — генератор тестового JSON
```

## Архитектура

`ui → domain → data`:

- **UI** — Compose-экраны. Навигация описана sealed-классом `AppRoute` и сохраняется через
  `rememberSaveable` (переживает поворот и убийство процесса).
- **Domain** — `ResourcesDataViewModel` (поток записей, CRUD, импорт/экспорт, синхронизация
  виджета) и `SettingsViewModel` (валидация адреса, тема). Чистая математика графиков вынесена
  в `StatsCalculations` и покрыта тестами.
- **Data** — `ResourceDataDao` (SQLDelight, операции на `Dispatchers.IO`, импорт в транзакции),
  `ResourceDataRepository` (тонкая обёртка), `SettingsManager`, `ExportImport`.

Платформенные различия спрятаны за `expect/actual`: драйвер БД, настройки, шаринг, файловый
picker, экспорт, данные виджета, обработка системного «назад», версия приложения.

DI — Koin (`dataModule`, `viewModelModule`). На Android ViewModel'и берутся через
`koinViewModel()` и живут в `ViewModelStore` Activity; на iOS — в scope композиции.

## Данные и миграции

Одна таблица `resources_records`: `recordId TEXT PRIMARY KEY`, `date INTEGER` — **ключ дня**
(UTC-полночь календарной даты, не момент времени), `waterHot`, `waterCold`, `electricityDay`,
`electricityNight`, `shared INTEGER` (0/1 — запись отправлена и заблокирована от правки).
На `date` стоит UNIQUE-индекс.

**Одна карточка на календарный день.** `date` — ключ дня (UTC-полночь локальной календарной
даты), поэтому ключ не «съезжает» при смене часового пояса; на `date` наложен UNIQUE-индекс.
Кнопка «+» при наличии карточки за сегодня открывает её на редактирование, а не создаёт вторую.
График использует порядковый номер точки как категорию оси X.

Схема версионируется файлами `shared/src/commonMain/sqldelight/.../migrations/*.sqm`:
`1.sqm` — создание таблицы, `2.sqm` — колонка `shared`, `3.sqm` — переход к ключам дня
(исторические дубликаты одного дня схлопываются в самую позднюю запись, затем создаётся
UNIQUE-индекс). Изменение схемы = новая миграция + правка `.sq`.

## Экспорт и импорт

`ExportContainer { appName, version, records: [ResourceDataJson] }` — pretty-printed JSON.
Импорт: `INSERT OR REPLACE`, поэтому повторный импорт того же файла идемпотентен; записи с
невалидным UUID пропускаются, их количество показывается пользователю. Даты из старых бэкапов
(реальные метки времени) нормализуются к ключу дня, а несколько записей одного дня схлопываются
в последнюю — инвариант «одна карточка на день» сохраняется.

## Сборка и запуск

Требуется JDK 17+ (проверено на JDK из Android Studio) и Android SDK 36.

```bash
# Android debug
./gradlew :androidApp:assembleDebug
# APK: androidApp/build/outputs/apk/debug/ComunalExpenses-v<версия>.apk

# Unit-тесты общего кода
./gradlew :shared:testDebugUnitTest

# Release-сборка (подпись — см. ниже)
./gradlew :androidApp:assembleRelease
```

iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить схему `iosApp` — Gradle-задача
`embedAndSignAppleFrameworkForXcode` собирает `Shared.framework` автоматически.
Проверка компиляции без Xcode: `./gradlew :shared:compileKotlinIosSimulatorArm64`.

## Подпись и версия

- Ключ читается из `keystore.properties` в корне (файл в `.gitignore`): `storeFile`,
  `storePassword`, `keyAlias`, `keyPassword`. Если файла нет, release-APK собирается
  **неподписанным** — fallback на debug-ключ намеренно убран.
- `versionCode` = число коммитов в `HEAD`, `versionName` = `<база>.<число коммитов>`, где база
  берётся из ближайшего git-тега (`1.0` → `1.0.42`), иначе `1.0`.
- Release включает R8 и shrinking ресурсов.

## Тесты и проверки

```bash
./gradlew :shared:testDebugUnitTest   # 66 тестов: импорт/экспорт, DAO и миграции на SQLite, настройки, ключи дня, маршруты, статистика
./gradlew :androidApp:lintDebug       # Android Lint
```

CI (GitHub Actions) выполняет тесты, сборку debug APK и lint на каждый push/PR, а также
компиляцию iOS-таргета на macOS-раннере. Локально включены parallel, build cache и
configuration cache (`gradle.properties`).

## Локализация

Строки лежат в `shared/src/commonMain/composeResources/values/strings.xml` (en) и
`values-ru/strings.xml`; наборы ключей обязаны совпадать. Строки Android-виджета — в
`androidApp/src/main/res/values{,-ru}/strings.xml`.

## Известные ограничения

- iOS-виджет не реализован (`updateWidgetData` — no-op).
- Расчётной части (тарифы, стоимость, периоды начисления) нет: приложение хранит показания.
- Нет шифрования БД; системный бэкап покрывает только SharedPreferences (данные — через экспорт).
- Нет CI-гейта на detekt/ktlint.

## Документация

- [README_COMPREHENSIVE_SPEC.md](README_COMPREHENSIVE_SPEC.md) — описание устройства приложения.
- [README_CLASS_DIAGRAM.puml](README_CLASS_DIAGRAM.puml), [README_ERD_Diagram.puml](README_ERD_Diagram.puml) — диаграммы PlantUML.
