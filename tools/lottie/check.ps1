param(
  [string]$keys = 'mountain_climbers,prone_elbow_lift,scapula_push_up',
  [string]$outName = 'check.png',
  [int]$cellW = 460,
  [int]$cols = 6
)
# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
. (Join-Path $PSScriptRoot 'preview.ps1')

$dir = (Join-Path $repo 'app\src\main\assets\exercises\lottie')
$outDir = (Join-Path $work 'diag')
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$rows = $keys -split ','
$frames = @(0, 8, 15, 23, 30, 38, 45, 53)
if ($cols -eq 6) { $frames = @(0, 9, 18, 27, 36, 45) }
$cellH = [int]($cellW * 0.72)

$bmp = New-Object System.Drawing.Bitmap ($cellW * $cols), ((30 + $cellH) * $rows.Count)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::White)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$fontS = New-Object System.Drawing.Font('Consolas', 11)

$r = 0
foreach ($key in $rows) {
  $json = Join-Path $dir ($key.Trim() + '.json')
  $j = Get-Content $json -Raw | ConvertFrom-Json
  $scale = [math]::Min($cellW / [double]$j.w, $cellH / [double]$j.h) * 0.99
  $c = 0
  foreach ($f in $frames) {
    $tmp = Join-Path $outDir '_c.png'
    Render-Anim $json $tmp $scale $f | Out-Null
    $img = [System.Drawing.Image]::FromFile($tmp)
    $x = $c * $cellW + [int](($cellW - $img.Width) / 2)
    $y = 22 + $r * (30 + $cellH) + [int](($cellH - $img.Height) / 2)
    $g.DrawImage($img, $x, $y, $img.Width, $img.Height)
    $img.Dispose()
    $g.DrawString(("f{0}  {1}" -f $f, $key.Trim()), $fontS, [System.Drawing.Brushes]::Gray, $c * $cellW + 4, $r * (30 + $cellH) + 3)
    $c++
  }
  $g.DrawLine([System.Drawing.Pens]::Gainsboro, 0, ($r + 1) * (30 + $cellH), $bmp.Width, ($r + 1) * (30 + $cellH))
  $r++
}
Remove-Item (Join-Path $outDir '_c.png') -ErrorAction SilentlyContinue
$out = Join-Path $outDir $outName
$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
$out
