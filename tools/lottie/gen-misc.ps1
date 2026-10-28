# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# Rotacion lineal (sin easing) para giros continuos de 360 grados.
function SpinKeys([array]$frames, [array]$vals) {
  $keys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) { $keys += @{ t = $frames[$i]; s = @($vals[$i]) } }
  @{ a = 1; k = $keys }
}

# Aro guia (trazo) que senala la trayectoria circular.
function Ring([double]$x, [double]$y, [double]$d, [array]$color) {
  [ordered]@{
    ty = 'gr'; nm = 'aro_guia'; hd = $false
    it = @(
      @{ ty = 'el'; d = 1; s = @{ a = 0; k = @($d, $d) }; p = @{ a = 0; k = @($x, $y) }; nm = 'elipse' },
      @{ ty = 'st'; c = @{ a = 0; k = $color }; o = @{ a = 0; k = 60 }; w = @{ a = 0; k = 5 }; lc = 2; lj = 2; nm = 'trazo' },
      @{ ty = 'tr'
        p = @{ a = 0; k = @(0, 0) }; a = @{ a = 0; k = @(0, 0) }; s = @{ a = 0; k = @(100, 100) }
        r = @{ a = 0; k = 0 }; o = @{ a = 0; k = 100 }; sk = @{ a = 0; k = 0 }; sa = @{ a = 0; k = 0 }; nm = 'transform'
      }
    )
    np = 3
  }
}

$guideC = @(0.55, 0.60, 0.58, 1)
$fr = @(0, 16, 30, 46, 60)

# ---------- 1. REMO EN SENTADILLA CON TOALLA ----------
# Cadera (capa raiz) baja a media sentadilla; muslo hereda, pantorrilla hereda
# y el pie se mantiene en el suelo. Brazos en cadena hombro->codo: se extienden
# y tiran de la toalla llevando los codos atras.
$W = 900; $H = 900
$shX = 440; $shY = 380
$hipX = 455; $hipY = 545
$squatDx = -30; $squatDy = 80

$suelo = StaticLayer 'suelo' 1 @(
  (Filled 'suelo' (Rect 470 864 760 24 12) $script:matDark),
  (Filled 'colchoneta' (Rect 470 846 700 20 10) $script:mat)
)

# capa raiz: torso + cabeza + pelvis (baja y sube con la sentadilla)
$root = MoveLayer 'tronco' 2 @(
  (Limb 'brazo_trasero' ($shX + 8) ($shY + 6) ($shX + 93) ($shY + 14) 32 $script:skinBk),
  (Limb 'antebrazo_trasero' ($shX + 93) ($shY + 14) ($shX + 168) ($shY + 20) 28 $script:skinBk),
  (Limb 'tronco' $shX $shY $hipX $hipY 76 $script:shirt),
  (Filled 'pelvis' (Rect ($hipX + 6) $hipY 112 74 34) $script:pants),
  (Filled 'cabello' (Ellipse ($shX - 8) ($shY - 58) 94 72) $script:hair),
  (Filled 'cara' (Ellipse ($shX + 20) ($shY - 32) 86 86) $script:skin),
  (Filled 'coleta' (Rect ($shX - 70) ($shY - 42) 76 26 13) $script:hair),
  (Filled 'ojo' (Ellipse ($shX + 48) ($shY - 26) 10 7) $script:hair)
) (MoveKeys $fr @(
    @(0, 0), @($squatDx, $squatDy), @($squatDx, $squatDy), @(0, 0), @(0, 0)
  )) @(0, 0)

# brazo lejano (mismo encadenamiento que el cercano)
$uArmF = ChildLayer 'brazo_sup_lejano' 3 2 @(
  (Limb 'brazo_sup' ($shX + 8) ($shY + 6) ($shX + 93) ($shY + 14) 32 $script:skinBk)
) (RotKeys $fr @(0, 0, 118, 0, 0)) @(($shX + 8), ($shY + 6))
$fArmF = ChildLayer 'brazo_inf_lejano' 4 3 @(
  (Limb 'antebrazo' ($shX + 93) ($shY + 14) ($shX + 168) ($shY + 20) 28 $script:skinBk),
  (Filled 'toalla_lejana' (Rect ($shX + 232) ($shY + 22) 104 20 10) $script:towelC),
  (Filled 'mano_lejana' (Ellipse ($shX + 168) ($shY + 20) 40 32) $script:skinBk)
) (RotKeys $fr @(0, 0, -108, 0, 0)) @(($shX + 93), ($shY + 14))

