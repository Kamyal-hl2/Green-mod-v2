# GreenAngel Engine (green-engine-v1)

Кастомная Android-сборка Source Engine (на основе nillerusr/source-engine)
для запуска Garry's Mod на телефоне через Termux + GitHub Actions CI.

## Структура

- `engine/` — форк nillerusr/source-engine с патчем для сборки под Android-хост (WAF)
- `garrysmod/` — открытый код Garry's Mod (Facepunch), Lua/gamemode-логика
- `html_chromium/` — JNI-мост Source Engine <-> Android WebView (свой модуль,
  замена тяжёлому Chromium/CEF для встроенного браузера/меню)
- `launcher/` — Android-лаунчер (Java/Kotlin), пока не реализован
- `tools/` — вспомогательные скрипты (копирование VPK и т.п.)
- `.github/workflows/build.yml` — CI-сборка движка под Android (ARM)

## ВАЖНО: про игровой контент

Этот репозиторий НЕ содержит VPK-файлов GMod (модели, текстуры, звуки, карты) —
это платный контент игры, его нужно получить из собственной легальной копии
Garry's Mod. Используйте скрипт в `tools/` (когда будет готов) для копирования
VPK с вашего ПК-инсталла на Android-устройство.

## Статус

🚧 В разработке. Движок пока не запускается на устройстве — соберится только
через CI (см. `.github/workflows/build.yml`), последующая интеграция с
лаунчером ещё не выполнена.

## Лицензия движка

Source Engine код основан на утечке 2017/2020 года, использование только
некоммерческое. Garry's Mod код — открытый, Facepunch Studios.
