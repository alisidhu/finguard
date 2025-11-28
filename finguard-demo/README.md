# FinGuard Demo Module

Purpose: reference implementation showing FinGuard integration paths.

Install (internal samples only):
```kotlin
implementation(project(":finguard-core"))
implementation(project(":finguard-demo"))
```

Notes:
- Depends on all feature modules.
- Intended for sample apps; not meant for production distribution.
- Add UI + flows here without pulling UI dependencies into the core modules.
