# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'

$lib = $PSScriptRoot
New-Item -ItemType Directory -Force -Path $lib | Out-Null

function Col([double]$r, [double]$g, [double]$b) { , @($r, $g, $b, 1) }

$script:mat      = Col 0.72 0.90 0.87
$script:matDark  = Col 0.60 0.82 0.79
$script:pants    = Col 0.15 0.19 0.26
$script:pantsBk  = Col 0.11 0.14 0.20
$script:shirt    = Col 0.86 0.23 0.26
$script:shirtBk  = Col 0.72 0.18 0.21
$script:skin     = Col 0.96 0.78 0.65
$script:skinBk   = Col 0.88 0.69 0.57
$script:hair     = Col 0.35 0.22 0.15
$script:shoe     = Col 0.94 0.94 0.96
$script:wallC    = Col 0.80 0.84 0.82
$script:wallDk   = Col 0.70 0.75 0.73
$script:chairC   = Col 0.74 0.60 0.44
$script:chairDk  = Col 0.62 0.49 0.35
$script:towelC   = Col 0.95 0.82 0.42

# Relleno: el group tiene su propio transform identidad; rc.p ya es el centro absoluto.
function Filled([string]$name, $shape, [array]$color) {
  [ordered]@{
    ty = 'gr'; nm = $name; hd = $false
    it = @(
      $shape,
      @{ ty = 'fl'; c = @{ a = 0; k = $color }; o = @{ a = 0; k = 100 }; r = 1; bm = 0; nm = 'relleno' },
      @{ ty = 'tr'
        p = @{ a = 0; k = @(0, 0) }; a = @{ a = 0; k = @(0, 0) }; s = @{ a = 0; k = @(100, 100) }
        r = @{ a = 0; k = 0 }; o = @{ a = 0; k = 100 }; sk = @{ a = 0; k = 0 }; sa = @{ a = 0; k = 0 }; nm = 'transform'
      }
    )
    np = 3
  }
}

function Rect([double]$x, [double]$y, [double]$w, [double]$h, [double]$round) {
  @{ ty = 'rc'; d = 1; s = @{ a = 0; k = @($w, $h) }; p = @{ a = 0; k = @($x, $y) }; r = @{ a = 0; k = $round }; nm = 'rect' }
}

function Ellipse([double]$x, [double]$y, [double]$w, [double]$h) {
  @{ ty = 'el'; d = 1; s = @{ a = 0; k = @($w, $h) }; p = @{ a = 0; k = @($x, $y) }; nm = 'elipse' }
}

# Segmento entre dos puntos. El rectangulo se coloca en el centro absoluto y
# gira en su propio group transform: igual que el superman (rc.p absoluto, tr identidad).
function Limb([string]$name, [double]$x1, [double]$y1, [double]$x2, [double]$y2, [double]$th, [array]$color) {
  $dx = $x2 - $x1; $dy = $y2 - $y1
  $len = [math]::Sqrt($dx * $dx + $dy * $dy)
  $ang = [math]::Atan2($dy, $dx) * 180 / [math]::PI
  $mx = ($x1 + $x2) / 2; $my = ($y1 + $y2) / 2
  $rc = @{ ty = 'rc'; d = 1; s = @{ a = 0; k = @($len, $th) }; p = @{ a = 0; k = @($mx, $my) }; r = @{ a = 0; k = $th / 2 }; nm = 'seg' }
  [ordered]@{
    ty = 'gr'; nm = $name; hd = $false
    it = @(
      $rc,
      @{ ty = 'fl'; c = @{ a = 0; k = $color }; o = @{ a = 0; k = 100 }; r = 1; bm = 0; nm = 'relleno' },
      @{ ty = 'tr'
        p = @{ a = 0; k = @($mx, $my) }; a = @{ a = 0; k = @($mx, $my) }; s = @{ a = 0; k = @(100, 100) }
        r = @{ a = 0; k = $ang }; o = @{ a = 0; k = 100 }; sk = @{ a = 0; k = 0 }; sa = @{ a = 0; k = 0 }; nm = 'transform'
      }
    )
    np = 3
  }
}

