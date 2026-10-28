# Normas de entrega de FitCasa

## Los cambios en la app se publican

Cualquier cambio que afecte a la app (código, assets, animaciones, textos) se
entrega en tres pasos, no solo en dos:

1. Compilar el APK de depuración:
   `gradlew.bat assembleDebug` y copiarlo a `FitCasa-debug.apk` en la raíz.
2. Commit y push a `origin/master`.
3. **Publicar una release en GitHub** con el APK adjunto:

   ```powershell
   gh release create vX.Y.Z .\FitCasa-debug.apk `
     --title "FitCasa vX.Y.Z" --notes-file notas.md --target master
   ```

   - Nombre del tag: `vX.Y.Z`, en orden creciente (`v0.1.2` → `v0.2.0`).
   - Título: `FitCasa vX.Y.Z`.
   - Cuerpo: "Novedades respecto a vANTERIOR" con viñetas y el recordatorio de
     que el APK va firmado en modo depuración.
   - El asset se llama siempre `FitCasa-debug.apk`.

Una release no sustituye al push, y un push no sustituye a la release: las dos
cosas van juntas.

## Limpieza

- Nada de ficheros temporales en el repo ni en la app: capturas, hojas de
  prueba, `.bak`, PNG sueltos o scripts de usar y tirar se borran al terminar.
- Los artefactos de build y el APK están en `.gitignore`; no se versionan.
- El generador de las animaciones propias vive en `tools/lottie/` (ver su
  README), no en carpetas temporales.
