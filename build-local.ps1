[CmdletBinding()]
param([switch]$Bootstrap, [switch]$AcceptAndroidLicenses, [string]$JdkPath, [string]$SdkPath)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$toolsRoot = Join-Path $projectRoot '.local-tools'
New-Item -ItemType Directory -Force -Path $toolsRoot | Out-Null
function Fetch([string]$Url, [string]$Destination) {
    if (Test-Path -LiteralPath $Destination) { return }
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile ($Destination + '.partial')
    # Both source and destination are fixed files inside this project's tools folder.
    Move-Item -LiteralPath ($Destination + '.partial') -Destination $Destination -Force
}
if (!$JdkPath) { $JdkPath = $env:JAVA_HOME }
if (!$JdkPath -or !(Test-Path -LiteralPath (Join-Path $JdkPath 'bin/javac.exe'))) {
    $jdkFolder = Get-ChildItem -LiteralPath $toolsRoot -Directory -Filter 'jdk-17*' | Select-Object -First 1
    if (!$jdkFolder) {
        if (!$Bootstrap) { throw 'Missing JDK 17. Set JAVA_HOME or run build-local.ps1 -Bootstrap.' }
        $jdkZip = Join-Path $toolsRoot 'jdk17.zip'
        Fetch 'https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip' $jdkZip
        Expand-Archive -LiteralPath $jdkZip -DestinationPath $toolsRoot -Force
        $jdkFolder = Get-ChildItem -LiteralPath $toolsRoot -Directory -Filter 'jdk-17*' | Select-Object -First 1
    }
    if (!$jdkFolder) { throw 'JDK extraction failed.' }
    $JdkPath = $jdkFolder.FullName
}
$env:JAVA_HOME = $JdkPath
$env:Path = (Join-Path $JdkPath 'bin') + ';' + $env:Path
$env:GRADLE_USER_HOME = Join-Path $toolsRoot 'gradle-cache'
if (!$SdkPath) { $SdkPath = $env:ANDROID_HOME }
if (!$SdkPath) { $SdkPath = Join-Path $toolsRoot 'android-sdk' }
$env:ANDROID_HOME = $SdkPath
$env:ANDROID_SDK_ROOT = $SdkPath
$sdkManager = Join-Path $SdkPath 'cmdline-tools/12.0/bin/sdkmanager.bat'
if (!(Test-Path -LiteralPath (Join-Path $SdkPath 'platforms/android-35/android.jar'))) {
    if (!$Bootstrap) { throw 'Missing Android SDK 35. Specify -SdkPath or use -Bootstrap.' }
    if (!(Test-Path -LiteralPath $sdkManager)) {
        $androidZip = Join-Path $toolsRoot 'android-command-line.zip'
        Fetch 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' $androidZip
        $extractRoot = Join-Path $toolsRoot 'android-extract'
        Expand-Archive -LiteralPath $androidZip -DestinationPath $extractRoot -Force
        $cliVersion = Join-Path $SdkPath 'cmdline-tools/12.0'
        New-Item -ItemType Directory -Force -Path $cliVersion | Out-Null
        Copy-Item -Path (Join-Path $extractRoot 'cmdline-tools/*') -Destination $cliVersion -Recurse -Force
    }
    if ($AcceptAndroidLicenses) { 1..100 | ForEach-Object { 'y' } | & $sdkManager "--sdk_root=$SdkPath" --licenses }
    else { & $sdkManager "--sdk_root=$SdkPath" --licenses }
    if ($LASTEXITCODE -ne 0) { throw 'SDK license step failed.' }
    & $sdkManager "--sdk_root=$SdkPath" 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
    if ($LASTEXITCODE -ne 0) { throw 'SDK installation failed.' }
}
$gradle = Join-Path $toolsRoot 'gradle-8.9/bin/gradle.bat'
if (!(Test-Path -LiteralPath $gradle)) {
    if (!$Bootstrap) { throw 'Missing Gradle 8.9. Use -Bootstrap to download a project-local copy.' }
    $gradleZip = Join-Path $toolsRoot 'gradle-8.9-bin.zip'
    Fetch 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' $gradleZip
    $checksum = (Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.9-bin.zip.sha256').Content.Trim()
    if ((Get-FileHash -LiteralPath $gradleZip -Algorithm SHA256).Hash.ToLowerInvariant() -ne $checksum.ToLowerInvariant()) { throw 'Gradle checksum mismatch.' }
    Expand-Archive -LiteralPath $gradleZip -DestinationPath $toolsRoot -Force
}
$sdkSetting = 'sdk.dir=' + $SdkPath.Replace('\','/').Replace(':','\:')
Set-Content -LiteralPath (Join-Path $projectRoot 'local.properties') -Value $sdkSetting -Encoding ascii
Push-Location $projectRoot
try {
    & $gradle --no-daemon --console=plain testDebugUnitTest lintDebug assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'Build or checks failed. APK was not delivered.' }
    $output = Join-Path $projectRoot 'output'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot 'app/build/outputs/apk/debug/app-debug.apk') -Destination (Join-Path $output 'HuanAssist-debug.apk') -Force
    Write-Host "APK ready: $output/HuanAssist-debug.apk"
} finally { Pop-Location }
