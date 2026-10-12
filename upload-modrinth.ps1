#Requires -Version 5.1
<#
.SYNOPSIS
    Uploads the built BukkitOfUtils jar to Modrinth as a new version.

.DESCRIPTION
    Reads the version from gradle.properties and the changelog from UPDATES.md, then
    creates a Modrinth version holding build\libs\BukkitOfUtils-<version>.jar.

    Game versions, loaders, and dependencies default to those of the latest version
    already on Modrinth, so a release only needs overrides when support changes.

    The Modrinth personal access token (scope: "Create versions") is read from
    -Token or the MODRINTH_TOKEN environment variable.

    Refuses to upload a version number that already exists on the project.

.PARAMETER Token
    Modrinth personal access token. Defaults to $env:MODRINTH_TOKEN.

.PARAMETER Project
    Modrinth project slug or ID. Defaults to "bukkitofutils".

.PARAMETER Version
    Version number to publish. Defaults to the version in gradle.properties.

.PARAMETER JarPath
    Jar to upload. Defaults to build\libs\<name>-<version>.jar.

.PARAMETER Changelog
    Path of the changelog file. Defaults to UPDATES.md.

.PARAMETER VersionType
    release, beta, or alpha. Defaults to release.

.PARAMETER GameVersions
    Overrides the Minecraft versions copied from the latest Modrinth version.

.PARAMETER Loaders
    Overrides the loaders copied from the latest Modrinth version.

.PARAMETER Featured
    Marks the new version as featured.

.PARAMETER Build
    Runs "gradlew.bat clean shadowJar" before uploading.

.PARAMETER DryRun
    Prints what would be uploaded without uploading anything.

.PARAMETER Force
    Skips the confirmation prompt.

.EXAMPLE
    .\upload-modrinth.ps1 -DryRun
    Shows the version, jar, changelog, and metadata that would be uploaded.

.EXAMPLE
    .\upload-modrinth.ps1 -Build
    Rebuilds the jar, then uploads it after confirmation.

.EXAMPLE
    .\upload-modrinth.ps1 -VersionType beta -Force
    Uploads the existing jar as a beta without prompting.

.EXAMPLE
    .\upload-modrinth.ps1 -GameVersions 1.21.11,26.3 -Loaders paper,folia
    Uploads with an explicit game version and loader list.
