# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
. (Join-Path $PSScriptRoot 'preview.ps1')

$own = @('superman','push_ups_knees','push_ups_diamond','wall_push_ups','mountain_climbers','bicycle_crunches',
         'triceps_dips_floor','kneeling_triceps_extension','side_push_up','body_up','prone_elbow_lift',
         'scapula_push_up','towel_squat_row','reverse_plank_leg_lift','warmup_ankle_circles','warmup_wrist_circles')
$root = $repo
$assets = Join-Path $root 'app\src\main\assets'
$outDir = $work
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$catalog = [System.IO.File]::ReadAllText((Join-Path $root 'app\src\main\java\com\example\data\repository\ExerciseCatalog.kt'))
$ms = [regex]::Matches($catalog, 'id = "([a-z_0-9]+)",\s*name = "([^"]+)",\s*bodyPart = BodyPart\.([A-Z_]+)[\s\S]*?lottieAsset = "([^"]+)"')

$cellW = 720; $cellH = 400
$frames = @(0, 8, 15, 23, 30, 38, 45, 53)
$stripW = $cellW * $frames.Count
$items = @()

foreach ($m in $ms) {
  $key = [System.IO.Path]::GetFileNameWithoutExtension($m.Groups[4].Value)
  if ($own -notcontains $key) { continue }
  $json = Join-Path $assets $m.Groups[4].Value
  $j = Get-Content $json -Raw | ConvertFrom-Json
  $scale = [math]::Min($cellW / [double]$j.w, $cellH / [double]$j.h)
  $strip = New-Object System.Drawing.Bitmap $stripW, $cellH
  $g = [System.Drawing.Graphics]::FromImage($strip)
  $g.Clear([System.Drawing.Color]::FromArgb(255, 247, 249, 248))
  $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $i = 0
  foreach ($f in $frames) {
    $tmp = Join-Path $outDir '_big.png'
    Render-Anim $json $tmp $scale $f | Out-Null
    $img = [System.Drawing.Image]::FromFile($tmp)
    $dx = $i * $cellW + [int](($cellW - $img.Width) / 2)
    $dy = [int](($cellH - $img.Height) / 2)
    $g.DrawImage($img, $dx, $dy, $img.Width, $img.Height)
    $img.Dispose()
    $i++
  }
  $g.Dispose()
  $stripPath = Join-Path $outDir "grande-$key.png"
  $strip.Save($stripPath, [System.Drawing.Imaging.ImageFormat]::Png)
  $strip.Dispose()
  $b64 = [System.Convert]::ToBase64String([System.IO.File]::ReadAllBytes($stripPath))
  $partTxt = switch ($m.Groups[3].Value) {
    'CUERPO_COMPLETO' { 'Cuerpo completo' }
    'PECHO' { 'Pecho' }
    'ABDOMEN' { 'Abdomen' }
    'PIERNAS' { 'Piernas' }
    'BRAZOS' { 'Brazos' }
    'ESPALDA' { 'Espalda' }
    default { $m.Groups[3].Value }
  }
  $items += [pscustomobject]@{ name = $m.Groups[2].Value; part = $partTxt; b64 = $b64; key = $key }
  "  {0,-30} {1,4} KB" -f $key, [math]::Round((Get-Item $stripPath).Length / 1KB)
}
Remove-Item (Join-Path $outDir '_big.png') -ErrorAction SilentlyContinue

$json = $items | ForEach-Object {
  '{"name":"' + ($_.name -replace '"','\"') + '","part":"' + $_.part + '","key":"' + $_.key + '","src":"data:image/png;base64,' + $_.b64 + '"}'
} | Join-String -Separator ','
if (-not $json) { throw 'sin datos' }