# pierna lejos (a 15px de profundidad: pie un poco a la derecha y mas abajo)
$hipF = @(($hipX + 15), ($hipY + 9))
$kneeF = @(($hipX + 30), ($hipY + 163))
$ankF  = @(($hipX + 46), ($hipY + 309))
$thighF = ChildLayer 'muslo_lejos' 5 2 @(
  (Limb 'muslo' $hipF[0] $hipF[1] $kneeF[0] $kneeF[1] 56 $script:pantsBk)
) (RotKeys $fr @(0, -34, -34, 0, 0)) @($hipF[0], $hipF[1])
$calfF = ChildLayer 'pantorrilla_lejos' 6 5 @(
  (Limb 'pantorrilla' $kneeF[0] $kneeF[1] $ankF[0] $ankF[1] 42 $script:pantsBk),
  (Limb 'pie' $ankF[0] $ankF[1] ($ankF[0] + 50) ($ankF[1] + 6) 30 $script:shoe)
) (RotKeys $fr @(0, 72, 72, 0, 0)) @($kneeF[0], $kneeF[1])

# brazo cercano
$uArm = ChildLayer 'brazo_sup' 7 2 @(
  (Limb 'brazo_sup' $shX $shY ($shX + 85) ($shY + 8) 36 $script:skin)
) (RotKeys $fr @(0, 0, 118, 0, 0)) @($shX, $shY)
$fArm = ChildLayer 'brazo_inf' 8 7 @(
  (Limb 'antebrazo' ($shX + 85) ($shY + 8) ($shX + 160) ($shY + 14) 32 $script:skin),
  (Filled 'toalla' (Rect ($shX + 226) ($shY + 16) 112 22 11) $script:towelC),
  (Filled 'mano' (Ellipse ($shX + 160) ($shY + 14) 42 34) $script:skin)
) (RotKeys $fr @(0, 0, -108, 0, 0)) @(($shX + 85), ($shY + 8))

# pierna cercano (con pie que se aplana al agacharse)
$kneeN = @(($hipX + 22), ($hipY + 161))
$ankN  = @(($hipX + 40), ($hipY + 301))
$thigh = ChildLayer 'muslo' 9 2 @(
  (Limb 'muslo' $hipX $hipY $kneeN[0] $kneeN[1] 60 $script:pants)
) (RotKeys $fr @(0, -35, -35, 0, 0)) @($hipX, $hipY)
$calf = ChildLayer 'pantorrilla' 10 9 @(
  (Limb 'pantorrilla' $kneeN[0] $kneeN[1] $ankN[0] $ankN[1] 44 $script:pantsBk)
) (RotKeys $fr @(0, 68, 68, 0, 0)) @($kneeN[0], $kneeN[1])
$foot = ChildLayer 'pie' 11 10 @(
  (Limb 'pie' $ankN[0] $ankN[1] ($ankN[0] + 50) ($ankN[1] + 6) 32 $script:shoe)
) (RotKeys $fr @(0, -27, -27, 0, 0)) @($ankN[0], $ankN[1])

Write-Anim 'towel_squat_row' $W $H 30 @(
  $suelo, $root, $uArmF, $fArmF, $thighF, $calfF, $uArm, $fArm, $thigh, $calf, $foot
)

# ---------- 2. CIRCULOS DE TOBILLOS ----------
# Apoyado en la pared, una pierna atras con el pie elevado; el pie gira
# 360 grados sobre el tobillo, senalado por un aro guia.
$W2 = 720; $H2 = 920
$ankX = 410; $ankY = 768

$wall = StaticLayer 'pared_suelo' 1 @(
  (Filled 'panel' (Rect 120 520 56 700 8) $script:wallC),
  (Filled 'canto' (Rect 148 520 12 700 4) $script:wallDk),
  (Filled 'suelo' (Rect 430 864 540 24 12) $script:matDark)
)

$shoeDk = @(0.20, 0.24, 0.32, 1)

# zapato con contorno oscuro: el blanco sobre el fondo claro no se veia
$pieLejano = ShapeLayer 'pie_lejano' 2 @(
  (Limb 'pie_contorno' $ankX $ankY ($ankX + 58) ($ankY + 30) 40 $shoeDk),
  (Limb 'pie' $ankX $ankY ($ankX + 52) ($ankY + 27) 30 $script:shoe)
) (SpinKeys @(0, 15, 30, 45, 60) @(0, 90, 180, 270, 360)) @($ankX, $ankY)

