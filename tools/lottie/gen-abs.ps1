# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

function ChildLayer([string]$name, [int]$ind, [int]$parent, [array]$shapeGroups, $r, [array]$pivot) {
  $l = ShapeLayer $name $ind $shapeGroups $r $pivot
  $l['pr'] = $parent
  $l
}

# ---------- 1. ABDOMINALES BICICLETA ----------
# Boca arriba con el tronco curvado (hombros fuera de la colchoneta) y las
# piernas pedaleando: una rodilla sube hacia el pecho mientras la otra se
# extiende. El brazo cerca va al encuentro de la rodilla contraria.
$W = 1000; $H = 560
$hipX = 620; $hipY = 452
$shX = 456; $shY = 392
$fr = @(0, 15, 30, 45, 60)

$matBic = StaticLayer 'colchoneta' 1 @(
  (Filled 'colchoneta' (Rect 600 512 760 26 13) $script:mat),
  (Filled 'borde' (Rect 600 526 760 7 3) $script:matDark)
)

$torsoRot = @(-6, 2, -6, 2, -6)

$torso = ShapeLayer 'tronco' 4 @(
    (Limb 'cintura' $hipX $hipY $shX $shY 74 $script:shirt),
    (Filled 'cadera' (Rect ($hipX + 4) $hipY 118 74 32) $script:pants),
    (Limb 'cuello' ($shX - 18) ($shY + 2) ($shX + 10) ($shY + 8) 46 $script:skin),
    (Filled 'cabello' (Ellipse ($shX - 84) ($shY - 50) 94 90) $script:hair),
    (Filled 'cara' (Ellipse ($shX - 64) ($shY - 40) 86 86) $script:skin),
    (Filled 'ojo' (Ellipse ($shX - 88) ($shY - 58) 10 7) $script:hair)
  ) (RotKeys $fr $torsoRot) @($hipX, $hipY)

# brazo cerca: codo abierto, la mano busca la rodilla contraria
$arm = ChildLayer 'brazo' 5 4 @(
    (Limb 'brazo' $shX $shY ($shX + 34) ($shY - 72) 36 $script:skin),
    (Limb 'antebrazo' ($shX + 34) ($shY - 72) ($shX + 104) ($shY - 88) 30 $script:skin),
    (Filled 'mano' (Ellipse ($shX + 112) ($shY - 90) 40 32) $script:skin)
  ) (RotKeys $fr @(-16, 10, -16, 10, -16)) @($shX, $shY)

# pierna cerca: f0 rodilla al pecho / f30 extendida
$nearBic = LegChain 'cerca' 6 @($hipX, $hipY) 148 131 @(0, 15, 30, 45, 60) `
  @(-85, -48, -10, -48, -85) @(51, 50, 0, 50, 51) 62 46 44 32 12 $script:pants $script:pants $script:shoe

# pierna lejos: fase opuesta
$farBic = LegChain 'lejos' 8 @(($hipX + 12), ($hipY + 14)) 138 124 @(0, 15, 30, 45, 60) `
  @(-10, -48, -85, -48, -10) @(0, 50, 51, 50, 0) 56 42 42 30 12 $script:pantsBk $script:pantsBk $script:shoe

Write-Anim 'bicycle_crunches' $W $H 30 @($matBic, $farBic[0], $farBic[1], $torso, $arm, $nearBic[0], $nearBic[1])

# ---------- 2. PLANCHA INVERSA CON ELEVACIÓN ----------
# Plancha inversa (mesa): cuerpo recto en diagonal, manos bajo los hombros y
# talones en la colchoneta. Se mantiene la postura y se eleva una pierna recta
# girando el muslo sobre la cadera.
$handX = 470; $handY = 490
$shX2 = 400; $shY2 = 348
$hipX2 = 600; $hipY2 = 420
$kneeX = 700; $kneeY = 456
$ankX = 800; $ankY = 492

$mat2 = StaticLayer 'colchoneta' 1 @(
  (Filled 'colchoneta' (Rect 600 508 760 30 15) $script:mat),
  (Filled 'borde' (Rect 600 524 760 8 4) $script:matDark)
)

$torsoR = StaticLayer 'tronco' 2 @(
    (Limb 'brazo_trasero' ($handX + 12) ($handY + 6) ($shX2 + 12) ($shY2 + 8) 26 $script:skinBk),
    (Filled 'mano_trasera' (Ellipse ($handX + 12) ($handY + 6) 40 30) $script:skinBk),
    (Limb 'cuello' ($shX2 - 70) ($shY2 - 2) $shX2 $shY2 42 $script:skin),
    (Limb 'tronco' $shX2 $shY2 $hipX2 $hipY2 74 $script:shirt),
    (Filled 'cadera' (Rect ($hipX2 + 6) $hipY2 112 72 32) $script:pants),
    (Limb 'brazo' $shX2 $shY2 $handX $handY 32 $script:skin),
    (Filled 'mano' (Rect ($handX + 8) ($handY + 6) 58 22 11) $script:skin),
    (Filled 'cabello' (Ellipse ($shX2 - 56) ($shY2 - 26) 92 70) $script:hair),
    (Filled 'cara' (Ellipse ($shX2 - 74) ($shY2 - 2) 86 86) $script:skin),
    (Filled 'ojo' (Ellipse ($shX2 - 96) ($shY2 - 6) 10 7) $script:hair)
  )

# pierna lejos: apoyada en la colchoneta
$thighFR = StaticLayer 'muslo_lejos' 3 @(
    (Limb 'muslo' ($hipX2 + 16) ($hipY2 + 10) ($kneeX + 12) ($kneeY + 9) 54 $script:pantsBk)
  )
$calfFR = StaticLayer 'pantorrilla_lejos' 4 @(
    (Limb 'pantorrilla' ($kneeX + 12) ($kneeY + 9) ($ankX + 12) ($ankY + 8) 40 $script:pantsBk),
    (Limb 'pie' ($ankX + 12) ($ankY + 8) ($ankX + 57) ($ankY + 12) 28 $script:shoe)
  )

# pierna cerca: arriba se eleva extendida (pantorrilla hereda el giro del muslo)
$thighNR = ShapeLayer 'muslo_cerca' 5 @(
    (Limb 'muslo' $hipX2 $hipY2 $kneeX $kneeY 60 $script:pants)
  ) (RotKeys $fr @(0, 0, -52, 0, 0)) @($hipX2, $hipY2)
$calfNR = ChildLayer 'pantorrilla_cerca' 6 5 @(
    (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 44 $script:pants),
    (Limb 'pie' $ankX $ankY ($ankX + 45) ($ankY + 4) 30 $script:shoe)
  ) (RotKeys $fr @(0, 0, 0, 0, 0)) @($kneeX, $kneeY)

Write-Anim 'reverse_plank_leg_lift' $W $H 30 @($mat2, $torsoR, $thighFR, $calfFR, $thighNR, $calfNR)
