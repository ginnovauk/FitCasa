# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# Plancha isometrica de perfil. El cuerpo va recto desde el hombro hasta el
# tobillo, apoyado en los antebrazos; la mano queda clavada en la colchoneta.
# Es una postura estatica: el bucle no necesita movimiento, solo que la figura
# se lea bien.
$W = 1000; $H = 560

# Colchoneta (misma que las flexiones para que el set sea coherente).
$mat = StaticLayer 'colchoneta' 1 @(
  (Filled 'colchoneta' (Rect 600 508 760 30 15) $script:mat),
  (Filled 'borde' (Rect 600 524 760 8 4) $script:matDark)
)

# Pierna lejana, un poco mas atras para dar profundidad.
$legFar = StaticLayer 'pierna_lejana' 2 @(
  (Limb 'muslo_lejano' 620 438 780 450 58 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 780 450 900 460 44 $script:pantsBk),
  (Limb 'pie_lejano' 900 460 940 450 28 $script:shoe)
)

# Tronco, cadera y pierna cercana.
$body = StaticLayer 'cuerpo' 3 @(
  (Limb 'tronco' 380 410 620 430 82 $script:shirt),
  (Filled 'pelvis' (Rect 650 432 120 76 34) $script:pants),
  (Limb 'muslo' 620 432 780 444 62 $script:pants),
  (Limb 'pantorrilla' 780 444 900 454 46 $script:pantsBk),
  (Limb 'pie' 900 454 940 444 30 $script:shoe),
  (Limb 'cuello' 340 408 370 412 40 $script:skin),
  (Filled 'cabello' (Ellipse 292 382 92 68) $script:hair),
  (Filled 'cara' (Ellipse 310 404 82 82) $script:skin),
  (Filled 'nariz' (Ellipse 352 410 16 12) $script:skinBk),
  (Filled 'coleta' (Rect 250 400 64 22 11) $script:hair),
  (Filled 'ojo' (Ellipse 332 400 9 6) $script:hair)
)

# Brazo lejano, detras del cuerpo.
$armFar = StaticLayer 'brazo_lejano' 4 @(
  (Limb 'brazo_lejano' 378 416 378 476 34 $script:skinBk),
  (Limb 'antebrazo_lejano' 378 476 300 504 28 $script:skinBk),
  (Filled 'mano_lejana' (Ellipse 296 504 44 20) $script:skinBk)
)

# Brazo cercano, apoyado en la colchoneta.
$armNear = StaticLayer 'brazo' 5 @(
  (Limb 'brazo' 380 410 380 470 38 $script:skin),
  (Limb 'antebrazo' 380 470 300 500 32 $script:skin),
  (Filled 'mano' (Ellipse 296 500 48 22) $script:skin)
)

Write-Anim 'plank' $W $H 30 @($mat, $legFar, $body, $armFar, $armNear)
