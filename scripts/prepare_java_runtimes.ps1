param(
    [string]$SourceApk,
    [string]$DestinationRoot
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$separator = [System.IO.Path]::DirectorySeparatorChar
$repoPrefix = $repoRoot.TrimEnd($separator) + $separator
if ([string]::IsNullOrWhiteSpace($DestinationRoot)) {
    $DestinationRoot = Join-Path $repoRoot "app_pojavlauncher/src/main/assets/components"
}
$destinationPath = [System.IO.Path]::GetFullPath($DestinationRoot)
if (-not $destinationPath.StartsWith($repoPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Destination must be inside the Flint Android repository: $destinationPath"
}

$upstreamApkUrl = "https://github.com/PojavLauncherTeam/PojavLauncher/releases/download/gladiolus/PojavLauncher.apk"
$upstreamApkSha256 = "CC8479E1600E3A094D2184BBB88B19809CE41A0F8F7882AEFD4527C9D032FC56"
$cacheRoot = Join-Path $repoRoot ".gradle/flint-java-runtimes"
$cachedApk = Join-Path $cacheRoot "PojavLauncher-gladiolus.apk"
$stagingRoot = Join-Path $cacheRoot "staging"
$runtimeNames = @("jre", "jre-new", "jre-21")
$runtimeFiles = @(
    "version", "universal.tar.xz", "bin-arm.tar.xz", "bin-arm64.tar.xz",
    "bin-x86.tar.xz", "bin-x86_64.tar.xz"
)

function Assert-UnderRepository {
    param([string]$Path)
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if (-not $fullPath.StartsWith($repoPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to modify a path outside the Flint Android repository: $fullPath"
    }
    return $fullPath
}

function Assert-UpstreamApk {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Upstream runtime APK does not exist: $Path"
    }
    $actualHash = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
    if ($actualHash -ne $upstreamApkSha256) {
        throw "Official PojavLauncher APK integrity check failed. Expected $upstreamApkSha256, got $actualHash."
    }
}

New-Item -ItemType Directory -Force -Path $cacheRoot | Out-Null
if ([string]::IsNullOrWhiteSpace($SourceApk)) {
    $downloadRequired = -not (Test-Path -LiteralPath $cachedApk -PathType Leaf)
    if (-not $downloadRequired) {
        try { Assert-UpstreamApk $cachedApk } catch { $downloadRequired = $true }
    }
    if ($downloadRequired) {
        Write-Host "Downloading the official PojavLauncher Gladiolus APK runtime bundle..."
        Invoke-WebRequest -Uri $upstreamApkUrl -OutFile $cachedApk
    }
    $SourceApk = $cachedApk
}
$sourceApkPath = (Resolve-Path $SourceApk).Path
Assert-UpstreamApk $sourceApkPath

$safeStagingRoot = Assert-UnderRepository $stagingRoot
if (Test-Path -LiteralPath $safeStagingRoot) {
    Remove-Item -LiteralPath $safeStagingRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $safeStagingRoot | Out-Null

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($sourceApkPath)
try {
    foreach ($runtimeName in $runtimeNames) {
        $runtimeStaging = Join-Path $safeStagingRoot $runtimeName
        New-Item -ItemType Directory -Force -Path $runtimeStaging | Out-Null
        foreach ($runtimeFile in $runtimeFiles) {
            $entryName = "assets/components/$runtimeName/$runtimeFile"
            $entry = $archive.GetEntry($entryName)
            if ($null -eq $entry -or $entry.Length -le 0) {
                throw "The official upstream APK is missing runtime component $entryName"
            }
            $outputPath = Join-Path $runtimeStaging $runtimeFile
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $outputPath, $true)
            if ((Get-Item -LiteralPath $outputPath).Length -ne $entry.Length) {
                throw "Runtime component extraction was truncated: $entryName"
            }
        }
    }
} finally {
    $archive.Dispose()
}

foreach ($runtimeName in $runtimeNames) {
    $runtimeDestination = Assert-UnderRepository (Join-Path $destinationPath $runtimeName)
    if (Test-Path -LiteralPath $runtimeDestination) {
        Remove-Item -LiteralPath $runtimeDestination -Recurse -Force
    }
    Move-Item -LiteralPath (Join-Path $safeStagingRoot $runtimeName) -Destination $runtimeDestination
}

Write-Host "Prepared official Java 8, 17, and 21 runtime components in $destinationPath"
