# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
$own = @('superman','push_ups_knees','push_ups_diamond','wall_push_ups','mountain_climbers','bicycle_crunches',
         'triceps_dips_floor','kneeling_triceps_extension','side_push_up','body_up','prone_elbow_lift',
         'scapula_push_up','towel_squat_row','reverse_plank_leg_lift','warmup_ankle_circles','warmup_wrist_circles')
$root = $repo
$outDir = $work
$catalog = [System.IO.File]::ReadAllText((Join-Path $root 'app\src\main\java\com\example\data\repository\ExerciseCatalog.kt'))
$ms = [regex]::Matches($catalog, 'id = "([a-z_0-9]+)",\s*name = "([^"]+)",\s*bodyPart = BodyPart\.([A-Z_]+)[\s\S]*?lottieAsset = "([^"]+)"')

$cards = @()
$n = 0
foreach ($m in $ms) {
  $key = [System.IO.Path]::GetFileNameWithoutExtension($m.Groups[4].Value)
  if ($own -notcontains $key) { continue }
  $n++
  $num = '{0:00}' -f $n
  $b64 = [System.Convert]::ToBase64String([System.IO.File]::ReadAllBytes((Join-Path $outDir "$key.png")))
  $cards += @"
      <figure class="card" id="anim-$num" data-num="$n">
        <div class="frame"><img alt="" src="data:image/png;base64,$b64"></div>
        <figcaption><strong><span class="num">$num</span>$($m.Groups[2].Value)</strong><span>propia de FitCasa · $key</span></figcaption>
      </figure>
"@
}

$html = @"
<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>FitCasa - animaciones propias</title>
<style>
  * { box-sizing: border-box; }
  body { margin: 0; padding: 26px 28px 48px; font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
         background: #f2f5f4; color: #16211f; }
  h1 { font-size: 24px; margin: 0 0 4px; letter-spacing: -0.4px; }
  .hint { color: #5a6b67; font-size: 14px; margin: 0 0 16px; }
  .bar { display: flex; gap: 8px; margin: 0 0 20px; }
  .bar button { border: 1px solid #cdd8d5; background: #fff; color: #2b3a37; font: inherit; font-size: 13px;
                padding: 6px 14px; border-radius: 999px; cursor: pointer; }
  .bar button[aria-pressed="true"] { background: #0f3d33; border-color: #0f3d33; color: #fff; }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
  .card { margin: 0; background: #fff; border: 1px solid #e2eae7; border-radius: 16px; overflow: hidden;
          box-shadow: 0 1px 2px rgba(18, 42, 36, .06); }
  .frame { position: relative; width: 100%; aspect-ratio: 360 / 200; overflow: hidden; background: #f7f9f8; }
  .frame img { position: absolute; top: 0; left: 0; width: 800%; max-width: none; display: block;
               animation: slide 1.45s steps(8) infinite; }
  .slow .frame img { animation-duration: 3.2s; }
  body.paused .frame img { animation-play-state: paused; }
  @keyframes slide { from { transform: translateX(0); } to { transform: translateX(-100%); } }
  figcaption { padding: 9px 13px 11px; display: flex; flex-direction: column; gap: 3px; border-top: 1px solid #eef3f1; }
  figcaption strong { font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
  figcaption .num { flex: none; min-width: 26px; height: 26px; padding: 0 6px; border-radius: 999px; background: #0f3d33;
                    color: #fff; font-size: 12px; font-weight: 700; display: inline-flex; align-items: center;
                    justify-content: center; letter-spacing: .02em; }
  figcaption span { font-size: 11px; color: #6b7d78; letter-spacing: .02em; padding-left: 34px; }
  figcaption span.num { padding-left: 0; }
  .card:target { outline: 3px solid #0f3d33; outline-offset: 2px; }
  .note { margin: 26px 0 0; font-size: 13px; color: #5a6b67; }
</style>
</head>
<body>
<h1>Animaciones propias (16)</h1>
<p class="hint">Las que he dibujado para FitCasa, numeradas del 01 al 16. Dime el número y sé cuál es.</p>
<div class="bar">
  <button id="pause" aria-pressed="false">Pausar</button>
  <button id="slow" aria-pressed="false">Lento</button>
</div>
<main class="grid" id="grid">
$($cards -join "`n")
</main>
<p class="note">Las otras 20 vienen de LottieFiles y no las he tocado en esta tanda; si quieres revisarlas, las capturas reales de la app están en <code>D:\FitCasa\app\build\workout-shots</code>.</p>
<script>
  const body = document.body;
  document.getElementById('pause').addEventListener('click', (e) => {
    body.classList.toggle('paused');
    e.currentTarget.setAttribute('aria-pressed', body.classList.contains('paused'));
  });
  document.getElementById('slow').addEventListener('click', (e) => {
    body.classList.toggle('slow');
    e.currentTarget.setAttribute('aria-pressed', body.classList.contains('slow'));
  });
</script>
</body>
</html>
"@

$htmlPath = Join-Path $outDir 'index.html'
[System.IO.File]::WriteAllText($htmlPath, $html, (New-Object System.Text.UTF8Encoding($false)))
"html: {0:N1} MB  |  tarjetas: {1}" -f ((Get-Item $htmlPath).Length / 1MB), $cards.Count
$htmlPath
