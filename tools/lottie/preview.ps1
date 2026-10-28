# Rutas portables: el repo se deduce de la ubicacion del script y los
# temporales de revision van a %TEMP%, nunca dentro del repo.
$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$work = Join-Path $env:TEMP 'fitcasa-lottie'
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

# Previsualizador del subconjunto de Lottie que usa la app:
#   - capas de formas (ty=4) con grupos rc/el + fl + tr
#   - capas de imagen (ty=2) con PNG en data URI
#   - capas de precomp (ty=0) con sus capas anidadas
# Respeta padre (pr), rotacion sobre el ancla, escala y orden de array.

$script:frame = 0
$script:imgCache = @{}

function Get-Kf($prop) {
  if ($null -eq $prop) { return $null }
  if ($prop.a -eq 1) {
    $sel = $prop.k[0]
    foreach ($kf in $prop.k) { if ($kf.t -le $script:frame) { $sel = $kf } }
    return $sel.s
  }
  return $prop.k
}

function Get-Matrix($layer, [hashtable]$byInd) {
  $p = Get-Kf $layer.ks.p
  $a = Get-Kf $layer.ks.a
  $r = Get-Kf $layer.ks.r
  $s = Get-Kf $layer.ks.s
  if ($r -is [array]) { $r = $r[0] }
  if (-not $s) { $s = @(100, 100) }
  $m = New-Object System.Drawing.Drawing2D.Matrix
  $m.Translate($p[0], $p[1])
  $m.Rotate($r)
  $m.Scale(($s[0] / 100.0), ($s[1] / 100.0))
  $m.Translate(-$a[0], -$a[1])
  if ($layer.pr -and $byInd.ContainsKey([int]$layer.pr)) {
    $pm = Get-Matrix $byInd[[int]$layer.pr] $byInd
    $pm.Multiply($m)
    return $pm
  }
  return $m
}

function Get-Image([string]$dataUri, [string]$id) {
  if ($script:imgCache.ContainsKey($id)) { return $script:imgCache[$id] }
  $b64 = $dataUri.Substring($dataUri.IndexOf(',') + 1)
  $bytes = [System.Convert]::FromBase64String($b64)
  $ms = New-Object System.IO.MemoryStream(,$bytes)
  $img = [System.Drawing.Image]::FromStream($ms)
  $script:imgCache[$id] = $img
  return $img
}

function Draw-Layers($layers, $assets, [System.Drawing.Graphics]$g) {
  if (-not $layers) { return }
  $byInd = @{}
  foreach ($l in $layers) { if ($l.ind) { $byInd[[int]$l.ind] = $l } }

  foreach ($l in $layers) {
    $st = $g.Save()
    if ($l.ks) {
      $g.MultiplyTransform((Get-Matrix $l $byInd))
    }

    if ($l.ty -eq 2 -and $l.refId) {
      $asset = $assets[$l.refId]
      if ($asset -and $asset.p) {
        $img = Get-Image $asset.p $asset.id
        $g.DrawImage($img, 0, 0, [single]$asset.w, [single]$asset.h)
      }
    } elseif ($l.ty -eq 0 -and $l.refId) {
      $asset = $assets[$l.refId]
      if ($asset) { Draw-Layers $asset.layers $assets $g }
    } elseif ($l.ty -eq 4 -and $l.shapes) {
      foreach ($gr in $l.shapes) {
        if ($gr.ty -ne 'gr') { continue }
        $shape = $null; $fill = $null; $tr = $null
        foreach ($it in $gr.it) {
          if ($it.ty -eq 'rc' -or $it.ty -eq 'el') { $shape = $it }
          elseif ($it.ty -eq 'fl') { $fill = $it }
          elseif ($it.ty -eq 'tr') { $tr = $it }
        }
        if (-not $shape -or -not $fill) { continue }
        $stG = $g.Save()
        if ($tr) {
          $tp = Get-Kf $tr.p; $ta = Get-Kf $tr.a; $trr = Get-Kf $tr.r
          if ($trr -is [array]) { $trr = $trr[0] }
          $gm = New-Object System.Drawing.Drawing2D.Matrix
          $gm.Translate($tp[0], $tp[1])
          $gm.Rotate($trr)
          $gm.Translate(-$ta[0], -$ta[1])
          $g.MultiplyTransform($gm)
        }
        $c = Get-Kf $fill.c
        $col = [System.Drawing.Color]::FromArgb(255, [int]($c[0] * 255), [int]($c[1] * 255), [int]($c[2] * 255))
        $brush = New-Object System.Drawing.SolidBrush $col
        $sp = Get-Kf $shape.p; $ss = Get-Kf $shape.s
        if ($shape.ty -eq 'rc') {
          $rr = Get-Kf $shape.r
          if ($rr -is [array]) { $rr = $rr[0] }
          $x = $sp[0] - $ss[0] / 2; $y = $sp[1] - $ss[1] / 2
          $d = [math]::Min($rr * 2, [math]::Min($ss[0], $ss[1]))
          if ($d -gt 0.5) {
            $pth = New-Object System.Drawing.Drawing2D.GraphicsPath
            $pth.AddArc($x, $y, $d, $d, 180, 90)
            $pth.AddArc($x + $ss[0] - $d, $y, $d, $d, 270, 90)
            $pth.AddArc($x + $ss[0] - $d, $y + $ss[1] - $d, $d, $d, 0, 90)
            $pth.AddArc($x, $y + $ss[1] - $d, $d, $d, 90, 90)
            $pth.CloseFigure()
            $g.FillPath($brush, $pth)
            $pth.Dispose()
          } else {
            $g.FillRectangle($brush, [single]$x, [single]$y, [single]$ss[0], [single]$ss[1])
          }
        } else {
          $g.FillEllipse($brush, [single]($sp[0] - $ss[0] / 2), [single]($sp[1] - $ss[1] / 2), [single]$ss[0], [single]$ss[1])
        }
        $brush.Dispose()
        $g.Restore($stG)
      }
    }
    $g.Restore($st)
  }
}

function Render-Anim([string]$path, [string]$outPng, [double]$scale = 1, [int]$frame = 0, [int]$cropX = -1, [int]$cropY = -1, [int]$cropW = -1, [int]$cropH = -1) {
  $script:frame = $frame
  $j = Get-Content $path -Raw | ConvertFrom-Json
  $w = [int]$j.w; $h = [int]$j.h
  if ($cropW -le 0) { $cropX = 0; $cropY = 0; $cropW = $w; $cropH = $h }
  $bw = [int]([double]$cropW * [double]$scale); $bh = [int]([double]$cropH * [double]$scale)
  $bmp = New-Object System.Drawing.Bitmap $bw, $bh
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.Clear([System.Drawing.Color]::FromArgb(255, 250, 250, 250))
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
  $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $g.ScaleTransform($scale, $scale)
  $g.TranslateTransform(-$cropX, -$cropY)

  $assets = @{}
  if ($j.assets) { foreach ($a in $j.assets) { $assets[$a.id] = $a } }

  Draw-Layers $j.layers $assets $g

  $g.Dispose()
  $bmp.Save($outPng, [System.Drawing.Imaging.ImageFormat]::Png)
  $bmp.Dispose()
  $outPng
}
