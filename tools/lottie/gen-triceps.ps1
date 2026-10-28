# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# ---------- 1. FONDOS DE TRICEPS EN EL SUELO (silla) ----------
# Sentado en el borde de una silla, manos en el asiento, piernas extendidas con
# los talones en el suelo. El brazo gira sobre la mano (que no se mueve) y el
# cuerpo acompana el arco del hombro: baja y avanza un poco. Las piernas son
# hijas del cuerpo y se contra-rotan en rodilla y tobillo para no despegar el pie.
function DipsFloor() {
  $W = 900; $H = 640
  $handX = 300; $handY = 428
  $shX = 395; $shY = 390
  $hipX = 530; $hipY = 470
  $kneeX = 640; $kneeY = 505
  $ankX = 690; $ankY = 540
  $dx = 7; $dy = 35          # arco del hombro al girar el brazo 20 grados
  $fr = @(0, 16, 30, 46, 60)

  $ground = StaticLayer 'silla_suelo' 1 @(
    (Filled 'suelo' (Rect 450 556 780 22 11) $script:matDark),
    (Filled 'pata_tras' (Rect 185 486 20 140 8) $script:chairDk),
    (Filled 'pata_delantera' (Rect 302 486 20 140 8) $script:chairDk),
    (Filled 'respaldo' (Rect 166 330 24 178 10) $script:chairC),
    (Filled 'asiento' (Rect 240 433 164 26 10) $script:chairC),
    (Filled 'canto_asiento' (Rect 240 447 164 8 4) $script:chairDk)
  )

  # brazo: gira sobre la mano (+20 grados); el hombro recorre el arco del cuerpo
  $armShapes = @(
    (Limb 'brazo' $shX $shY 352 430 36 $script:skin),
    (Limb 'antebrazo' 352 430 $handX $handY 32 $script:skin),
    (Filled 'mano' (Ellipse $handX $handY 46 34) $script:skin)
  )
  $arm = ShapeLayer 'brazo' 2 $armShapes (RotKeys $fr @(0, 20, 20, 0, 0)) @($handX, $handY)

  # cuerpo: baja y avanza siguiendo el arco del hombro
  $body = MoveLayer 'cuerpo' 3 @(
    (Limb 'brazo_trasero' ($shX + 6) ($shY + 10) 306 438 30 $script:skinBk),
    (Limb 'tronco' $shX $shY $hipX $hipY 78 $script:shirt),
    (Filled 'cadera' (Rect ($hipX + 6) $hipY 118 76 34) $script:pants),
    (Filled 'cabello' (Ellipse ($shX - 6) ($shY - 56) 96 74) $script:hair),
    (Filled 'cara' (Ellipse ($shX + 16) ($shY - 30) 88 88) $script:skin),
    (Filled 'coleta' (Rect ($shX - 70) ($shY - 44) 78 26 13) $script:hair),
    (Filled 'ojo' (Ellipse ($shX + 44) ($shY - 24) 11 7) $script:hair)
  ) (MoveKeys $fr @(
      @(0, 0), @($dx, $dy), @($dx, $dy), @(0, 0), @(0, 0)
    )) @(0, 0)

  # piernas: hijas del cuerpo; rodilla y tobillo compensan la bajada
  $thigh = ChildLayer 'muslo' 4 3 @(
    (Limb 'muslo' $hipX $hipY $kneeX $kneeY 60 $script:pants)
  ) (RotKeys $fr @(0, -15.6, -15.6, 0, 0)) @($hipX, $hipY)
  $calf = ChildLayer 'pantorrilla' 5 4 @(
    (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 44 $script:pantsBk)
  ) (RotKeys $fr @(0, 15.6, 15.6, 0, 0)) @($kneeX, $kneeY)
  $foot = ChildLayer 'pie' 6 5 @(
    (Limb 'pie' $ankX $ankY ($ankX + 50) ($ankY + 4) 32 $script:shoe)
  ) (RotKeys $fr @(0, 0, 0, 0, 0)) @($ankX, $ankY)

  Write-Anim 'triceps_dips_floor' $W $H 30 @($ground, $arm, $body, $thigh, $calf, $foot)
}
DipsFloor

