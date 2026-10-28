# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
. (Join-Path $PSScriptRoot 'preview.ps1')

$root = $repo
$assets = Join-Path $root 'app\src\main\assets'
$outDir = $work
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$catalog = [System.IO.File]::ReadAllText((Join-Path $root 'app\src\main\java\com\example\data\repository\ExerciseCatalog.kt'))
$ms = [regex]::Matches($catalog, 'id = "([a-z_0-9]+)",\s*name = "([^"]+)",\s*bodyPart = BodyPart\.([A-Z_]+)[\s\S]*?lottieAsset = "([^"]+)"')

$cellW = 360; $cellH = 200
$frames = @(0, 8, 15, 23, 30, 38, 45, 53)
$stripW = $cellW * $frames.Count
$cache = @{}
$items = @()

foreach ($m in $ms) {
  $id = $m.Groups[1].Value
  $name = $m.Groups[2].Value
  $part = $m.Groups[3].Value
  $asset = $m.Groups[4].Value
  $json = Join-Path $assets $asset
  $key = [System.IO.Path]::GetFileNameWithoutExtension($json)

  if (-not $cache.ContainsKey($key)) {
    $j = Get-Content $json -Raw | ConvertFrom-Json
    $scale = [math]::Min($cellW / [double]$j.w, $cellH / [double]$j.h)
    $strip = New-Object System.Drawing.Bitmap $stripW, $cellH
    $g = [System.Drawing.Graphics]::FromImage($strip)
    $g.Clear([System.Drawing.Color]::FromArgb(255, 247, 249, 248))
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $i = 0
    foreach ($f in $frames) {
      $tmp = Join-Path $outDir "_f.png"
      Render-Anim $json $tmp $scale $f | Out-Null
      $img = [System.Drawing.Image]::FromFile($tmp)
      $dx = $i * $cellW + [int](($cellW - $img.Width) / 2)
      $dy = [int](($cellH - $img.Height) / 2)
      $g.DrawImage($img, $dx, $dy, $img.Width, $img.Height)
      $img.Dispose()
      $i++
    }
    $g.Dispose()
    $stripPath = Join-Path $outDir "$key.png"
    $strip.Save($stripPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $strip.Dispose()
    $cache[$key] = "galeria/$key.png"
    "  listo: $key"
  }
  $items += [pscustomobject]@{ id = $id; name = $name; part = $part; img = $cache[$key] }
}
Remove-Item (Join-Path $outDir '_f.png') -ErrorAction SilentlyContinue

$cards = foreach ($it in $items) {
  $partTxt = switch ($it.part) {
    'CUERPO_COMPLETO' { 'Cuerpo completo' }
    'PECHO' { 'Pecho' }
    'ABDOMEN' { 'Abdomen' }
    'PIERNAS' { 'Piernas' }
    'BRAZOS' { 'Brazos' }
    'ESPALDA' { 'Espalda' }
    default { $it.part }
  }
  @"
      <figure class="card" data-part="$($it.part)">
        <div class="sprite" style="background-image:url('$($it.img)')"></div>
        <figcaption><strong>$($it.name)</strong><span>$partTxt</span></figcaption>
      </figure>
"@
}

$html = @"
<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>FitCasa · las 36 animaciones</title>
<style>
  :root { color-scheme: light; }
  * { box-sizing: border-box; }
  body { margin: 0; padding: 28px 32px 60px; font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
         background: #f2f5f4; color: #16211f; }
  header { display: flex; flex-wrap: wrap; align-items: baseline; gap: 16px; margin-bottom: 6px; }
  h1 { font-size: 26px; margin: 0; letter-spacing: -0.4px; }
  .hint { color: #5a6b67; font-size: 14px; margin: 0 0 22px; }
  .filters { display: flex; flex-wrap: wrap; gap: 8px; margin: 0 0 22px; }
  .filters button { border: 1px solid #cdd8d5; background: #fff; color: #2b3a37; font: inherit; font-size: 13px;
                    padding: 6px 12px; border-radius: 999px; cursor: pointer; }
  .filters button[aria-pressed="true"] { background: #0f3d33; border-color: #0f3d33; color: #fff; }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 18px; }
  .card { margin: 0; background: #fff; border: 1px solid #e2eae7; border-radius: 16px; overflow: hidden;
          box-shadow: 0 1px 2px rgba(18, 42, 36, .06); }
  .sprite { width: 100%; aspect-ratio: 360 / 200; background-repeat: no-repeat;
            background-size: 800% 100%; animation: play 1.45s steps(8) infinite; }
  body.paused .sprite { animation-play-state: paused; }
  @keyframes play { from { background-position: 0 0; } to { background-position: -800% 0; } }
  figcaption { padding: 10px 14px 12px; display: flex; flex-direction: column; gap: 2px; border-top: 1px solid #eef3f1; }
  figcaption strong { font-size: 14px; font-weight: 600; }
  figcaption span { font-size: 12px; color: #6b7d78; text-transform: uppercase; letter-spacing: .06em; }
  .hidden { display: none; }
</style>
</head>
<body>
<header><h1>FitCasa · las 36 animaciones</h1></header>
<p class="hint">Vista previa de los JSON que lleva la app (mismo dibujo que en el móvil). Cada tarjeta reproduce el ciclo completo.</p>
<div class="filters" id="filters">
  <button data-part="*" aria-pressed="true">Todas</button>
  <button data-part="CUERPO_COMPLETO" aria-pressed="false">Cuerpo completo</button>
  <button data-part="PECHO" aria-pressed="false">Pecho</button>
  <button data-part="ABDOMEN" aria-pressed="false">Abdomen</button>
  <button data-part="PIERNAS" aria-pressed="false">Piernas</button>
  <button data-part="BRAZOS" aria-pressed="false">Brazos</button>
  <button data-part="ESPALDA" aria-pressed="false">Espalda</button>
  <button id="pause" aria-pressed="false">Pausar</button>
</div>
<main class="grid" id="grid">
$($cards -join "`n")
</main>
<script>
  const grid = document.getElementById('grid');
  document.getElementById('filters').addEventListener('click', (e) => {
    const b = e.target.closest('button'); if (!b) return;
    if (b.id === 'pause') { document.body.classList.toggle('paused'); b.setAttribute('aria-pressed', document.body.classList.contains('paused')); return; }
    const part = b.dataset.part;
    document.querySelectorAll('#filters button[data-part]').forEach(x => x.setAttribute('aria-pressed', x === b));
    grid.querySelectorAll('.card').forEach(c => c.classList.toggle('hidden', part !== '*' && c.dataset.part !== part));
  });
</script>
</body>
</html>
"@

$htmlPath = Join-Path $outDir 'index.html'
[System.IO.File]::WriteAllText($htmlPath, $html, (New-Object System.Text.UTF8Encoding($false)))
"tarjetas: $($items.Count)  |  strips: $($cache.Count)"
$htmlPath
