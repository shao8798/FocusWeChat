# GitHub Actions 在线编译指南

## 🚀 快速开始

### 步骤1：创建GitHub仓库
1. 访问 https://github.com/new
2. 仓库名称：`FocusWeChat`
3. 选择 **Public**（公开）
4. 点击 **Create repository**

### 步骤2：上传代码
```bash
# 在项目目录下执行
cd FocusWeChat

git init
git add .
git commit -m "Initial commit"

git branch -M main
git remote add origin https://github.com/你的用户名/FocusWeChat.git
git push -u origin main
```

### 步骤3：触发编译
1. 进入GitHub仓库页面
2. 点击 **Actions** 标签
3. 点击 **Build APK** 工作流
4. 点击 **Run workflow** → **Run workflow**

### 步骤4：下载APK
1. 等待编译完成（约3-5分钟）
2. 点击 **Actions** → 最新运行记录
3. 在 **Artifacts** 部分下载 `FocusWeChat-APK`
4. 解压下载的文件，得到 `app-debug.apk`

---

## 📱 安装到手机

### 方法1：直接安装
1. 将APK传输到手机
2. 点击安装，允许"未知来源"
3. 开启"无障碍服务"权限

### 方法2：ADB安装
```bash
adb install app-debug.apk
```

---

## ⚙️ 自动发布Release

手动触发编译时会自动创建Release：
1. 进入 **Actions** 标签
2. 点击 **Build APK**
3. 点击 **Run workflow**
4. 编译完成后，在 **Releases** 页面下载APK

---

## 🔧 配置文件说明

`.github/workflows/build.yml` 包含：
- JDK 17 环境配置
- Android SDK 自动安装
- Gradle 编译
- APK 自动上传
- Release 自动创建

---

## ❓ 常见问题

### Q: 编译失败怎么办？
A: 检查GitHub Actions日志，通常是依赖下载问题，重试即可。

### Q: APK在哪里下载？
A: 在Actions运行记录的Artifacts部分，或Releases页面。

### Q: 如何更新代码重新编译？
A: 修改代码后push到GitHub，Actions会自动触发编译。

---

## ✅ 优势

- 无需本地配置Android开发环境
- 免费使用GitHub的服务器资源
- 自动编译，随时获取最新APK
- 版本历史可追溯

---

**开始操作吧！有任何问题随时问我。**