# ---------- 2. EXTENSION DE TRICEPS DE RODILLAS ----------
# De rodillas en el suelo, tronco horizontal y manos bajo los hombros con los
# codos doblados hacia atras. Al extender los codos el pecho sube; la pelvis y
# las rodillas no se mueven (el tronco gira sobre la cadera).
function KneelingTri() {
  $W = 1000; $H = 560
  $kneeX = 700; $kneeY = 488
  $hipX = 700; $hipY = 398
  $shX = 540; $shY = 400
  $handX = 505; $handY = 490
  $l1 = 82; $l2 = 101
  $fr = @(0, 16, 30, 46, 60)
  $lift = @(0, 9, 9, 0, 0)

  $mat = StaticLayer 'colchoneta' 1 @(
    (Filled 'colchoneta' (Rect 600 508 760 30 15) $script:mat),
    (Filled 'borde' (Rect 600 524 760 8 4) $script:matDark)
  )

  # IK con el codo hacia atras (signo -1): el codo queda por debajo del torso y
  # se lee el doblez; la mano queda clavada en la colchoneta.
  $el0 = Codo $shX $shY $handX $handY $l1 $l2 -1
  $thKeys = @(); $phiKeys = @()
  for ($i = 0; $i -lt $fr.Count; $i++) {
    $t = $lift[$i]
    $s = RotPt $shX $shY $hipX $hipY $t
    $e = Codo $s[0] $s[1] $handX $handY $l1 $l2 -1
    $theta = (AngDeg ($e[0] - $s[0]) ($e[1] - $s[1])) - (AngDeg ($el0[0] - $shX) ($el0[1] - $shY)) - $t
    $phi = (AngDeg ($handX - $e[0]) ($handY - $e[1])) - (AngDeg ($handX - $el0[0]) ($handY - $el0[1])) - $t - $theta
    $thKeys += [math]::Round($theta, 2)
    $phiKeys += [math]::Round($phi, 2)
  }

  # piernas y pelvis: fijas en el suelo (el giro del tronco no las mueve)
  $legs = StaticLayer 'piernas' 2 @(
    (Limb 'muslo' $hipX $hipY $kneeX $kneeY 66 $script:pants),
    (Filled 'rodilla' (Ellipse $kneeX $kneeY 70 70) $script:pants),
    (Limb 'pantorrilla' $kneeX $kneeY 830 486 46 $script:pantsBk),
    (Limb 'pie' 830 486 874 480 34 $script:shoe),
    (Limb 'muslo_lejos' ($hipX - 12) ($hipY + 4) ($kneeX - 12) ($kneeY + 2) 58 $script:pantsBk),
    (Limb 'pantorrilla_lejos' ($kneeX - 12) ($kneeY + 2) 816 492 42 $script:pantsBk),
    (Limb 'pie_lejos' 816 492 858 488 30 $script:shoe)
  )

  $torso = ShapeLayer 'tronco' 3 (@(
    (Limb 'brazo_lejos' ($shX + 6) ($shY - 4) ($el0[0] + 6) ($el0[1] - 6) 24 $script:skinBk),
    (Limb 'antebrazo_lejos' ($el0[0] + 6) ($el0[1] - 6) ($handX + 40) ($handY + 2) 22 $script:skinBk),
    (Filled 'mano_lejos' (Ellipse ($handX + 40) ($handY + 6) 36 22) $script:skinBk),
    (Limb 'tronco' $shX $shY $hipX $hipY 70 $script:shirt),
    (Filled 'cadera' (Rect ($hipX - 10) $hipY 112 70 32) $script:pants),
    (Limb 'cuello' ($shX - 30) ($shY - 2) ($shX + 4) ($shY + 2) 44 $script:skin),
    (Filled 'cabello' (Ellipse ($shX - 66) ($shY - 30) 90 68) $script:hair),
    (Filled 'cara' (Ellipse ($shX - 88) ($shY - 8) 84 84) $script:skin),
    (Filled 'ojo' (Ellipse ($shX - 114) ($shY + 2) 10 7) $script:hair)
  )) (RotKeys $fr $lift) @($hipX, $hipY)

  $upper = ChildLayer 'brazo' 4 3 @(
    (Limb 'brazo' $shX $shY $el0[0] $el0[1] 46 $script:skin),
    (Filled 'hombro' (Ellipse $shX $shY 48 48) $script:skin)
  ) (RotKeys $fr $thKeys) @($shX, $shY)

  $fore = ChildLayer 'antebrazo' 5 4 @(
    (Limb 'antebrazo' $el0[0] $el0[1] $handX $handY 40 $script:skin),
    (Filled 'codo' (Ellipse $el0[0] $el0[1] 38 38) $script:skin),
    (Filled 'mano' (Rect ($handX - 4) ($handY + 4) 56 20 10) $script:skin)
  ) (RotKeys $fr $phiKeys) @($el0[0], $el0[1])

  Write-Anim 'kneeling_triceps_extension' $W $H 30 @($mat, $legs, $torso, $upper, $fore)
}
KneelingTri

