@echo off
setlocal

:: Configurar variables
set APP_NAME=QRCreator - Almonte
set INPUT_DIR=launcher
set MAIN_JAR=QRCreator-1.0.jar
set MAIN_CLASS=junkier.qrcreator.app.App
set MODULE_PATH=launcher/javafx
set MODULES=java.base,java.desktop,javafx.controls,javafx.fxml,javafx.swing,javafx.graphics
set OUTPUT_DIR=dist
set ICON_WIN=launcher\Almonte.ico

:: Generar instalador para Windows (.exe)
echo Generando instalador para Windows...
jpackage ^
    --name "%APP_NAME%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --type exe ^
    --module-path "%MODULE_PATH%" ^
    --add-modules "%MODULES%" ^
    --dest "%OUTPUT_DIR%" ^
    --app-version "1.0" ^
    --vendor "Ayuntamiento de Almonte" ^
    --icon "%ICON_WIN%" ^
    --copyright "© 2025 Ayuntamiento de Almonte - Informatica Alcaldia. Todos los derechos reservados." ^
    --description "Proyecto QCreator" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "Ayuntamiento" ^
    --win-dir-chooser ^
    --win-upgrade-uuid "dcb86de9-54e2-46db-af24-95796dea9ee9"

echo.
echo Paquetes creados en %OUTPUT_DIR%
pause
endlocal