# aro guia punteado (puntos rellenos: se ven igual en la app y en la galeria)
$guia = StaticLayer 'guia' 3 @(
  @(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) | ForEach-Object {
    $p = CirclePt $ankX $ankY 74 ($_ * 30)
    (Filled "guia$_" (Ellipse $p[0] $p[1] 9 9) @(0.45, 0.53, 0.51, 1))
  }
)

$cuerpo = StaticLayer 'cuerpo' 4 @(
  (Limb 'muslo_lejano' 300 600 378 690 54 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 378 690 $ankX $ankY 40 $script:pantsBk),
  (Limb 'brazo' 258 408 158 436 34 $script:skin),
  (Filled 'mano' (Ellipse 158 436 44 40) $script:skin),
  (Limb 'tronco' 258 408 292 596 76 $script:shirt),
  (Filled 'pelvis' (Rect 298 596 104 74 34) $script:pants),
  (Limb 'muslo' 292 596 302 716 58 $script:pants),
  (Limb 'pantorrilla' 302 716 296 838 44 $script:pantsBk),
  (Limb 'pie' 296 844 246 852 32 $script:shoe),
  (Filled 'cabello' (Ellipse 256 336 92 70) $script:hair),
  (Filled 'cara' (Ellipse 232 362 86 86) $script:skin),
  (Filled 'coleta' (Rect 288 352 74 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 206 368 10 7) $script:hair)
)

Write-Anim 'warmup_ankle_circles' $W2 $H2 30 @($wall, $pieLejano, $guia, $cuerpo)

# ---------- 3. CIRCULOS DE MUECAS ----------
# De pie, brazos extendidos a la altura de los hombros; cada mano gira
# 360 grados sobre su propia muneca.
$W3 = 720; $H3 = 920
$wrX = 468; $wrY = 416

$suelo3 = StaticLayer 'suelo' 1 @(
  (Filled 'suelo' (Rect 430 864 540 24 12) $script:matDark),
  (Filled 'colchoneta' (Rect 430 846 540 20 10) $script:mat)
)

$manoLejana = ShapeLayer 'mano_lejana' 2 @(
  (Limb 'mano' ($wrX + 6) ($wrY + 8) ($wrX + 76) ($wrY + 4) 32 $script:skinBk),
  (Filled 'nudos' (Ellipse ($wrX + 78) ($wrY + 4) 34 30) $script:skinBk)
) (SpinKeys @(0, 15, 30, 45, 60) @(0, 90, 180, 270, 360)) @(($wrX + 6), ($wrY + 8))

$cuerpo3 = StaticLayer 'cuerpo' 3 @(
  (Limb 'brazo_lejano' 306 416 394 420 32 $script:skinBk),
  (Limb 'antebrazo_lejano' 394 420 ($wrX + 6) ($wrY + 8) 28 $script:skinBk),
  (Limb 'muslo_lejano' 331 608 341 728 54 $script:pantsBk),
  (Limb 'pantorrilla_lejana' 341 728 334 848 40 $script:pantsBk),
  (Limb 'brazo' 300 410 388 414 34 $script:skin),
  (Limb 'antebrazo' 388 414 $wrX $wrY 30 $script:skin),
  (Limb 'tronco' 300 410 315 600 76 $script:shirt),
  (Filled 'pelvis' (Rect 321 600 108 74 34) $script:pants),
  (Limb 'muslo' 315 600 325 720 58 $script:pants),
  (Limb 'pantorrilla' 325 720 318 840 44 $script:pantsBk),
  (Limb 'pie' 318 846 368 854 32 $script:shoe),
  (Ring $wrX $wrY 74 $guideC),
  (Filled 'cabello' (Ellipse 292 346 94 72) $script:hair),
  (Filled 'cara' (Ellipse 316 372 86 86) $script:skin),
  (Filled 'coleta' (Rect 244 362 76 26 13) $script:hair),
  (Filled 'ojo' (Ellipse 344 378 10 7) $script:hair)
)

$manoCerca = ShapeLayer 'mano' 4 @(
  (Limb 'mano' $wrX $wrY ($wrX + 70) ($wrY - 4) 34 $script:skin),
  (Filled 'nudos' (Ellipse ($wrX + 72) ($wrY - 4) 36 32) $script:skin)
) (SpinKeys @(0, 15, 30, 45, 60) @(0, 90, 180, 270, 360)) @($wrX, $wrY)

Write-Anim 'warmup_wrist_circles' $W3 $H3 30 @($suelo3, $manoLejana, $cuerpo3, $manoCerca)
