# Animaciones Lottie propias de FitCasa

Generador de las 16 animaciones vectoriales que no vienen de LottieFiles
(calentamiento, abdomen, pecho, tríceps, espalda y cuerpo completo). Los JSON
resultantes están en `app/src/main/assets/exercises/lottie/`.

## Regenerar

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/lottie/gen-abs.ps1
```

Cada `gen-*.ps1` reescribe sus JSON completos:

| Script | Animaciones |
| --- | --- |
| `gen-abs.ps1` | `bicycle_crunches`, `reverse_plank_leg_lift` |
| `gen-prone.ps1` | `mountain_climbers`, `prone_elbow_lift`, `scapula_push_up` |
| `gen-pushups.ps1` | `push_ups_knees`, `push_ups_diamond` |
| `gen-triceps.ps1` | `triceps_dips_floor`, `kneeling_triceps_extension`, `side_push_up`, `body_up` |
| `gen-wallpush.ps1` | `wall_push_ups` |
| `gen-misc.ps1` | `towel_squat_row`, `warmup_ankle_circles`, `warmup_wrist_circles` |

Si se toca una animación ya aprobada, conviene comprobar con `git status` que el
resto de JSON del script no ha cambiado.

`superman.json` no lo genera ninguno de estos scripts: se editó a mano y se
mantiene tal cual.

## Revisar

- `check.ps1 -keys 0,9,18 -outName hoja` — hoja de control con 6 fotogramas de
  una animación en una rejilla.
- `galeria.ps1` / `galeria-propia.ps1` — tiras de 8 fotogramas y una galería
  `index.html` con las imágenes incrustadas. Se sirve con
  `python -m http.server` desde la carpeta temporal que imprimen.

## Reglas del formato (importante)

- **Nada de `rx`/`ry`/`rz` en las capas.** Son propiedades 3D: si aparecen en una
  composición 2D (`ddd: 0`), el reproductor Lottie descarta la capa entera y la
  animación se ve vacía o congelada. Ya pasó una vez.
- Las formas se dibujan con coordenadas absolutas del lienzo y el `tr` del grupo
  usa `p = a = pivote`, así que el giro positivo es horario en pantalla.
- El orden del array `layers` va de fondo a frente.
- Lottie es 2D por capas: no se puede girar en 3D, solo rotar, trasladar y
  cambiar de tamaño.
- Los scripts están en ASCII/ANSI sin BOM: PowerShell 5 los lee como ANSI, así
  que hay que evitar acentos en los literales o guardarlos en UTF-8 con BOM.
