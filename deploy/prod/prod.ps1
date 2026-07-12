param(
    [Parameter(Position = 0)]
    [string] $Command = "up",

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Rest
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

$ComposeArgs = @(
    "compose",
    "--env-file", (Join-Path $RepoRoot ".env"),
    "-f", (Join-Path $ScriptDir "docker-compose.yml")
)

if ($Command -eq "up") {
    $ComposeArgs += @("up", "-d", "--force-recreate")
} elseif ($Command -eq "logs") {
    $ComposeArgs += @("logs", "-f")
} else {
    $ComposeArgs += $Command
}

$ComposeArgs += $Rest

& docker @ComposeArgs
exit $LASTEXITCODE