$html = @"
<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>FitCasa - visor de animaciones</title>
<style>
  * { box-sizing: border-box; }
  body { margin: 0; min-height: 100vh; padding: 22px 26px 40px; font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
         background: #f2f5f4; color: #16211f; display: flex; flex-direction: column; align-items: center; }
  header { width: 100%; max-width: 1000px; display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }
  h1 { font-size: 22px; margin: 0 0 2px; letter-spacing: -0.3px; }
  .counter { font-size: 13px; color: #5a6b67; }
  .stage { width: 100%; max-width: 1000px; margin: 14px 0 0; background: #fff; border: 1px solid #e2eae7;
           border-radius: 18px; overflow: hidden; box-shadow: 0 1px 3px rgba(18,42,36,.07); }
  .viewport { position: relative; width: 100%; aspect-ratio: 720 / 400; overflow: hidden; background: #f7f9f8; }
  .viewport img { position: absolute; top: 0; left: 0; width: 800%; max-width: none; display: block;
                  transform: translateX(0); }
  .meta { padding: 12px 16px 14px; border-top: 1px solid #eef3f1; display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap; }
  .meta strong { font-size: 17px; }
  .meta span { font-size: 11px; letter-spacing: .07em; text-transform: uppercase; color: #6b7d78; }
  .controls { width: 100%; max-width: 1000px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-top: 14px; }
  button { border: 1px solid #cdd8d5; background: #fff; color: #22332f; font: inherit; font-size: 14px;
           padding: 9px 16px; border-radius: 999px; cursor: pointer; }
  button:hover { border-color: #9db3ae; }
  button.main { background: #0f3d33; border-color: #0f3d33; color: #fff; min-width: 110px; }
  button:disabled { opacity: .4; cursor: default; }
  .slider { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #455854; }
  input[type=range] { accent-color: #0f3d33; width: 170px; }
  .keys { width: 100%; max-width: 1000px; margin-top: 12px; font-size: 12px; color: #6b7d78; }
  kbd { background: #e7eeec; border-radius: 4px; padding: 1px 5px; font-family: inherit; font-size: 11px; }
</style>
</head>
<body>
<header>
  <div><h1>Visor de animaciones propias</h1></div>
  <div class="counter" id="counter"></div>
</header>

<div class="stage">
  <div class="viewport"><img id="shot" alt=""></div>
  <div class="meta"><strong id="name"></strong><span id="part"></span></div>
</div>

<div class="controls">
  <button id="prev">&#8249; Anterior</button>
  <button class="main" id="play">Pausar</button>
  <button id="next">Siguiente &#8250;</button>
  <label class="slider">Velocidad <input type="range" id="speed" min="25" max="200" step="25" value="100"></label>
  <label class="slider">Fotograma <input type="range" id="frame" min="0" max="7" step="1" value="0"> <span id="frameLbl">0</span></label>
</div>

<div class="keys">
  <kbd>&larr;</kbd> <kbd>&rarr;</kbd> cambiar de animación &nbsp;·&nbsp; <kbd>espacio</kbd> pausa &nbsp;·&nbsp;
  <kbd>&uarr;</kbd> <kbd>&darr;</kbd> paso de fotograma &nbsp;·&nbsp; <kbd>1</kbd>–<kbd>8</kbd> ir a un fotograma
</div>

<script>
  const DATA = [$json];
  const FRAMES = 8, TAGS = [0, 8, 15, 23, 30, 38, 45, 53], BASE = 1450;
  let i = 0, f = 0, playing = true, speed = 1, timer = null;

  const shot = document.getElementById('shot');
  const name = document.getElementById('name');
  const part = document.getElementById('part');
  const counter = document.getElementById('counter');
  const playBtn = document.getElementById('play');
  const frameInput = document.getElementById('frame');
  const frameLbl = document.getElementById('frameLbl');
  const speedInput = document.getElementById('speed');

  function paint() {
    shot.style.transform = 'translateX(' + (-f * 100 / FRAMES) + '%)';
    frameLbl.textContent = TAGS[f];
    frameInput.value = f;
  }
  function show(idx) {
    i = (idx + DATA.length) % DATA.length;
    shot.src = DATA[i].src;
    name.textContent = DATA[i].name;
    part.textContent = DATA[i].part;
    counter.textContent = (i + 1) + ' / ' + DATA.length;
    f = 0; paint();
  }
  function loop() {
    if (timer) clearInterval(timer);
    timer = setInterval(() => { if (!playing) return; f = (f + 1) % FRAMES; paint(); }, BASE / FRAMES / speed);
  }
  function setPlaying(v) { playing = v; playBtn.textContent = v ? 'Pausar' : 'Reproducir'; loop(); }

  playBtn.onclick = () => setPlaying(!playing);
  document.getElementById('prev').onclick = () => show(i - 1);
  document.getElementById('next').onclick = () => show(i + 1);
  frameInput.oninput = () => { f = +frameInput.value; setPlaying(false); paint(); };
  speedInput.oninput = () => { speed = +speedInput.value / 100; loop(); };
  window.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowRight') show(i + 1);
    else if (e.key === 'ArrowLeft') show(i - 1);
    else if (e.key === ' ') { e.preventDefault(); setPlaying(!playing); }
    else if (e.key === 'ArrowUp') { setPlaying(false); f = (f + 1) % FRAMES; paint(); }
    else if (e.key === 'ArrowDown') { setPlaying(false); f = (f + FRAMES - 1) % FRAMES; paint(); }
    else if (/^[1-8]$/.test(e.key)) { setPlaying(false); f = +e.key - 1; paint(); }
  });

  show(0); loop();
</script>
</body>
</html>
"@

$htmlPath = Join-Path $outDir 'detalle.html'
[System.IO.File]::WriteAllText($htmlPath, $html, (New-Object System.Text.UTF8Encoding($false)))
"detalle: {0:N1} MB  |  animaciones: {1}" -f ((Get-Item $htmlPath).Length / 1MB), $items.Count
$htmlPath
