param(
    [Parameter(Position = 0)]
    [string] $Command = "up",

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Rest
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

Push-Location $RepoRoot
try {
    git pull origin main
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}

& (Join-Path $ScriptDir "prod.ps1") $Command @Rest
exit $LASTEXITCODE
