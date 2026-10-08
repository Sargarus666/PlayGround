# MMO Calendar — исходники Android (Kotlin + Compose)

Календарь выхода MMORPG. Пакет `com.mmocal.app`, minSdk 26, target/compile 34.

## Структура

- `app/src/main/java/com/mmocal/app/` — код: `MainActivity.kt`, `MMOApp.kt`, `data/` (Models, GamesRepository, Stores, BackupHelper), `ui/` (theme, components, screens), `notifications/`
- `app/src/main/res/` — ресурсы (иконки игр в `drawable-nodpi/`, темы, FileProvider)
- `app/src/main/AndroidManifest.xml`
- `apks/` — подписанные APK по версиям (1.3, 1.4, …)
- `logs/` — сессия сборки + снимок remote data.json
- `release.jks` — ключ подписи (пароль в `app/build.gradle.kts`). **Не светить, хранить копию.**

## Сборка

Нужны JDK 17, Android SDK (platform 34, build-tools), Gradle 8.7:

```bash
cd mmo-calendar
echo "sdk.dir=/путь/к/Android/SDK" > local.properties
gradle :app:assembleDebug        # отладка
gradle :app:assembleRelease      # релиз (подпись release.jks), выход: app/build/outputs/apk/release/
```

Важно: релиз подписывается тем же ключом (`release.jks`), поэтому APK встаёт поверх 1.4 как обновление без переустановки. Если ключ потерять — придётся переустанавливать приложение.

## Данные

Игры подтягиваются из `https://mmocal-data.surge.sh/data.json` (см. `GamesRepository.DATA_URL`), кэш — в приложении. Недостающие иконки/скриншоты 6 игр закрыты встроенным `mediaPatch` в `GamesRepository.kt` (ссылки проверены). Для постоянного решения — влить `mmo-data-fix.json` (корень репозитория) в `data.json` и перезалить на surge:

```bash
npm i -g surge
surge ./папка-с-data.json mmocal-data.surge.sh
```
