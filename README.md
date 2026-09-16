# 到场（OnSite）

把一句话钉在某个地点。人到了才看得见，时间到了就消失。

Android v0 · Kotlin + Jetpack Compose  
液态玻璃底栏：`io.github.kyant0:backdrop-android`

## 打开工程

1. Android Studio 打开本仓库
2. 本机创建 `local.properties`，写上 SDK 路径，例如：
   `sdk.dir=/Users/你的用户名/Library/Android/sdk`
3. Sync Gradle，真机或带 Google Play 的模拟器运行

minSdk 26。玻璃折射建议 API 33+ 真机看。

## v0 做什么

- 只给自己留地点信
- 列表不展示正文
- Play Services 地理围栏 + 本地通知
- 拆信前再校验距离
- 过期后不可读，围栏注销
