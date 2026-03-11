# 快速编译脚本 - Windows

Write-Host "🚀 开始编译 FocusWeChat APP..." -ForegroundColor Green

$projectPath = $PSScriptRoot
Set-Location $projectPath

# 检查gradlew
if (!(Test-Path "gradlew.bat")) {
    Write-Host "⚠️ 未找到gradlew，尝试生成..." -ForegroundColor Yellow
    
    # 创建gradle wrapper
    $gradleWrapperJar = "gradle/wrapper/gradle-wrapper.jar"
    if (!(Test-Path $gradleWrapperJar)) {
        New-Item -ItemType Directory -Path "gradle/wrapper" -Force | Out-Null
        
        # 下载gradle wrapper
        try {
            Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -OutFile $gradleWrapperJar -UseBasicParsing
            Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.properties" -OutFile "gradle/wrapper/gradle-wrapper.properties" -UseBasicParsing
        } catch {
            Write-Host "❌ 下载Gradle Wrapper失败，请手动安装Gradle" -ForegroundColor Red
            exit 1
        }
    }
    
    # 创建gradlew.bat
    @'
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
'@ | Out-File -FilePath "gradlew.bat" -Encoding ASCII
}

# 清理旧构建
Write-Host "🧹 清理旧构建..." -ForegroundColor Yellow
if (Test-Path "app\build") {
    Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue
}

# 编译Debug版本
Write-Host "🔨 开始编译Debug版本..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug --console=plain

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 编译失败！请检查错误信息" -ForegroundColor Red
    exit 1
}

# 检查APK是否生成
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (!(Test-Path $apkPath)) {
    Write-Host "❌ APK文件未找到" -ForegroundColor Red
    exit 1
}

# 获取APK大小
$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "✅ 编译成功！" -ForegroundColor Green
Write-Host "📦 APK大小: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
Write-Host "📍 文件位置: $apkPath" -ForegroundColor White

# 复制到桌面
$desktopPath = [Environment]::GetFolderPath("Desktop")
$destPath = "$desktopPath\FocusWeChat-v1.0.0.apk"
Copy-Item $apkPath $destPath -Force
Write-Host "📋 已复制到桌面: $destPath" -ForegroundColor Green

Write-Host ""
Write-Host "🎉 编译完成！请按以下步骤安装：" -ForegroundColor Green
Write-Host "1. 将APK传输到手机" -ForegroundColor White
Write-Host "2. 允许'未知来源'安装" -ForegroundColor White
Write-Host "3. 安装后开启无障碍服务权限" -ForegroundColor White
Write-Host "4. 开始使用视频号助手！" -ForegroundColor White