# ---------- 3. FLEXIONES LATERALES DE TRICEPS ----------
# Plancha de lado en diagonal: pies apilados en la colchoneta, mano de apoyo
# justo bajo el hombro y el otro brazo vertical hacia el techo. El cuerpo baja
# en bloque girando sobre los pies (el codo de apoyo se dobla con la mano
# clavada) y vuelve a subir.
function SidePushUp() {
  $W = 1000; $H = 560
  $toeX = 884; $toeY = 492
  $ankX = 844; $ankY = 468
  $kneeX = 760; $kneeY = 440
  $hipX = 690; $hipY = 412
  $shX = 470; $shY = 320
  $handX = 470; $handY = 492
  $l1 = 86; $l2 = 86
  $fr = @(0, 16, 30, 46, 60)
  $dip = @(0, -4, -4, 0, 0)

  $mat = StaticLayer 'colchoneta' 1 @(
    (Filled 'colchoneta' (Rect 600 516 780 26 13) $script:mat),
    (Filled 'borde' (Rect 600 530 780 7 3) $script:matDark)
  )

  $el0 = Codo $shX $shY $handX $handY $l1 $l2 -1
  $thKeys = @(); $phiKeys = @()
  for ($i = 0; $i -lt $fr.Count; $i++) {
    $t = $dip[$i]
    $s = RotPt $shX $shY $toeX $toeY $t
    $e = Codo $s[0] $s[1] $handX $handY $l1 $l2 -1
    $theta = (AngDeg ($e[0] - $s[0]) ($e[1] - $s[1])) - (AngDeg ($el0[0] - $shX) ($el0[1] - $shY)) - $t
    $phi = (AngDeg ($handX - $e[0]) ($handY - $e[1])) - (AngDeg ($handX - $el0[0]) ($handY - $el0[1])) - $t - $theta
    $thKeys += [math]::Round($theta, 2)
    $phiKeys += [math]::Round($phi, 2)
  }

  $body = ShapeLayer 'cuerpo' 2 @(
    (Limb 'muslo' $hipX $hipY $kneeX $kneeY 62 $script:pants),
    (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 46 $script:pantsBk),
    (Limb 'muslo_lejos' ($hipX + 14) ($hipY + 2) ($kneeX + 10) ($kneeY + 6) 56 $script:pantsBk),
    (Limb 'pantorrilla_lejos' ($kneeX + 10) ($kneeY + 6) ($ankX + 4) ($ankY + 4) 42 $script:pantsBk),
    (Limb 'pie' $ankX $ankY $toeX $toeY 34 $script:shoe),
    (Limb 'pie_lejos' ($ankX + 4) ($ankY + 4) ($toeX + 2) ($toeY + 2) 30 $script:shoe),
    (Limb 'tronco' $shX $shY $hipX $hipY 76 $script:shirt),
    (Filled 'cadera' (Rect ($hipX + 6) $hipY 116 74 34) $script:pants),
    (Limb 'brazo_libre' ($shX + 8) ($shY - 4) ($shX + 16) ($shY - 148) 34 $script:skin),
    (Filled 'mano_libre' (Ellipse ($shX + 18) ($shY - 160) 42 44) $script:skin),
    (Filled 'cabello' (Ellipse ($shX - 62) ($shY - 44) 92 76) $script:hair),
    (Filled 'cara' (Ellipse ($shX - 84) ($shY - 18) 88 88) $script:skin),
    (Filled 'ojo' (Ellipse ($shX - 112) ($shY - 12) 10 7) $script:hair)
  ) (RotKeys $fr $dip) @($toeX, $toeY)

  $upper = ChildLayer 'brazo_apoyo' 3 2 @(
    (Limb 'brazo' $shX $shY $el0[0] $el0[1] 38 $script:skin)
  ) (RotKeys $fr $thKeys) @($shX, $shY)

  $fore = ChildLayer 'antebrazo' 4 3 @(
    (Limb 'antebrazo' $el0[0] $el0[1] $handX $handY 32 $script:skin),
    (Filled 'mano' (Rect ($handX - 8) ($handY + 2) 60 22 11) $script:skin)
  ) (RotKeys $fr $phiKeys) @($el0[0], $el0[1])

  Write-Anim 'side_push_up' $W $H 30 @($mat, $body, $upper, $fore)
}
SidePushUp

