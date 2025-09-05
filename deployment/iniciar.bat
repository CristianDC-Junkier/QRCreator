@echo off
setlocal EnableDelayedExpansion

:: Configurar variables
set JAR_NAME=launcher/QRCreator-1.0-jar-with-dependencies.jar
set MAIN_CLASS=junkier.qrcreator.app.App
set JAVAFX_DIR=launcher/javafx
set MODULES=javafx.controls,javafx.fxml,javafx.swing

:: Verificar que el JAR existe
if not exist "%JAR_NAME%" (
    echo Error: No se encontró %JAR_NAME%.
    echo Asegúrate de haber construido el proyecto antes de ejecutar este script.
    pause
    exit /b 1
)

:: Ejecutar la aplicación
java ^
    --module-path "%JAVAFX_DIR%" ^
    --add-modules %MODULES% ^
    -cp "%JAR_NAME%" ^
    %MAIN_CLASS%

:: Mantener la ventana abierta
pause
endlocal
