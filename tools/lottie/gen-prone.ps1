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

$W = 1000; $H = 560
$fr = @(0, 15, 30, 45, 60)

# Plancha alta comun: mano en la colchoneta, hombro un brazo por encima.
$handX = 380; $handY = 470
$shX = 380; $shY = 320
$hipX = 560; $hipY = 340

$mat = StaticLayer 'colchoneta' 1 @(
  (Filled 'colchoneta' (Rect 600 508 760 30 15) $script:mat),
  (Filled 'borde' (Rect 600 524 760 8 4) $script:matDark)
)

function Head([double]$x, [double]$y) {
  @(
    (Filled 'cabello' (Ellipse ($x - 66) ($y - 30) 90 68) $script:hair),
    (Filled 'cara' (Ellipse ($x - 86) ($y - 8) 82 82) $script:skin),
    (Filled 'coleta' (Rect ($x - 26) ($y - 24) 70 24 12) $script:hair),
    (Filled 'ojo' (Ellipse ($x - 110) ($y - 4) 10 7) $script:hair)
  )
}

# ---------- 1. ESCALADORES DE MONTAÑA ----------
# Plancha alta con los brazos rectos. La rodilla entra hacia el pecho (el muslo
# gira sobre la cadera y la pantorrilla pliega para que el pie quede recogido);
# las piernas se alternan. La cadera va lo bastante alta para que el muslo no
# atraviese la colchoneta en el punto bajo del recorrido.
$climb = StaticLayer 'tronco_brazo' 2 (@(
  (Limb 'brazo_trasero' ($shX + 6) ($shY + 10) ($handX + 6) ($handY - 4) 32 $script:skinBk),
  (Limb 'brazo' $shX $shY ($handX + 4) $handY 36 $script:skin),
  (Filled 'mano' (Ellipse ($handX + 4) $handY 48 36) $script:skin),
  (Limb 'tronco' $shX $shY $hipX $hipY 82 $script:shirt),
  (Filled 'cadera' (Rect ($hipX - 4) $hipY 132 80 36) $script:pants)
) + (Head $shX $shY))

# lejos (detras del cuerpo): empieza extendida
$far = LegChain 'lejos' 5 @($hipX, $hipY) 132 112 $fr `
  @(22, 70, 150, 70, 22) @(35, 0, 44, 0, 35) 56 42 42 30 12 $script:pantsBk $script:pantsBk $script:shoe

# cerca (delante): empieza recogida
$near = LegChain 'cerca' 3 @($hipX, $hipY) 138 118 $fr `
  @(150, 70, 22, 70, 150) @(44, 0, 35, 0, 44) 62 46 44 32 12 $script:pants $script:pants $script:shoe

Write-Anim 'mountain_climbers' $W $H 30 @($mat, $far[0], $far[1], $climb, $near[0], $near[1])

# ---------- 2. ELEVACION DE CODOS BOCA ABAJO ----------
# Boca abajo con los antebrazos en la colchoneta y las manos delante de la
# cabeza: al extender los codos el pecho se separa del suelo girando sobre la
# cadera y los codos suben. La mano queda clavada, asi que el codo sube por IK.
$hip2X = 660; $hip2Y = 452
$sh2X = 480; $sh2Y = 446
$hand2X = 330; $hand2Y = 490
$l1b = 47; $l2b = 140
$elbow0 = Codo $sh2X $sh2Y $hand2X $hand2Y $l1b $l2b 1

$lift = @(0, 18, 18, 0, 0)
$thKeys = @(); $phiKeys = @()
for ($i = 0; $i -lt $fr.Count; $i++) {
  $t = $lift[$i]
  $s = RotPt $sh2X $sh2Y $hip2X $hip2Y $t
  $e = Codo $s[0] $s[1] $hand2X $hand2Y $l1b $l2b 1
  $theta = (AngDeg ($e[0] - $s[0]) ($e[1] - $s[1])) - (AngDeg ($elbow0[0] - $sh2X) ($elbow0[1] - $sh2Y)) - $t
  $phi = (AngDeg ($hand2X - $e[0]) ($hand2Y - $e[1])) - (AngDeg ($hand2X - $elbow0[0]) ($hand2Y - $elbow0[1])) - $t - $theta
  $thKeys += [math]::Round($theta, 2)
  $phiKeys += [math]::Round($phi, 2)
}

$torso2 = ShapeLayer 'tronco' 2 (@(
  (Limb 'brazo_lejos' ($sh2X + 8) ($sh2Y + 12) ($hand2X + 26) ($hand2Y + 2) 30 $script:skinBk),
  (Filled 'mano_lejos' (Rect ($hand2X + 26) ($hand2Y + 1) 50 18 9) $script:skinBk),
  (Limb 'tronco' $sh2X $sh2Y $hip2X $hip2Y 82 $script:shirt),
  (Filled 'cadera' (Rect ($hip2X - 6) $hip2Y 126 78 34) $script:pants)
) + (Head $sh2X $sh2Y)) (RotKeys $fr $lift) @($hip2X, $hip2Y)

