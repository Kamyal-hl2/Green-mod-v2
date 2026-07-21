# G'reen Mod

Android порт Source Engine (на основе nillerusr/source-engine) для запуска Garry's Mod.

## Сборка

Workflow автоматически собирает APK при push в main/master:

1. **Движок armv7a** — сборка всех .so модулей (клиент, сервер, движок, меню, физика и т.д.)
2. **Chromium** — скомпилированный WebView для HTML-панелей
3. **APK** — упаковка движка + контента GMod + лаунчер в APK

## Архитектура

- `engine/` — исходный код Source Engine
- `garrysmod/` — контент Garry's Mod (Lua, gamemodes, аддоны)
- `launcher/` — Android лаунчер (Java Activity + OpenGL ES)
- `.github/workflows/build.yml` — CI/CD pipeline