# ---------- 4. FONDOS ELEVADOS DE TRICEPS (banco) ----------
# Palmas en el banco, piernas al frente con los talones en el suelo; el cuerpo
# baja flexionando los codos. Las piernas cuelgan del cuerpo y compensan en
# rodilla y tobillo para mantener el pie apoyado.
function BodyUp() {
  $W = 900; $H = 620
  $handX = 340; $handY = 427
  $shX = 430; $shY = 388
  $hipX = 570; $hipY = 455
  $kneeX = 700; $kneeY = 500
  $ankX = 780; $ankY = 540
  $dx = 8; $dy = 33
  $fr = @(0, 16, 30, 46, 60)

  $bench = StaticLayer 'banco' 1 @(
    (Filled 'suelo' (Rect 450 556 780 22 11) $script:matDark),
    (Filled 'pata_tras' (Rect 262 488 22 130 8) $script:chairDk),
    (Filled 'pata_delantera' (Rect 396 488 22 130 8) $script:chairDk),
    (Filled 'tablon' (Rect 330 440 210 26 10) $script:chairC),
    (Filled 'canto' (Rect 330 454 210 8 4) $script:chairDk)
  )

  $armShapes = @(
    (Limb 'brazo' $shX $shY 395 420 36 $script:skin),
    (Limb 'antebrazo' 395 420 $handX $handY 32 $script:skin),
    (Filled 'mano' (Ellipse $handX $handY 46 34) $script:skin)
  )
  $arm = ShapeLayer 'brazo' 2 $armShapes (RotKeys $fr @(0, 20, 20, 0, 0)) @($handX, $handY)

  $body = MoveLayer 'cuerpo' 3 @(
    (Limb 'brazo_trasero' ($shX + 6) ($shY + 10) ($handX + 8) ($handY - 6) 30 $script:skinBk),
    (Limb 'tronco' $shX $shY $hipX $hipY 76 $script:shirt),
    (Filled 'cadera' (Rect ($hipX + 6) $hipY 112 72 32) $script:pants),
    (Filled 'cabello' (Ellipse ($shX - 6) ($shY - 56) 94 72) $script:hair),
    (Filled 'cara' (Ellipse ($shX + 18) ($shY - 30) 86 86) $script:skin),
    (Filled 'coleta' (Rect ($shX - 68) ($shY - 40) 76 26 13) $script:hair),
    (Filled 'ojo' (Ellipse ($shX + 46) ($shY - 24) 11 7) $script:hair)
  ) (MoveKeys $fr @(
      @(0, 0), @($dx, $dy), @($dx, $dy), @(0, 0), @(0, 0)
    )) @(0, 0)

  $thigh = ChildLayer 'muslo' 4 3 @(
    (Limb 'muslo' $hipX $hipY $kneeX $kneeY 58 $script:pants)
  ) (RotKeys $fr @(0, -13.5, -13.5, 0, 0)) @($hipX, $hipY)
  $calf = ChildLayer 'pantorrilla' 5 4 @(
    (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 46 $script:pantsBk)
  ) (RotKeys $fr @(0, 13.5, 13.5, 0, 0)) @($kneeX, $kneeY)
  $foot = ChildLayer 'pie' 6 5 @(
    (Limb 'pie' $ankX $ankY ($ankX + 52) ($ankY + 4) 32 $script:shoe)
  ) (RotKeys $fr @(0, 0, 0, 0, 0)) @($ankX, $ankY)

  Write-Anim 'body_up' $W $H 30 @($bench, $arm, $body, $thigh, $calf, $foot)
}
BodyUp