#>
[CmdletBinding()]
param(
    [string]$Token = $env:MODRINTH_TOKEN,
    [string]$Project = 'bukkitofutils',
    [string]$Version,
    [string]$JarPath,
    [string]$Changelog,
    [ValidateSet('release', 'beta', 'alpha')]
    [string]$VersionType = 'release',
    [string[]]$GameVersions,
    [string[]]$Loaders,
    [switch]$Featured,
    [switch]$Build,
    [switch]$DryRun,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12

$ApiBase = 'https://api.modrinth.com/v2'
# Modrinth rejects requests without an identifying User-Agent.
$UserAgent = 'Streamline-Essentials/BukkitOfUtils upload-modrinth (drak@plas.host)'

function Read-GradleProperty([string]$Key) {
    $file = Join-Path $PSScriptRoot 'gradle.properties'
    foreach ($line in Get-Content -LiteralPath $file) {
        if ($line -match "^\s*$([regex]::Escape($Key))\s*=\s*(.+?)\s*$") { return $Matches[1] }
    }
    throw "'$Key' not found in $file"
}

function Invoke-Modrinth([string]$Path) {
    Invoke-RestMethod -Uri "$ApiBase$Path" -Headers @{ 'User-Agent' = $UserAgent }
}

function Split-List([string[]]$Values) {
    # "powershell -File" passes "a,b" as one string rather than an array.
    @($Values | ForEach-Object { $_ -split ',' } | ForEach-Object { $_.Trim() } | Where-Object { $_ })
}

try {
    if ($GameVersions) { $GameVersions = Split-List $GameVersions }
    if ($Loaders) { $Loaders = Split-List $Loaders }

    $name = Read-GradleProperty 'name'
    if (-not $Version) { $Version = Read-GradleProperty 'version' }
    if (-not $JarPath) { $JarPath = Join-Path $PSScriptRoot "build\libs\$name-$Version.jar" }
    if (-not $Changelog) { $Changelog = Join-Path $PSScriptRoot 'UPDATES.md' }

    if ($Build) {
        Write-Host "Building $name $Version..."
        & (Join-Path $PSScriptRoot 'gradlew.bat') -p $PSScriptRoot clean shadowJar
        if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit $LASTEXITCODE)." }
    }

    if (-not (Test-Path -LiteralPath $JarPath)) { throw "Jar not found: $JarPath (build it, or pass -Build)." }
    if (-not (Test-Path -LiteralPath $Changelog)) { throw "Changelog not found: $Changelog" }
    $changelogText = (Get-Content -LiteralPath $Changelog -Raw -Encoding UTF8).Trim()
    if (-not $changelogText) { throw "Changelog is empty: $Changelog" }

    $projectInfo = Invoke-Modrinth "/project/$Project"
    # Invoke-RestMethod emits a JSON array as a single object; re-emitting it unrolls the elements.
    $existing = @(Invoke-Modrinth "/project/$($projectInfo.id)/version" | ForEach-Object { $_ })
    if ($existing | Where-Object { $_.version_number -eq $Version }) {
        throw "Version $Version already exists on Modrinth project '$($projectInfo.slug)'."
    }

    # The API lists versions newest first.
    $latest = $existing | Select-Object -First 1
    if (-not $GameVersions) {
        if (-not $latest) { throw 'No earlier version to copy game versions from; pass -GameVersions.' }
        $GameVersions = @($latest.game_versions)
    }
    if (-not $Loaders) {
        if (-not $latest) { throw 'No earlier version to copy loaders from; pass -Loaders.' }
        $Loaders = @($latest.loaders)
    }
    $dependencies = @()
    if ($latest) {
        $dependencies = @($latest.dependencies | ForEach-Object {
            @{ project_id = $_.project_id; version_id = $_.version_id; file_name = $_.file_name; dependency_type = $_.dependency_type }
        })
    }

    $jarName = [IO.Path]::GetFileName($JarPath)
    $data = [ordered]@{
        project_id     = $projectInfo.id
        name           = "$name $Version"
        version_number = $Version
        changelog      = $changelogText
        dependencies   = [object[]]$dependencies
        game_versions  = [string[]]$GameVersions
        loaders        = [string[]]$Loaders
        version_type   = $VersionType
        featured       = [bool]$Featured
        status         = 'listed'
        file_parts     = [string[]]@('jar')
        primary_file   = 'jar'
    }
    $json = ConvertTo-Json -InputObject $data -Depth 5

    Write-Host "Project:       $($projectInfo.title) ($($projectInfo.slug))"
    Write-Host "Version:       $Version ($VersionType)"
    Write-Host "Jar:           $JarPath ($([math]::Round((Get-Item -LiteralPath $JarPath).Length / 1KB)) KB)"
    Write-Host "Loaders:       $($Loaders -join ', ')"
    Write-Host "Game versions: $($GameVersions[0]) .. $($GameVersions[-1]) ($($GameVersions.Count) total)"
    Write-Host "Dependencies:  $($dependencies.Count) copied from $(if ($latest) { $latest.version_number } else { 'none' })"
    Write-Host "Changelog:"
    $changelogText -split "`r?`n" | ForEach-Object { Write-Host "  $_" }

    if ($DryRun) {
        Write-Host "`nDry run; nothing uploaded. Request data:"
        Write-Host $json
        exit 0
    }

    if (-not $Token) { throw 'No token: pass -Token or set MODRINTH_TOKEN.' }

    if (-not $Force) {
        $answer = Read-Host "`nUpload $name $Version to Modrinth? [y/N]"
        if ($answer -notmatch '^(y|yes)$') { Write-Host 'Cancelled.'; exit 1 }
    }

    Add-Type -AssemblyName System.Net.Http
    $client = New-Object System.Net.Http.HttpClient
    $client.Timeout = [TimeSpan]::FromMinutes(5)
    $client.DefaultRequestHeaders.TryAddWithoutValidation('Authorization', $Token) | Out-Null
    $client.DefaultRequestHeaders.TryAddWithoutValidation('User-Agent', $UserAgent) | Out-Null

    $form = New-Object System.Net.Http.MultipartFormDataContent
    $dataPart = New-Object System.Net.Http.StringContent($json, [Text.Encoding]::UTF8, 'application/json')
    $form.Add($dataPart, 'data')
    $fileStream = [IO.File]::OpenRead((Resolve-Path -LiteralPath $JarPath).Path)
    try {
        $filePart = New-Object System.Net.Http.StreamContent($fileStream)
        $filePart.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/java-archive')
        $form.Add($filePart, 'jar', $jarName)

        Write-Host "`nUploading..."
        $response = $client.PostAsync("$ApiBase/version", $form).GetAwaiter().GetResult()
        $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    } finally {
        $fileStream.Dispose()
        $form.Dispose()
        $client.Dispose()
    }

    if (-not $response.IsSuccessStatusCode) {
        throw "Modrinth returned $([int]$response.StatusCode) $($response.ReasonPhrase): $body"
    }
    $created = $body | ConvertFrom-Json
    Write-Host "Uploaded: https://modrinth.com/plugin/$($projectInfo.slug)/version/$($created.id)"
    exit 0
} catch {
    Write-Error $_
    exit 1
}
