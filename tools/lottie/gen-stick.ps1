# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# Palo de madera: marron oscuro con un canto mas claro para que se lea
# sobre el lienzo claro y sobre la ropa.
$script:stickC  = Col 0.52 0.35 0.20
$script:stickHi = Col 0.74 0.55 0.31

# Rotacion lineal (sin easing) para giros continuos de 360 grados.
function SpinKeys([array]$frames, [array]$vals) {
  $keys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) { $keys += @{ t = $frames[$i]; s = @($vals[$i]) } }
  @{ a = 1; k = $keys }
}

# Escala animada por eje. Se usa para simular el giro del tronco: al girar de
# frente, los hombros se estrechan en pantalla (escorzo) sin que la cadera se
# mueva, algo que una rotacion 2D no puede expresar.
function ScaleKeys([array]$frames, [array]$xs, [array]$ys) {
  $keys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) {
    $k = @{ t = $frames[$i]; s = @($xs[$i], $ys[$i], 100) }
    if ($i -lt $frames.Count - 1) {
      $k.i = @{ x = @(0.42, 0.42, 0.42); y = @(1, 1, 1) }
      $k.o = @{ x = @(0.58, 0.58, 0.58); y = @(0, 0, 0) }
    }
    $keys += $k
  }
  @{ a = 1; k = $keys }
}

# Palo de un solo tramo. Limb ya devuelve un grupo con relleno.
function Stick([double]$x1, [double]$y1, [double]$x2, [double]$y2, [double]$th = 12) {
  Limb 'palo' $x1 $y1 $x2 $y2 $th $script:stickC
}

function Hand([double]$x, [double]$y, [array]$color, [double]$w = 36) {
  Filled 'mano' (Ellipse $x $y $w ($w * 5 / 6)) $color
}

# El codo se elige siempre hacia fuera del cuerpo (menor x en el brazo
# izquierdo, mayor x en el derecho). Con un signo fijo, al subir los brazos por
# encima de la cabeza el codo se cruzaba por delante del pecho.
function ElbowPick([double]$sx, [double]$sy, [double]$hx, [double]$hy,
                   [double]$l1, [double]$l2, [string]$side, [string]$mode = 'out') {
  $a = Codo $sx $sy $hx $hy $l1 $l2 1
  $b = Codo $sx $sy $hx $hy $l1 $l2 -1
  if ($mode -eq 'down') {
    # Sostener el palo delante: el codo cae por debajo de la linea hombro-mano.
    if ($a[1] -gt $b[1]) { return $a } else { return $b }
  }
  if ($side -eq 'left') {
    if ($a[0] -lt $b[0]) { return $a } else { return $b }
  }
  if ($a[0] -gt $b[0]) { return $a } else { return $b }
}

# Cadena hombro -> codo -> mano con IK. Devuelve dos capas: brazo y antebrazo
# (la mano va dentro del antebrazo). Las manos siguen la lista $hands.
function ArmIK([string]$name, [int]$ind, [int]$parent, [array]$shoulder, [array]$baseHand,
                [array]$hands, [array]$frames, [double]$l1, [double]$l2,
                [array]$upperColor, [array]$foreColor, [string]$side,
                [string]$elbowMode = 'out') {
  $baseElbow = ElbowPick $shoulder[0] $shoulder[1] $baseHand[0] $baseHand[1] $l1 $l2 $side $elbowMode
  $up0 = AngDeg ($baseElbow[0] - $shoulder[0]) ($baseElbow[1] - $shoulder[1])
  $fore0 = AngDeg ($baseHand[0] - $baseElbow[0]) ($baseHand[1] - $baseElbow[1])
  $upKeys = @(); $foreKeys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) {
    $h = $hands[$i]
    $e = ElbowPick $shoulder[0] $shoulder[1] $h[0] $h[1] $l1 $l2 $side $elbowMode
    $up = AngDeg ($e[0] - $shoulder[0]) ($e[1] - $shoulder[1])
    $fore = AngDeg ($h[0] - $e[0]) ($h[1] - $e[1])
    $upD = $up - $up0
    $foreD = ($fore - $fore0) - $upD
    $upKeys += [math]::Round($upD, 2)
    $foreKeys += [math]::Round($foreD, 2)
  }

  $upper = ShapeLayer "$($name)_sup" $ind @(
    (Limb 'brazo' $shoulder[0] $shoulder[1] $baseElbow[0] $baseElbow[1] 34 $upperColor)
  ) (RotKeys $frames $upKeys) @($shoulder[0], $shoulder[1])
  if ($parent -gt 0) { $upper['pr'] = $parent }

  $fore = ChildLayer "$($name)_ant" ($ind + 1) $ind @(
    (Limb 'antebrazo' $baseElbow[0] $baseElbow[1] $baseHand[0] $baseHand[1] 30 $foreColor),
    (Hand $baseHand[0] $baseHand[1] $foreColor)
  ) (RotKeys $frames $foreKeys) @($baseElbow[0], $baseElbow[1])
  , @($upper, $fore)
}

