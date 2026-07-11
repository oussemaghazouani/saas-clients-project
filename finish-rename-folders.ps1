# ============================================================================
#  Termine le renommage "Brandwood -> SaaS Client" pour les DOSSIERS physiques.
#  À EXÉCUTER VS CODE FERMÉ (un dossier ouvert/utilisé ne peut pas être renommé).
#
#  Étapes :
#   1. Fermez VS Code complètement.
#   2. Ouvrez une fenêtre PowerShell (hors VS Code) dans ce dossier.
#   3. Lancez :  powershell -ExecutionPolicy Bypass -File .\finish-rename-folders.ps1
#   4. Rouvrez "saas-client.code-workspace" dans VS Code.
#  (Vous pouvez supprimer ce script ensuite.)
# ============================================================================
$ErrorActionPreference = 'Stop'
$root = "c:\Users\ousam\Desktop\module1_brandwood_auth\module1"

Rename-Item (Join-Path $root "brandwood-backend")  "backend"
Rename-Item (Join-Path $root "brandwood-frontend") "frontend"

$utf8  = New-Object System.Text.UTF8Encoding($false)
$files = @(
    (Join-Path $root "saas-client.code-workspace"),
    (Join-Path $root ".vscode\launch.json"),
    (Join-Path $root ".vscode\tasks.json")
)
foreach ($file in $files) {
    $c = [System.IO.File]::ReadAllText($file, $utf8)
    $c = $c -replace 'brandwood-backend',  'backend'
    $c = $c -replace 'brandwood-frontend', 'frontend'
    [System.IO.File]::WriteAllText($file, $c, $utf8)
}

Write-Host "OK - dossiers renommes en 'backend' / 'frontend' et references mises a jour." -ForegroundColor Green
Write-Host "Rouvrez saas-client.code-workspace dans VS Code." -ForegroundColor Green
