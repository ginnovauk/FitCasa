# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
$root = $repo
$assets = Join-Path $root 'app\src\main\assets'
$outDir = $work

$catalog = [System.IO.File]::ReadAllText((Join-Path $root 'app\src\main\java\com\example\data\repository\ExerciseCatalog.kt'))
$ms = [regex]::Matches($catalog, 'id = "([a-z_0-9]+)",\s*name = "([^"]+)",\s*bodyPart = BodyPart\.([A-Z_]+)[\s\S]*?lottieAsset = "([^"]+)"')

$b64 = @{}
$cards = foreach ($m in $ms) {
  $name = $m.Groups[2].Value
  $part = $m.Groups[3].Value
  $key = [System.IO.Path]::GetFileNameWithoutExtension($m.Groups[4].Value)
  if (-not $b64.ContainsKey($key)) {
    $b64[$key] = [System.Convert]::ToBase64String([System.IO.File]::ReadAllBytes((Join-Path $outDir "$key.png")))
  }
  $partTxt = switch ($part) {
    'CUERPO_COMPLETO' { 'Cuerpo completo' }
    'PECHO' { 'Pecho' }
    'ABDOMEN' { 'Abdomen' }
    'PIERNAS' { 'Piernas' }
    'BRAZOS' { 'Brazos' }
    'ESPALDA' { 'Espalda' }
    default { $part }
  }
  @"
      <figure class="card" data-part="$part">
        <div class="sprite" style="background-image:url('data:image/png;base64,$($b64[$key])')"></div>
        <figcaption><strong>$name</strong><span>$partTxt</span></figcaption>
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
  * { box-sizing: border-box; }
  body { margin: 0; padding: 26px 28px 60px; font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
         background: #f2f5f4; color: #16211f; }
  h1 { font-size: 25px; margin: 0 0 4px; letter-spacing: -0.4px; }
  .hint { color: #5a6b67; font-size: 14px; margin: 0 0 18px; }
  .filters { display: flex; flex-wrap: wrap; gap: 8px; margin: 0 0 20px; }
  .filters button { border: 1px solid #cdd8d5; background: #fff; color: #2b3a37; font: inherit; font-size: 13px;
                    padding: 6px 12px; border-radius: 999px; cursor: pointer; }
  .filters button[aria-pressed="true"] { background: #0f3d33; border-color: #0f3d33; color: #fff; }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
  .card { margin: 0; background: #fff; border: 1px solid #e2eae7; border-radius: 16px; overflow: hidden;
          box-shadow: 0 1px 2px rgba(18, 42, 36, .06); }
  .sprite { width: 100%; aspect-ratio: 360 / 200; background-repeat: no-repeat;
            background-size: 800% 100%; animation: play 1.45s steps(8) infinite; }
  body.paused .sprite { animation-play-state: paused; }
  @keyframes play { from { background-position: 0% 0; } to { background-position: -800% 0; } }
  figcaption { padding: 9px 13px 11px; display: flex; flex-direction: column; gap: 2px; border-top: 1px solid #eef3f1; }
  figcaption strong { font-size: 14px; font-weight: 600; }
  figcaption span { font-size: 11px; color: #6b7d78; text-transform: uppercase; letter-spacing: .07em; }
  .hidden { display: none; }
</style>
</head>
<body>
<h1>FitCasa · las 36 animaciones</h1>
<p class="hint">Cada tarjeta reproduce el ciclo completo del JSON que lleva la app.</p>
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
"html: {0:N1} MB" -f ((Get-Item $htmlPath).Length / 1MB)
$htmlPath