function RotKeys([array]$frames, [array]$values) {
  $keys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) {
    $k = @{ t = $frames[$i]; s = @($values[$i]) }
    if ($i -lt $frames.Count - 1) {
      $k.i = @{ x = @(0.42); y = @(1) }
      $k.o = @{ x = @(0.58); y = @(0) }
    }
    $keys += $k
  }
  @{ a = 1; k = $keys }
}

# Traslacion animada: values = array de arrays [x,y].
function MoveKeys([array]$frames, [array]$values) {
  $keys = @()
  for ($i = 0; $i -lt $frames.Count; $i++) {
    $k = @{ t = $frames[$i]; s = @($values[$i]) }
    if ($i -lt $frames.Count - 1) {
      $k.i = @{ x = @(0.42, 0.42); y = @(1, 1) }
      $k.o = @{ x = @(0.58, 0.58); y = @(0, 0) }
    }
    $keys += $k
  }
  @{ a = 1; k = $keys }
}

# Capa con padre (parenting de Lottie): el hijo hereda la rotacion del padre.
function ChildLayer([string]$name, [int]$ind, [int]$parent, [array]$shapeGroups, $r, [array]$pivot) {
  $l = ShapeLayer $name $ind $shapeGroups $r $pivot
  $l['pr'] = $parent
  $l
}

# Capa con desplazamiento animado (p) y ancla fija; util para bajar/subir el
# cuerpo entero sin rotar. El desplazamiento es un delta: la ancla va en (0,0)
# para no desplazar las formas, que ya estan en coordenadas absolutas.
function MoveLayer([string]$name, [int]$ind, [array]$shapeGroups, $p, [array]$pivot, [int]$op = 60) {
  $l = ShapeLayer $name $ind $shapeGroups @{ a = 0; k = 0 } @(0, 0) $op
  $l.ks.p = $p
  $l
}

# Capa con rotacion animada (gira sobre un pivot dado) o estatica.
function ShapeLayer([string]$name, [int]$ind, [array]$shapeGroups, $r, [array]$pivot, [int]$op = 60) {
  [ordered]@{
    ddd = 0; ind = $ind; ty = 4; nm = $name; sr = 1
    ks = [ordered]@{
      o = @{ a = 0; k = 100 }
      r = $r
      p = @{ a = 0; k = $pivot }
      a = @{ a = 0; k = $pivot }
      s = @{ a = 0; k = @(100, 100, 100) }
    }
    ao = 0; shapes = $shapeGroups; ip = 0; op = $op; st = 0; bm = 0
  }
}

function StaticLayer([string]$name, [int]$ind, [array]$shapeGroups, [int]$op = 60) {
  ShapeLayer $name $ind $shapeGroups @{ a = 0; k = 0 } @(0, 0) $op
}

# Punto sobre una circunferencia. ang en grados, 0 = derecha, +90 = abajo.
function CirclePt([double]$cx, [double]$cy, [double]$r, [double]$ang) {
  $a = $ang * [math]::PI / 180
  , @([math]::Round($cx + $r * [math]::Cos($a), 2), [math]::Round($cy + $r * [math]::Sin($a), 2))
}

# Gira un punto alrededor de un centro (positivo = horario en pantalla).
function RotPt([double]$px, [double]$py, [double]$cx, [double]$cy, [double]$a) {
  $r = $a * [math]::PI / 180
  $dx = $px - $cx; $dy = $py - $cy
  , @([math]::Round($cx + $dx * [math]::Cos($r) - $dy * [math]::Sin($r), 2),
      [math]::Round($cy + $dx * [math]::Sin($r) + $dy * [math]::Cos($r), 2))
}

