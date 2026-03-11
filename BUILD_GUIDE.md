# FocusWeChat APP 编译指南

## 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

---

## 方法一：使用 Android Studio（推荐）

### 1. 打开项目
```bash
# 在项目目录下
start FocusWeChat
```

### 2. 编译步骤
1. 打开 Android Studio
2. File → Open → 选择 `FocusWeChat` 文件夹
3. 等待 Gradle Sync 完成
4. Build → Make Project (Ctrl+F9)
5. Build → Build Bundle(s) / APK(s) → Build APK(s)

### 3. 获取APK
编译完成后：
- 路径：`app/build/outputs/apk/debug/app-debug.apk`
- 点击右下角提示的 "locate" 即可找到文件

---

## 方法二：命令行编译

### 1. 进入项目目录
```bash
cd FocusWeChat
```

### 2. 编译Debug版本
```bash
# Windows
gradlew.bat assembleDebug

# Mac/Linux
./gradlew assembleDebug
```

### 3. 编译Release版本
```bash
# Windows
gradlew.bat assembleRelease

# Mac/Linux
./gradlew assembleRelease
```

### 4. APK输出位置
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 方法三：快速编译脚本

### Windows (PowerShell)
```powershell
# 保存为 build.ps1
$projectPath = "$PSScriptRoot\FocusWeChat"
Set-Location $projectPath

# 清理旧构建
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue

# 编译
.\gradlew.bat assembleDebug

# 复制到桌面
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
$desktopPath = [Environment]::GetFolderPath("Desktop")
Copy-Item $apkPath "$desktopPath\FocusWeChat-v1.0.0.apk" -Force

Write-Host "✅ 编译完成！APK已复制到桌面"
```

运行：
```powershell
.\build.ps1
```

---

## 安装到手机

### 方法一：ADB安装
```bash
# 连接手机，开启USB调试
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 方法二：手动安装
1. 将APK复制到手机
2. 文件管理器点击安装
3. 允许"未知来源"安装

---

## 常见问题

### 1. Gradle Sync失败
```bash
# 清除Gradle缓存
gradlew clean
gradlew build --refresh-dependencies
```

### 2. 编译内存不足
在 `gradle.properties` 添加：
```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
```

### 3. JDK版本不匹配
确保使用JDK 17：
```bash
# 检查JDK版本
java -version

# 在Android Studio中设置
File → Settings → Build → Build Tools → Gradle → Gradle JDK
```

---

## 文件大小优化

当前预估大小：
- Debug APK: ~8-10 MB
- Release APK: ~5-7 MB

优化后可以达到 <10MB 目标 ✅

---

## 下一步

1. 编译APK
2. 安装到手机
3. 开启无障碍服务权限
4. 测试视频号监测功能

需要我提供测试指南吗？
