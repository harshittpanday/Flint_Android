param(
    [string]$Destination
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if ([string]::IsNullOrWhiteSpace($Destination)) {
    $Destination = Join-Path $repoRoot "app_pojavlauncher\src\main\assets\components\jre-new"
}
$destinationPath = [System.IO.Path]::GetFullPath($Destination)
$repoPrefix = $repoRoot.TrimEnd('\') + '\'
if (-not $destinationPath.StartsWith($repoPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Destination must be inside the Flint Android repository: $destinationPath"
}

$cacheRoot = Join-Path $repoRoot ".gradle\flint-jre17"
$downloadRoot = Join-Path $cacheRoot "downloads"
$workRoot = Join-Path $cacheRoot "work"
$outputRoot = Join-Path $cacheRoot "output"
$gitTar = "C:\Program Files\Git\usr\bin\tar.exe"
$usingGitTar = Test-Path -LiteralPath $gitTar
$tarCommand = if ($usingGitTar) { $gitTar } else { "tar.exe" }
if ($usingGitTar) {
    $env:Path = "C:\Program Files\Git\usr\bin;C:\Program Files\Git\mingw64\bin;$env:Path"
}

$archives = @(
    @{
        Arch = "arm"
        File = "jre17-arm-20210914-release.tar.xz"
        Url = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-arm-20210914-release.tar.xz"
        Sha256 = "1C27A6A839FC76FC14618AD547B212F881F8D5B8DFA0C5853A38817BA316B428"
    },
    @{
        Arch = "arm64"
        File = "jre17-arm64-20210825-release.tar.xz"
        Url = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-arm64-20210825-release.tar.xz"
        Sha256 = "C64583AC2E0EC8857E43456FA9ADCF482C6A8E454A7133173BF15692D2478B8D"
    },
    @{
        Arch = "x86"
        File = "jre17-x86-20220225-release.tar.xz"
        Url = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-x86-20220225-release.tar.xz"
        Sha256 = "51A51B05CB19A830733864C10A1A706353224353941842362DC6376CC0AB0658"
    },
    @{
        Arch = "x86_64"
        File = "jre17-x86_64-20210825-release.tar.xz"
        Url = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-x86_64-20210825-release.tar.xz"
        Sha256 = "EBBDF75AB864A83671A108032C30E67174F79CC19596CFC1D7BFB71BE26B6E71"
    }
)

function Invoke-Tar {
    param([string[]]$Arguments)
    if ($script:usingGitTar) {
        $tarArguments = $Arguments | ForEach-Object {
            if ($_ -match '^([A-Za-z]):\\(.*)$') {
                "/$($Matches[1].ToLower())/$($Matches[2].Replace('\', '/'))"
            } else {
                $_
            }
        }
        & $script:tarCommand @tarArguments
    } else {
        & $script:tarCommand @Arguments
    }
    if ($LASTEXITCODE -ne 0) {
        throw "$script:tarCommand failed with exit code ${LASTEXITCODE}: $($Arguments -join ' ')"
    }
}

function Assert-ArchiveHash {
    param([string]$Path, [string]$Expected)
    $actual = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
    if ($actual -ne $Expected) {
        throw "Runtime archive integrity check failed for $(Split-Path $Path -Leaf). Expected $Expected, got $actual."
    }
}

New-Item -ItemType Directory -Force -Path $downloadRoot | Out-Null
foreach ($archive in $archives) {
    $archivePath = Join-Path $downloadRoot $archive.File
    $download = -not (Test-Path -LiteralPath $archivePath)
    if (-not $download) {
        try { Assert-ArchiveHash $archivePath $archive.Sha256 } catch { $download = $true }
    }
    if ($download) {
        Write-Host "Downloading official Pojav JRE 17 for $($archive.Arch)..."
        Invoke-WebRequest -Uri $archive.Url -OutFile $archivePath
    }
    Assert-ArchiveHash $archivePath $archive.Sha256
}

foreach ($path in @($workRoot, $outputRoot)) {
    $fullPath = [System.IO.Path]::GetFullPath($path)
    if (-not $fullPath.StartsWith($repoPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean unexpected path: $fullPath"
    }
    if (Test-Path -LiteralPath $fullPath) { Remove-Item -LiteralPath $fullPath -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $fullPath | Out-Null
}

# Reproduce the inherited Pojav 9_repackjre.sh layout: one architecture-neutral
# archive plus a small archive containing binaries/native libraries per ABI.
$arm64 = $archives | Where-Object Arch -eq "arm64"
$universalRoot = Join-Path $workRoot "universal"
New-Item -ItemType Directory -Force -Path $universalRoot | Out-Null
Invoke-Tar @("-xJf", (Join-Path $downloadRoot $arm64.File), "-C", $universalRoot)
foreach ($relativePath in @("bin", "lib\server", "lib\client", "lib\jexec", "lib\jvm.cfg", "release")) {
    $path = Join-Path $universalRoot $relativePath
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path -Recurse -Force }
}
Get-ChildItem -LiteralPath $universalRoot -Recurse -File -Filter "*.so" | Remove-Item -Force
Invoke-Tar @("-cJf", (Join-Path $outputRoot "universal.tar.xz"), "-C", $universalRoot, ".")

foreach ($archive in $archives) {
    $extractRoot = Join-Path $workRoot "extract-$($archive.Arch)"
    $binpackRoot = Join-Path $workRoot "binpack-$($archive.Arch)"
    New-Item -ItemType Directory -Force -Path $extractRoot, (Join-Path $binpackRoot "lib") | Out-Null
    Invoke-Tar @("-xJf", (Join-Path $downloadRoot $archive.File), "-C", $extractRoot)

    Move-Item -LiteralPath (Join-Path $extractRoot "bin") -Destination (Join-Path $binpackRoot "bin")
    foreach ($relativePath in @("lib\jexec", "lib\jvm.cfg", "lib\server", "lib\client")) {
        $source = Join-Path $extractRoot $relativePath
        if (Test-Path -LiteralPath $source) {
            Move-Item -LiteralPath $source -Destination (Join-Path $binpackRoot "lib")
        }
    }
    foreach ($nativeLibrary in Get-ChildItem -LiteralPath $extractRoot -Recurse -File -Filter "*.so") {
        $target = Join-Path (Join-Path $binpackRoot "lib") $nativeLibrary.Name
        if (Test-Path -LiteralPath $target) {
            throw "Duplicate native library while packing $($archive.Arch): $($nativeLibrary.Name)"
        }
        Move-Item -LiteralPath $nativeLibrary.FullName -Destination $target
    }
    Move-Item -LiteralPath (Join-Path $extractRoot "release") -Destination (Join-Path $binpackRoot "release")
    Invoke-Tar @("-cJf", (Join-Path $outputRoot "bin-$($archive.Arch).tar.xz"), "-C", $binpackRoot, ".")
}

Set-Content -LiteralPath (Join-Path $outputRoot "version") -Value "jre17-ec28559" -NoNewline -Encoding ascii

if (Test-Path -LiteralPath $destinationPath) { Remove-Item -LiteralPath $destinationPath -Recurse -Force }
New-Item -ItemType Directory -Force -Path (Split-Path $destinationPath -Parent) | Out-Null
Move-Item -LiteralPath $outputRoot -Destination $destinationPath

Write-Host "Prepared verified JRE 17 assets in $destinationPath"