# Codo por interseccion de circunferencias: hombro->codo (l1) y mano->codo (l2).
# signo -1 deja el codo hacia atras (hacia los pies/cabeza segun la pose).
function Codo([double]$sx, [double]$sy, [double]$hx, [double]$hy, [double]$l1, [double]$l2, [double]$sign = -1) {
  $dx = $hx - $sx; $dy = $hy - $sy
  $d = [math]::Sqrt($dx * $dx + $dy * $dy)
  if ($d -lt 0.01) { $d = 0.01 }
  $a = ($l1 * $l1 - $l2 * $l2 + $d * $d) / (2 * $d)
  $hh = [math]::Sqrt([math]::Max(0, $l1 * $l1 - $a * $a))
  $mx = $sx + $a * $dx / $d; $my = $sy + $a * $dy / $d
  , @([math]::Round($mx - $sign * $hh * $dy / $d, 2), [math]::Round($my + $sign * $hh * $dx / $d, 2))
}

function AngDeg([double]$dx, [double]$dy) { [math]::Atan2($dy, $dx) * 180 / [math]::PI }

# Cadena muslo -> pantorrilla -> pie a partir de angulos absolutos por fotograma.
# El pie hereda la pantorrilla; la rotacion de cada capa es el delta respecto a
# la pose base (fotograma 0) y el hijo descuenta el giro heredado del padre.
function LegChain([string]$name, [int]$ind, [array]$hip, [double]$l1, [double]$l2,
                  [array]$frames, [array]$thAng, [array]$shAng,
                  [double]$thW, [double]$calW, [double]$footLen, [double]$footW,
                  [double]$footRel, [array]$cThigh, [array]$cCalf, [array]$cShoe, [int]$op = 60) {
  $th0 = $thAng[0]; $sh0 = $shAng[0]
  $knee0 = CirclePt $hip[0] $hip[1] $l1 $th0
  $ank0 = CirclePt $knee0[0] $knee0[1] $l2 $sh0
  $toe0 = CirclePt $ank0[0] $ank0[1] $footLen ($sh0 + $footRel)

  $thD = @(); $calD = @()
  for ($i = 0; $i -lt $frames.Count; $i++) {
    $td = $thAng[$i] - $th0
    $cd = ($shAng[$i] - $sh0) - $td
    $thD += [math]::Round($td, 2)
    $calD += [math]::Round($cd, 2)
  }

  $thigh = ShapeLayer "$($name)_muslo" $ind @(
    (Limb 'muslo' $hip[0] $hip[1] $knee0[0] $knee0[1] $thW $cThigh)
  ) (RotKeys $frames $thD) @($hip[0], $hip[1]) $op

  $calf = ShapeLayer "$($name)_pantorrilla" ($ind + 1) @(
    (Limb 'pantorrilla' $knee0[0] $knee0[1] $ank0[0] $ank0[1] $calW $cCalf),
    (Limb 'pie' $ank0[0] $ank0[1] $toe0[0] $toe0[1] $footW $cShoe)
  ) (RotKeys $frames $calD) @($knee0[0], $knee0[1]) $op
  $calf['pr'] = $ind
  , @($thigh, $calf)
}

function Write-Anim([string]$outName, [int]$w, [int]$h, [int]$fr, [array]$layers) {
  $out = (Join-Path $repo 'app\src\main\assets\exercises\lottie\') + $outName + '.json'
  $anim = [ordered]@{
    v = '5.7.4'; fr = $fr; ip = 0; op = 60; w = $w; h = $h
    nm = $outName; ddd = 0; assets = @()
    layers = $layers; markers = @()
    meta = @{ g = 'FitCasa' }
  }
  $json = $anim | ConvertTo-Json -Depth 80 -Compress
  [System.IO.File]::WriteAllText($out, $json, (New-Object System.Text.UTF8Encoding($false)))
  "{0,-26} {1,5} KB" -f $outName, [math]::Round((Get-Item $out).Length / 1KB)
}