$legs2 = StaticLayer 'piernas' 3 @(
  (Limb 'muslo' ($hip2X + 40) ($hip2Y + 10) 840 470 62 $script:pants),
  (Limb 'pantorrilla' 840 470 946 476 46 $script:pantsBk),
  (Limb 'pie' 946 476 990 488 32 $script:shoe),
  (Limb 'muslo_lejos' ($hip2X + 40) ($hip2Y + 22) 838 486 56 $script:pantsBk),
  (Limb 'pantorrilla_lejos' 838 486 940 492 42 $script:pantsBk),
  (Limb 'pie_lejos' 940 492 982 500 30 $script:shoe)
)

$upper2 = ChildLayer 'brazo' 4 2 @(
  (Limb 'brazo' $sh2X $sh2Y $elbow0[0] $elbow0[1] 34 $script:skin)
) (RotKeys $fr $thKeys) @($sh2X, $sh2Y)

$fore2 = ChildLayer 'antebrazo' 5 4 @(
  (Limb 'antebrazo' $elbow0[0] $elbow0[1] $hand2X $hand2Y 30 $script:skin),
  (Filled 'mano' (Rect ($hand2X - 6) ($hand2Y + 2) 54 20 10) $script:skin)
) (RotKeys $fr $phiKeys) @($elbow0[0], $elbow0[1])

Write-Anim 'prone_elbow_lift' $W $H 30 @($mat, $legs2, $torso2, $upper2, $fore2)

# ---------- 3. FLEXIONES ESCAPULARES ----------
# Plancha alta con los codos rectos. El pecho baja recto entre las manos
# (escapulas separadas) y vuelve a subir sin doblar los codos: el tronco se
# desplaza en vertical y las piernas, los pies y las manos no se mueven.
$scapDrop = @(@(0, 0), @(0, 52), @(0, 52), @(0, 0), @(0, 0))
$armLen = 116

$legs3 = StaticLayer 'piernas' 2 @(
  (Limb 'muslo' ($hipX + 40) ($hipY + 4) 712 412 62 $script:pants),
  (Limb 'pantorrilla' 712 412 830 446 46 $script:pantsBk),
  (Limb 'pie' 830 446 872 470 32 $script:shoe),
  (Limb 'muslo_lejos' ($hipX + 40) ($hipY + 14) 710 424 56 $script:pantsBk),
  (Limb 'pantorrilla_lejos' 710 424 824 456 42 $script:pantsBk),
  (Limb 'pie_lejos' 824 456 864 478 30 $script:shoe)
)

# brazo del lado lejano: se queda clavado con la mano en la colchoneta
$farArm3 = StaticLayer 'brazo_lejos' 3 @(
  (Limb 'brazo_lejos' ($shX + 10) ($shY + 12) ($handX + 12) ($handY - 6) 28 $script:skinBk),
  (Filled 'mano_lejos' (Ellipse ($handX + 14) ($handY) 44 30) $script:skinBk)
)

$torso3 = MoveLayer 'tronco' 5 ((@(
  (Limb 'tronco' $shX $shY $hipX $hipY 82 $script:shirt),
  (Filled 'cadera' (Rect ($hipX - 4) $hipY 132 80 36) $script:pants)
) + (Head $shX $shY))) (MoveKeys $fr $scapDrop) @(0, 0)

$brazo3 = StaticLayer 'brazo' 4 @(
  (Limb 'brazo' ($handX) ($handY + 2) $handX ($handY - $armLen) 36 $script:skin),
  (Filled 'mano' (Ellipse $handX ($handY + 2) 48 34) $script:skin)
)

# Flecha guia sobre la espalda: marca que el pecho baja y sube (el recorrido es
# corto y sin guia no se entiende).
$guiC12 = @(0.44, 0.56, 0.51, 1)
$gx = 470; $gy = 246; $gh = 44
$guia12 = StaticLayer 'guia' 2 @(
  (Limb 'eje' $gx ($gy - $gh) $gx ($gy + $gh) 10 $guiC12),
  (Limb 'punta_sup_a' $gx ($gy - $gh - 4) ($gx - 19) ($gy - $gh + 18) 10 $guiC12),
  (Limb 'punta_sup_b' $gx ($gy - $gh - 4) ($gx + 19) ($gy - $gh + 18) 10 $guiC12),
  (Limb 'punta_inf_a' $gx ($gy + $gh + 4) ($gx - 19) ($gy + $gh - 18) 10 $guiC12),
  (Limb 'punta_inf_b' $gx ($gy + $gh + 4) ($gx + 19) ($gy + $gh - 18) 10 $guiC12)
)

Write-Anim 'scapula_push_up' $W $H 30 @($mat, $guia12, $legs3, $farArm3, $brazo3, $torso3)
