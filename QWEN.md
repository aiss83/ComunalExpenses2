# ComunalExpenses2 — QWEN.md

## Обзор проекта

**ComunalExpenses2** — Android-приложение для учёта показаний коммунальных счётчиков, написанное на Kotlin с использованием Jetpack Compose.

### Основные возможности
- Добавление, редактирование и удаление записей показаний
- Просмотр истории показаний в виде списка
- Сохранение настроек адреса (улица, дом, квартира)
- Поддержка тёмной и светлой темы, а также Dynamic Color (Android 12+)

### Типы учитываемых ресурсов
- Холодная вода (Cold Water)
- Горячая вода (Hot Water)
- Электроэнергия дневная (Day Electricity)
- Электроэнергия ночная (Night Electricity)

---

## Архитектура

**Паттерн:** MVVM (Model-View-ViewModel)

| Слой | Технологии |
|------|------------|
| UI | Jetpack Compose, Material 3, Navigation Compose |
| ViewModel | AndroidX ViewModel, LiveData, Flow |
| Данные (БД) | Room (SQLite) |
| Настройки | DataStore Preferences |
| DI | Ручные ViewModelFactory |

### Структура пакетов

```
ru.aiss83.comunalexpenses2/
├── MainActivity.kt                  — Точка входа
├── NavRoutes.kt                     — Маршруты навигации (sealed class)
├── ResourcesDataViewModel.kt        — ViewModel для данных
├── ResourcesDataViewModelFactory.kt — Фабрика ViewModel
├── SettingsViewModel.kt             — ViewModel для настроек
├── SettingsViewModelFactory.kt      — Фабрика ViewModel
├── DeleteConfirmationDialog.kt      — Диалог удаления
│
├── screens/
│   ├── MainScreen.kt                — NavHost
│   ├── HomeScreen.kt                — Главный экран (LazyColumn)
│   ├── AddResourcesDataScreen.kt    — Экран добавления показаний
│   └── SettingsScreen.kt            — Экран настроек
│
├── data/
│   ├── ResourceData.kt              — Room Entity
│   ├── ResourceDataDao.kt           — Room DAO
│   ├── ResourcesDataRoomDatabase.kt — Room Database
│   ├── ResourcesDataRepository.kt   — Репозиторий
│   ├── ResourcesDataTypesConverter.kt — TypeConverters
│   ├── SettingsData.kt              — Модель настроек
│   ├── DataStoreManager.kt          — Интерфейс DataStore
│   ├── DataStoreManagerFactory.kt   — Фабрика DataStore
│   └── DataStoreManagerImpl.kt      — Реализация DataStore
│
└── ui/theme/
    ├── Color.kt                     — Палитра
    ├── Theme.kt                     — Тема приложения
    └── Type.kt                      — Типографика
```

---

## Технологии и зависимости

| Компонент | Версия |
|-----------|--------|
| Kotlin | 2.2.10 |
| Compile SDK | 33 |
| Min SDK | 31 |
| Target SDK | 33 |
| Compose BOM | 2023.05.00 |
| Room | 2.5.2 |
| Navigation Compose | 2.6.0 |
| Lifecycle | 2.6.1 |
| DataStore Preferences | 1.0.0 |
| KSP | 2.3.2 |
| Java Compatibility | 19 |

---

## Сборка и запуск

### Требования
- Android Studio (с поддержкой Kotlin и Compose)
- JDK 19+
- Android SDK 33

### Команды Gradle

```bash
# Сборка debug-версии
./gradlew assembleDebug

# Запуск тестов
./gradlew test

# Запуск Android-тестов
./gradlew connectedAndroidTest

# Очистка сборки
./gradlew clean

# Линтинг
./gradlew lint
```

### Запуск из Android Studio
1. Откройте проект в Android Studio
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Выберите устройство/эмулятор (min Android 12 / API 31)
4. Нажмите Run (Shift+F10)

---

## conventions разработки

### Стиль кода
- **Язык:** Kotlin
- **Code style:** official (`kotlin.code.style=official`)
- **AndroidX:** включён (`android.useAndroidX=true`)
- **Non-transitive R class:** включён для уменьшения размера

### Архитектурные принципы
- Репозиторий инкапсулирует доступ к DAO
- ViewModel предоставляет данные через `LiveData` и `Flow`
- Корутинные операции с БД выполняются на `Dispatchers.IO`
- Ручной DI через ViewModelFactory

### База данных (Room)
- Singleton-паттерн с `RoomDatabase.Callback` для инициализации
- KSP для генерации кода Room (`room.schemaLocation`)
- TypeConverter для `Date` и `UUID`

### Настройки (DataStore)
- Preferences DataStore для хранения адреса
- Реактивный `Flow<SettingsData>` для UI

---

## Конфигурационные файлы

| Файл | Назначение |
|------|------------|
| `build.gradle` (root) | Плагины Android, Kotlin, KSP, Compose |
| `app/build.gradle` | Зависимости и конфигурация модуля app |
| `settings.gradle` | Репозитории зависимостей |
| `gradle.properties` | Настройки Gradle (AndroidX, JVM args) |
| `proguard-rules.pro` | Правила ProGuard для release |

---

## Git

- Управляемый репозиторий Git
- Текущая ветка: `main` (предположительно)
- Запланирована ветка `multiplatform` для миграции на KMP
