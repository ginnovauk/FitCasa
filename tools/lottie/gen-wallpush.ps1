# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# Flexiones en la pared: cuerpo recto inclinado con un paso atras, giro sobre
# los pies al acercar el pecho. Brazo de dos segmentos encadenados:
#   cuerpo (giro -6 sobre el tobillo) <- brazo superior (-12 sobre el hombro)
#   <- antebrazo (+61 sobre el codo), que deja la mano clavada en la pared.
# La solucion de IK esta calculada para que la mano no se despegue.
$W = 720; $H = 920

$ankX = 560; $ankY = 830     # pivot: pies
$shX = 345; $shY = 462       # hombro
$elX = 263; $elY = 458       # codo
$hdX = 184; $hdY = 438       # mano en la pared (cara de la pared en x=170)

$fr = @(0, 16, 30, 46, 60)

$scene = StaticLayer 'pared_suelo' 1 @(
  (Filled 'panel' (Rect 140 470 60 760 8) $script:wallC),
  (Filled 'canto' (Rect 175 470 10 760 4) $script:wallDk),
  (Filled 'suelo' (Rect 440 864 540 24 12) $script:matDark),
  (Filled 'colchoneta' (Rect 470 846 460 20 10) $script:mat)
)

# brazo superior lejano (paralelo al cercano: mismas claves funcionan)
$uArmF = ChildLayer 'brazo_sup_lejano' 3 2 @(
  (Limb 'brazo_sup' ($shX - 10) ($shY + 12) ($elX - 10) ($elY + 12) 32 $script:skinBk)
) (RotKeys $fr @(0, -12, -12, 0, 0)) @(($shX - 10), ($shY + 12))

# cuerpo: gira sobre los pies (antic horario = hacia la pared)
$body = ShapeLayer 'cuerpo' 2 @(
  (Limb 'tronco' $shX $shY 442 628 76 $script:shirt),
  (Filled 'pelvis' (Rect 448 628 100 72 32) $script:pants),
  (Limb 'muslo' 442 628 506 738 56 $script:pants),
  (Limb 'pantorrilla' 506 738 $ankX $ankY 44 $script:pantsBk),
  (Limb 'pie' $ankX 838 505 848 32 $script:shoe),
  (Filled 'cabello' (Ellipse 352 380 92 70) $script:hair),
  (Filled 'cara' (Ellipse 330 406 86 86) $script:skin),
  (Filled 'coleta' (Rect 400 396 74 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 304 412 10 7) $script:hair)
) (RotKeys $fr @(0, -6, -6, 0, 0)) @($ankX, $ankY)

# brazo superior cercano
$uArm = ChildLayer 'brazo_sup' 4 2 @(
  (Limb 'brazo_sup' $shX $shY $elX $elY 36 $script:skin)
) (RotKeys $fr @(0, -12, -12, 0, 0)) @($shX, $shY)

# antebrazo lejano: la mano se mantiene en la pared
$foreF = ChildLayer 'antebrazo_lejano' 5 3 @(
  (Limb 'antebrazo' ($elX - 10) ($elY + 12) ($hdX - 10) ($hdY + 12) 28 $script:skinBk),
  (Filled 'mano_lejana' (Ellipse ($hdX - 10) ($hdY + 12) 42 38) $script:skinBk)
) (RotKeys $fr @(0, 61, 61, 0, 0)) @(($elX - 10), ($elY + 12))

# antebrazo cercano: mano clavada en la pared
$fore = ChildLayer 'antebrazo' 6 4 @(
  (Limb 'antebrazo' $elX $elY $hdX $hdY 30 $script:skin),
  (Filled 'mano' (Ellipse $hdX $hdY 46 42) $script:skin)
) (RotKeys $fr @(0, 61, 61, 0, 0)) @($elX, $elY)

Write-Anim 'wall_push_ups' $W $H 30 @($scene, $uArmF, $body, $uArm, $foreF, $fore)