# Suelo comun a las animaciones de pie.
function Ground([int]$ind) {
  StaticLayer 'suelo' $ind @(
    (Filled 'suelo' (Rect 360 864 640 26 13) $script:matDark),
    (Filled 'colchoneta' (Rect 360 846 590 20 10) $script:mat)
  )
}

# Piernas de frente, con el peso repartido.
function FrontLegs([double]$hipY = 610) {
  @(
    (Limb 'muslo_lejano' 338 $hipY 322 738 56 $script:pantsBk),
    (Limb 'pantorrilla_lejana' 322 738 316 846 42 $script:pantsBk),
    (Limb 'pie_lejano' 316 846 366 856 30 $script:shoe),
    (Limb 'muslo' 382 $hipY 398 738 60 $script:pants),
    (Limb 'pantorrilla' 398 738 404 846 44 $script:pants),
    (Limb 'pie' 404 846 454 856 32 $script:shoe)
  )
}

# Tronco y cabeza de frente (sin brazos).
function FrontTorso {
  @(
    (Limb 'tronco' 360 420 360 600 132 $script:shirt),
    (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants),
    (Limb 'cuello' 360 410 360 388 46 $script:skin),
    (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
    (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
    (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
    (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
  )
}

# ---------- 1. PASES DE PALO SOBRE LA CABEZA ----------
# De pie, el palo sube desde los muslos hasta por encima de la cabeza y vuelve.
# Los brazos se resuelven con IK: las manos quedan en los extremos del palo.
$fr = @(0, 12, 24, 36, 48, 60)
$hsY = @(560, 480, 400, 310, 400, 560)
$handL = @(); $handR = @()
for ($i = 0; $i -lt $fr.Count; $i++) {
  $handL += , @(250, $hsY[$i])
  $handR += , @(470, $hsY[$i])
}

$body1 = StaticLayer 'cuerpo' 2 ((FrontLegs) + (FrontTorso))
$stick1 = MoveLayer 'palo' 3 @(
  (Stick 250 560 470 560 12)
) (MoveKeys $fr @(
    @(0, 0), @(0, -80), @(0, -160), @(0, -250), @(0, -160), @(0, 0)
  )) @(0, 0)
$armL1 = ArmIK 'brazo_izq' 4 0 @(296, 432) @(250, 560) $handL $fr 72 72 $script:skin $script:skin 'left'
$armR1 = ArmIK 'brazo_der' 6 0 @(424, 432) @(470, 560) $handR $fr 72 72 $script:skin $script:skin 'right'

Write-Anim 'stick_shoulder_pass' 720 920 30 @(
  (Ground 1), $body1, $stick1, $armL1[0], $armL1[1], $armR1[0], $armR1[1]
)

# ---------- 2. GIROS DE TRONCO CON PALO ----------
# Palo sobre los hombros; el tronco gira a un lado y a otro mientras las
# piernas y la cadera se quedan quietas. El escorzo del giro se dibuja
# estrechando el grupo en horizontal, no rotandolo (eso seria una inclinacion).
$fr2 = @(0, 15, 30, 45, 60)
$root2Shapes = @(
  (Limb 'tronco' 360 420 360 600 132 $script:shirt),
  (Stick 248 420 472 420 12),
  (Limb 'brazo_izq' 296 432 266 470 32 $script:skin),
  (Limb 'antebrazo_izq' 266 470 248 420 28 $script:skin),
  (Hand 248 420 $script:skin),
  (Limb 'brazo_der' 424 432 454 470 34 $script:skin),
  (Limb 'antebrazo_der' 454 470 472 420 30 $script:skin),
  (Hand 472 420 $script:skin),
  (Limb 'cuello' 360 410 360 388 46 $script:skin),
  (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
  (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
  (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
)
$root2 = ShapeLayer 'tronco' 3 $root2Shapes (RotKeys $fr2 @(0, 5, 0, -5, 0)) @(360, 610)
$root2.ks.s = ScaleKeys $fr2 @(100, 62, 100, 62, 100) @(100, 100, 100, 100, 100)
$body2 = StaticLayer 'cuerpo' 2 @(
  (Limb 'muslo_lejano' 338 610 322 738 56 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 322 738 316 846 42 $script:pantsBk),
  (Limb 'pie_lejano' 316 846 366 856 30 $script:shoe),
  (Limb 'muslo' 382 610 398 738 60 $script:pants),
  (Limb 'pantorrilla' 398 738 404 846 44 $script:pants),
  (Limb 'pie' 404 846 454 856 32 $script:shoe),
  (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants)
)

Write-Anim 'stick_torso_twist' 720 920 30 @((Ground 1), $body2, $root2)

# ---------- 3. EMPUJE VERTICAL CON PALO ----------
# Palo a la altura del pecho; los brazos lo empujan por encima de la cabeza y
# lo bajan con control.
$fr3 = @(0, 15, 30, 45, 60)
$pressY = @(480, 400, 310, 400, 480)
$pressL = @(); $pressR = @()
for ($i = 0; $i -lt $fr3.Count; $i++) {
  $pressL += , @(250, $pressY[$i])
  $pressR += , @(470, $pressY[$i])
}
$body3 = StaticLayer 'cuerpo' 2 ((FrontLegs) + (FrontTorso))
$stick3 = MoveLayer 'palo' 3 @(
  (Stick 250 480 470 480 12)
) (MoveKeys $fr3 @(
    @(0, 0), @(0, -80), @(0, -170), @(0, -80), @(0, 0)
  )) @(0, 0)
$armL3 = ArmIK 'brazo_izq' 4 0 @(296, 432) @(250, 480) $pressL $fr3 70 70 $script:skin $script:skin 'left'
$armR3 = ArmIK 'brazo_der' 6 0 @(424, 432) @(470, 480) $pressR $fr3 70 70 $script:skin $script:skin 'right'

Write-Anim 'stick_overhead_press' 720 920 30 @(
  (Ground 1), $body3, $stick3, $armL3[0], $armL3[1], $armR3[0], $armR3[1]
)

# ---------- 4. INCLINACION LATERAL CON PALO ----------
# Palo por encima de la cabeza; el tronco se inclina a un lado y al otro
# mientras las piernas y la cadera no se mueven.
$fr4 = @(0, 15, 30, 45, 60)
$root4Shapes = @(
  (Limb 'tronco' 360 420 360 600 132 $script:shirt),
  (Limb 'brazo_izq' 296 432 270 370 32 $script:skin),
  (Limb 'antebrazo_izq' 270 370 248 300 28 $script:skin),
  (Hand 248 300 $script:skin),
  (Limb 'brazo_der' 424 432 450 370 34 $script:skin),
  (Limb 'antebrazo_der' 450 370 472 300 30 $script:skin),
  (Hand 472 300 $script:skin),
  (Stick 248 300 472 300 12),
  (Limb 'cuello' 360 410 360 388 46 $script:skin),
  (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
  (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
  (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
)
$root4 = ShapeLayer 'tronco' 3 $root4Shapes (RotKeys $fr4 @(0, -20, 0, 20, 0)) @(360, 610)
$body4 = StaticLayer 'cuerpo' 2 @(
  (Limb 'muslo_lejano' 338 610 322 738 56 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 322 738 316 846 42 $script:pantsBk),
  (Limb 'pie_lejano' 316 846 366 856 30 $script:shoe),
  (Limb 'muslo' 382 610 398 738 60 $script:pants),
  (Limb 'pantorrilla' 398 738 404 846 44 $script:pants),
  (Limb 'pie' 404 846 454 856 32 $script:shoe),
  (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants)
)

Write-Anim 'stick_side_bend' 720 920 30 @((Ground 1), $body4, $root4)

# ---------- 5. BUENOS DIAS CON PALO ----------
# De perfil: palo cruzado sobre los omoplatos; la cadera va atras, las rodillas
# se flexionan y el tronco baja con la espalda recta. El palo sobresale por
# delante de la cara y por detras de la espalda para que se lea como una barra
# y no como un punto; solo se dibuja el brazo cercano, con la mano sobre el
# extremo delantero.
$fr5 = @(0, 15, 30, 45, 60)
$root5Shapes = @(
  (Limb 'brazo_lejano' 376 424 340 450 26 $script:skinBk),
  (Limb 'tronco' 380 420 380 600 104 $script:shirt),
  (Stick 298 450 478 424 15),
  (Limb 'brazo' 382 428 404 458 30 $script:skin),
  (Limb 'antebrazo' 404 458 468 430 26 $script:skin),
  (Hand 470 429 $script:skin 30),
  (Limb 'cuello' 380 410 390 388 44 $script:skin),
  (Filled 'cabello' (Ellipse 390 330 100 78) $script:hair),
  (Filled 'cara' (Ellipse 410 360 90 90) $script:skin),
  (Filled 'nariz' (Ellipse 458 366 14 12) $script:skin),
  (Filled 'coleta' (Rect 330 350 72 24 12) $script:hair),
  (Filled 'ojo' (Ellipse 436 360 9 6) $script:hair)
)
$hips5 = MoveLayer 'cadera' 5 @(
  (Filled 'pelvis' (Rect 386 612 116 76 34) $script:pants)
) (MoveKeys $fr5 @(
    @(0, 0), @(-8, 0), @(-22, 0), @(-8, 0), @(0, 0)
  )) @(0, 0)
$root5 = ShapeLayer 'tronco' 6 $root5Shapes (RotKeys $fr5 @(0, 20, 55, 20, 0)) @(380, 610)
$root5['pr'] = 5

# Piernas rectas y clavadas en la colchoneta: en el buenos dias la bisagra es
# la cadera, no la rodilla, asi que los pies no deben moverse.
$body5 = StaticLayer 'piernas' 2 @(
  (Limb 'muslo_lejano' 398 610 416 738 54 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 416 738 420 846 40 $script:pantsBk),
  (Limb 'pie_lejano' 420 846 470 854 28 $script:shoe),
  (Limb 'muslo' 372 610 390 738 58 $script:pants),
  (Limb 'pantorrilla' 390 738 398 846 42 $script:pants),
  (Limb 'pie' 398 846 448 854 30 $script:shoe)
)

Write-Anim 'stick_good_morning' 720 920 30 @((Ground 1), $body5, $hips5, $root5)

# ---------- 6. SENTADILLA CON PALO EN LA ESPALDA ----------
# Palo sobre los omoplatos; la cadera baja y las rodillas se abren mientras los
# pies quedan clavados en el suelo. Los pies van en su propia capa y giran al
# reves que la pantorrilla para no hundirse en la colchoneta.
$fr6 = @(0, 15, 30, 45, 60)
$root6Shapes = @(
  (Limb 'tronco' 360 420 360 600 132 $script:shirt),
  (Stick 245 414 475 414 12),
  (Limb 'brazo_izq' 296 432 270 470 32 $script:skin),
  (Limb 'antebrazo_izq' 270 470 245 414 28 $script:skin),
  (Hand 245 414 $script:skin),
  (Limb 'brazo_der' 424 432 450 470 34 $script:skin),
  (Limb 'antebrazo_der' 450 470 475 414 30 $script:skin),
  (Hand 475 414 $script:skin),
  (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants),
  (Limb 'cuello' 360 410 360 388 46 $script:skin),
  (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
  (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
  (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
)
$root6 = MoveLayer 'tronco' 5 $root6Shapes (MoveKeys $fr6 @(
    @(0, 0), @(0, 45), @(0, 45), @(0, 0), @(0, 0)
  )) @(0, 0)

$thighF6 = ChildLayer 'muslo_lejano' 2 5 @(
  (Limb 'muslo' 338 610 322 738 56 $script:pantsBk)
) (RotKeys $fr6 @(0, 32, 32, 0, 0)) @(338, 610)
$calfF6 = ChildLayer 'pantorrilla_lejana' 3 2 @(
  (Limb 'pantorrilla' 322 738 316 846 42 $script:pantsBk)
) (RotKeys $fr6 @(0, -68, -68, 0, 0)) @(322, 738)
$footF6 = ChildLayer 'pie_lejano' 4 3 @(
  (Limb 'pie_lejano' 316 846 366 856 30 $script:shoe)
) (RotKeys $fr6 @(0, 36, 36, 0, 0)) @(316, 846)
$thighN6 = ChildLayer 'muslo' 6 5 @(
  (Limb 'muslo' 382 610 398 738 60 $script:pants)
) (RotKeys $fr6 @(0, -32, -32, 0, 0)) @(382, 610)
$calfN6 = ChildLayer 'pantorrilla' 7 6 @(
  (Limb 'pantorrilla' 398 738 404 846 44 $script:pants)
) (RotKeys $fr6 @(0, 68, 68, 0, 0)) @(398, 738)
$footN6 = ChildLayer 'pie' 8 7 @(
  (Limb 'pie' 404 846 454 856 32 $script:shoe)
) (RotKeys $fr6 @(0, -36, -36, 0, 0)) @(404, 846)

Write-Anim 'stick_back_squat' 720 920 30 @(
  (Ground 1), $thighF6, $calfF6, $footF6, $root6, $thighN6, $calfN6, $footN6
)

# ---------- 7. MOLINO DE HOMBRO CON PALO ----------
# Un brazo estirado con el palo vertical dibuja un circulo completo; el otro
# brazo queda en la cadera. El aro de puntos marca la trayectoria.
$fr7 = @(0, 15, 30, 45, 60)
$guide7 = @()
for ($i = 0; $i -lt 12; $i++) {
  $p = CirclePt 424 432 145 ($i * 30)
  $guide7 += (Filled "guia$i" (Ellipse $p[0] $p[1] 9 9) @(0.45, 0.53, 0.51, 1))
}
$body7 = StaticLayer 'cuerpo' 2 ((FrontLegs) + @(
  (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants),
  (Limb 'tronco' 360 420 360 600 132 $script:shirt),
  (Limb 'brazo_izq' 296 432 262 500 32 $script:skin),
  (Limb 'antebrazo_izq' 262 500 322 556 28 $script:skin),
  (Hand 322 556 $script:skin),
  (Limb 'cuello' 360 410 360 388 46 $script:skin),
  (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
  (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
  (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
))
$arm7 = ShapeLayer 'brazo_palo' 4 @(
  (Stick 560 320 560 544 12),
  (Limb 'brazo' 424 432 560 432 34 $script:skin),
  (Hand 560 432 $script:skin)
) (SpinKeys $fr7 @(0, 90, 180, 270, 360)) @(424, 432)
$guideLayer7 = StaticLayer 'guia' 3 $guide7

Write-Anim 'stick_shoulder_windmill' 720 920 30 @((Ground 1), $body7, $guideLayer7, $arm7)

# ---------- 8. LENADOR CON PALO ----------
# Palo en diagonal sujeto con las dos manos; las manos bajan de arriba a la
# derecha hasta abajo a la izquierda y vuelven, como al dar un hachazo. El
# tronco se queda de frente para que el recorrido diagonal se lea bien.
$fr8 = @(0, 15, 30, 45, 60)
$grip8 = @(@(420, 320), @(400, 440), @(330, 500), @(400, 440), @(420, 320))
$ang8 = @(-55, 10, 120, 10, -55)
$handL8 = @(); $handR8 = @()
for ($i = 0; $i -lt $fr8.Count; $i++) {
  $g = $grip8[$i]
  $a = $ang8[$i] * [math]::PI / 180
  $handL8 += , @($g[0], $g[1])
  $handR8 += , @([math]::Round($g[0] + 46 * [math]::Cos($a), 2),
                  [math]::Round($g[1] + 46 * [math]::Sin($a), 2))
}
$stick8 = ShapeLayer 'palo' 3 @(
  (Stick 0 0 132 0 12)
) (RotKeys $fr8 $ang8) @(0, 0)
$stick8.ks.p = MoveKeys $fr8 $grip8
$armL8 = ArmIK 'brazo_izq' 4 0 @(296, 432) $handL8[0] $handL8 $fr8 85 85 $script:skin $script:skin 'left'
$armR8 = ArmIK 'brazo_der' 6 0 @(424, 432) $handR8[0] $handR8 $fr8 85 85 $script:skinBk $script:skinBk 'right'
$body8 = StaticLayer 'cuerpo' 2 ((FrontLegs) + (FrontTorso))

Write-Anim 'stick_woodchopper' 720 920 30 @(
  (Ground 1), $body8, $stick8, $armL8[0], $armL8[1], $armR8[0], $armR8[1]
)

# ---------- 9. ROTACIONES DE MUNECA CON PALO ----------
# Brazos al frente con el palo horizontal; las munecas lo basculan arriba y
# abajo mientras los codos se quedan casi quietos. Un giro completo del palo
# en el plano taparia la cara y no representa el gesto.
$fr9 = @(0, 15, 30, 45, 60)
$tilt9 = @(0, 18, 0, -18, 0)
$handL9 = @(); $handR9 = @()
for ($i = 0; $i -lt $fr9.Count; $i++) {
  $a = $tilt9[$i] * [math]::PI / 180
  $handL9 += , @([math]::Round(360 - 145 * [math]::Cos($a), 2), [math]::Round(460 - 145 * [math]::Sin($a), 2))
  $handR9 += , @([math]::Round(360 + 145 * [math]::Cos($a), 2), [math]::Round(460 + 145 * [math]::Sin($a), 2))
}
$body9 = StaticLayer 'cuerpo' 2 ((FrontLegs) + @(
  (Filled 'pelvis' (Rect 360 612 120 78 36) $script:pants),
  (Limb 'tronco' 360 420 360 600 132 $script:shirt),
  (Limb 'cuello' 360 410 360 388 46 $script:skin),
  (Filled 'cabello' (Ellipse 360 330 104 80) $script:hair),
  (Filled 'cara' (Ellipse 360 356 92 92) $script:skin),
  (Filled 'coleta' (Rect 300 342 72 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 392 360 10 7) $script:hair)
))
$stick9 = ShapeLayer 'palo' 3 @(
  (Stick 170 460 550 460 12),
  (Limb 'banda' 330 458 358 458 17 $script:stickHi)
) (RotKeys $fr9 $tilt9) @(360, 460)
$armL9 = ArmIK 'brazo_izq' 4 0 @(296, 432) @(215, 460) $handL9 $fr9 80 80 $script:skin $script:skin 'left' 'down'
$armR9 = ArmIK 'brazo_der' 6 0 @(424, 432) @(505, 460) $handR9 $fr9 80 80 $script:skin $script:skin 'right' 'down'

Write-Anim 'stick_wrist_rolls' 720 920 30 @(
  (Ground 1), $body9, $stick9, $armL9[0], $armL9[1], $armR9[0], $armR9[1]
)

# ---------- 10. ELEVACION DE RODILLAS CON PALO ----------
# De perfil, palo sobre los hombros y espalda recta. Se levanta una rodilla y
# luego la otra: el muslo gira desde la cadera hasta quedar horizontal y la
# pantorrilla cuelga, para que la rodilla suba de verdad.
$fr10 = @(0, 15, 30, 45, 60)
$root10Shapes = @(
  (Limb 'brazo_lejano' 376 424 340 450 26 $script:skinBk),
  (Limb 'tronco' 380 420 380 600 104 $script:shirt),
  (Stick 298 450 478 424 15),
  (Limb 'brazo' 382 428 404 458 30 $script:skin),
  (Limb 'antebrazo' 404 458 468 430 26 $script:skin),
  (Hand 470 429 $script:skin 30),
  (Filled 'pelvis' (Rect 386 612 116 76 34) $script:pants),
  (Limb 'cuello' 380 410 390 388 44 $script:skin),
  (Filled 'cabello' (Ellipse 390 330 100 78) $script:hair),
  (Filled 'cara' (Ellipse 410 360 90 90) $script:skin),
  (Filled 'nariz' (Ellipse 458 366 14 12) $script:skin),
  (Filled 'coleta' (Rect 330 350 72 24 12) $script:hair),
  (Filled 'ojo' (Ellipse 436 360 9 6) $script:hair)
)
$root10 = StaticLayer 'tronco' 5 $root10Shapes

$thighF10 = ChildLayer 'muslo_lejano' 2 5 @(
  (Limb 'muslo' 400 610 412 738 54 $script:pantsBk)
) (RotKeys $fr10 @(0, 0, 0, -85, 0)) @(400, 610)
$calfF10 = ChildLayer 'pantorrilla_lejana' 3 2 @(
  (Limb 'pantorrilla' 412 738 420 846 40 $script:pantsBk)
) (RotKeys $fr10 @(0, 0, 0, 85, 0)) @(412, 738)
$footF10 = ChildLayer 'pie_lejano' 4 3 @(
  (Limb 'pie_lejano' 420 846 470 854 28 $script:shoe)
) (RotKeys $fr10 @(0, 0, 0, 0, 0)) @(420, 846)

$thighN10 = ChildLayer 'muslo' 6 5 @(
  (Limb 'muslo' 368 610 390 738 58 $script:pants)
) (RotKeys $fr10 @(0, -85, 0, 0, 0)) @(368, 610)
$calfN10 = ChildLayer 'pantorrilla' 7 6 @(
  (Limb 'pantorrilla' 390 738 396 846 42 $script:pants)
) (RotKeys $fr10 @(0, 85, 0, 0, 0)) @(390, 738)
$footN10 = ChildLayer 'pie' 8 7 @(
  (Limb 'pie' 396 846 446 854 30 $script:shoe)
) (RotKeys $fr10 @(0, 0, 0, 0, 0)) @(396, 846)

Write-Anim 'stick_knee_raise' 720 920 30 @(
  (Ground 1), $thighF10, $calfF10, $footF10, $root10, $thighN10, $calfN10, $footN10
)
