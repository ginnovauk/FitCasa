# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib.ps1')

# Flexiones de perfil. El cuerpo es rigido y gira sobre la rodilla (o el pie),
# asi que el hombro baja en arco. El brazo se resuelve con IK de dos huesos:
# la mano queda clavada en la colchoneta y el codo se atrasa al bajar.
$W = 1000; $H = 560
$frames = @(0, 16, 30, 46, 60)
$rotVals = @(0, 0, 0, 0, 0)   # se rellena por pose

function Ang([double]$dx, [double]$dy) { [math]::Atan2($dy, $dx) * 180 / [math]::PI }

function RotPt([double]$px, [double]$py, [double]$cx, [double]$cy, [double]$a) {
  $r = $a * [math]::PI / 180
  $dx = $px - $cx; $dy = $py - $cy
  , @([math]::Round($cx + $dx * [math]::Cos($r) - $dy * [math]::Sin($r), 2),
      [math]::Round($cy + $dx * [math]::Sin($r) + $dy * [math]::Cos($r), 2))
}

# Codo: interseccion de las circunferencias de radios l1 (hombro) y l2 (mano).
function Codo([double]$sx, [double]$sy, [double]$hx, [double]$hy, [double]$l1, [double]$l2) {
  $dx = $hx - $sx; $dy = $hy - $sy
  $d = [math]::Sqrt($dx * $dx + $dy * $dy)
  $a = ($l1 * $l1 - $l2 * $l2 + $d * $d) / (2 * $d)
  $hh = [math]::Sqrt([math]::Max(0, $l1 * $l1 - $a * $a))
  $mx = $sx + $a * $dx / $d; $my = $sy + $a * $dy / $d
  # signo -1: el codo se atrasa (hacia los pies)
  , @([math]::Round($mx - $hh * $dy / $d, 2), [math]::Round($my + $hh * $dx / $d, 2))
}

function PushUp([string]$name, [bool]$kneeling, [double]$bodyRot) {
  $handX = 380; $handY = 470
  $l1 = 58; $l2 = 60
  $s0x = $handX; $s0y = $handY - ($l1 + $l2)      # brazo recto en reposo
  $e0x = $s0x; $e0y = $s0y + $l1
  $hipX = 560; $hipY = $s0y + 18

  if ($kneeling) {
    $kneeX = 700; $kneeY = 466
    $ankX = 792; $ankY = 424
    $toeX = 844; $toeY = 416
    $legs = @(
      (Limb 'muslo' $hipX $hipY $kneeX $kneeY 64 $script:pants),
      (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 46 $script:pantsBk),
      (Limb 'pie' $ankX $ankY $toeX $toeY 32 $script:shoe)
    )
    $pivotX = $kneeX; $pivotY = $kneeY
  } else {
    $kneeX = 712; $kneeY = $s0y + 60
    $ankX = 830; $ankY = $s0y + 86
    $toeX = 878; $toeY = $s0y + 114
    $legs = @(
      (Limb 'muslo' $hipX $hipY $kneeX $kneeY 62 $script:pants),
      (Limb 'pantorrilla' $kneeX $kneeY $ankX $ankY 46 $script:pantsBk),
      (Limb 'pie' $ankX $ankY $toeX $toeY 30 $script:shoe)
    )
    $pivotX = $ankX; $pivotY = $ankY
  }

  $body = @(
    (Filled 'cadera' (Rect $hipX $hipY 120 78 36) $script:pants)
  ) + $legs + @(
    (Limb 'tronco' $s0x $s0y $hipX $hipY 78 $script:shirt),
    (Limb 'cuello' ($s0x - 28) ($s0y - 10) $s0x $s0y 40 $script:skin),
    (Filled 'cabello' (Ellipse ($s0x - 66) ($s0y - 30) 90 68) $script:hair),
    (Filled 'cara' (Ellipse ($s0x - 86) ($s0y - 8) 82 82) $script:skin),
    (Filled 'coleta' (Rect ($s0x - 26) ($s0y - 24) 70 24 12) $script:hair),
    (Filled 'ojo' (Ellipse ($s0x - 110) ($s0y - 4) 10 7) $script:hair)
  )

  # claves de rotacion del cuerpo, IK del brazo y desplazamiento del hombro
  $bodyRotKeys = @(); $pKeys = @(); $thetaKeys = @(); $phiKeys = @()
  foreach ($f in $frames) {
    $t = if ($f -eq 0 -or $f -eq 60) { 0 } elseif ($f -eq 46) { 0 } else { 1 }
    $r = if ($f -eq 16 -or $f -eq 30) { $bodyRot } else { 0 }
    $bodyRotKeys += $r
    $s = RotPt $s0x $s0y $pivotX $pivotY $r
    $e = Codo $s[0] $s[1] $handX $handY $l1 $l2
    $theta = (Ang ($e[0] - $s[0]) ($e[1] - $s[1])) - (Ang ($e0x - $s0x) ($e0y - $s0y))
    $phi = (Ang ($handX - $e[0]) ($handY - $e[1])) - (Ang ($handX - $e0x) ($handY - $e0y)) - $theta
    $thetaKeys += [math]::Round($theta, 2)
    $phiKeys += [math]::Round($phi, 2)
    $pKeys += , @([math]::Round($s[0], 2), [math]::Round($s[1], 2))
  }

  $mat = StaticLayer 'colchoneta' 1 @(
    (Filled 'colchoneta' (Rect 600 508 760 30 15) $script:mat),
    (Filled 'borde' (Rect 600 524 760 8 4) $script:matDark)
  )

  $bodyLayer = ShapeLayer 'cuerpo' 2 $body (RotKeys $frames $bodyRotKeys) @($pivotX, $pivotY)

  $handLayer = StaticLayer 'mano' 3 @(
    (Filled 'mano' (Rect $handX ($handY + 6) 56 22 11) $script:skin)
  )

  # brazo delantero: gira sobre el hombro; el hombro se desplaza con el cuerpo
  $upper = ShapeLayer 'brazo' 4 @(
    (Limb 'brazo' $s0x $s0y $e0x $e0y 38 $script:skin)
  ) (RotKeys $frames $thetaKeys) @($s0x, $s0y)
  $upper.ks.p = MoveKeys $frames $pKeys

  $fore = ChildLayer 'antebrazo' 5 4 @(
    (Limb 'antebrazo' $e0x $e0y $handX $handY 32 $script:skin)
  ) (RotKeys $frames $phiKeys) @($e0x, $e0y)

  Write-Anim $name $W $H 30 @($mat, $bodyLayer, $handLayer, $upper, $fore)
  "  theta: $($thetaKeys -join ' / ')"
  "  phi:   $($phiKeys -join ' / ')"
  "  hombro: $((($pKeys | ForEach-Object { $_.ToString() }) -join ' | '))"
}

# Rodillas apoyadas: giro corto sobre la rodilla.
PushUp 'push_ups_knees' $true -5.5
# Diamante: piernas rectas, mas recorrido.
PushUp 'push_ups_diamond' $false -6.5
