@echo off
setlocal EnableExtensions DisableDelayedExpansion

set "ENTREGA=%~1"
if not defined ENTREGA set "ENTREGA=LAB06"

if not "%ENTREGA%"=="%ENTREGA:\=%" goto nombre_invalido
if not "%ENTREGA%"=="%ENTREGA:/=%" goto nombre_invalido
if not "%ENTREGA%"=="%ENTREGA:..=%" goto nombre_invalido

set "RAIZ=%~dp0"
set "DOCUMENTOS=%RAIZ%docs\entregas\%ENTREGA%"
set "CODIGO=%RAIZ%Codigo"
set "SALIDA=%RAIZ%EntreFunciones_%ENTREGA%.zip"
set "TEMP_ID=%RANDOM%_%RANDOM%"
set "PREPARACION=%TEMP%\EntreFunciones_paquete_%ENTREGA%_%TEMP_ID%"
set "ZIP_TEMP=%TEMP%\EntreFunciones_%ENTREGA%_%TEMP_ID%.zip"

if not exist "%DOCUMENTOS%\" (
    echo [ERROR] No existe la entrega "%DOCUMENTOS%".
    exit /b 1
)

if not exist "%CODIGO%\pom.xml" (
    echo [ERROR] No se encontro el proyecto Maven en "%CODIGO%".
    exit /b 1
)

mkdir "%PREPARACION%\Documentos" >nul 2>&1
if errorlevel 1 goto error_preparacion
mkdir "%PREPARACION%\Codigo" >nul 2>&1
if errorlevel 1 goto error_preparacion

echo Preparando documentos de %ENTREGA%...
robocopy "%DOCUMENTOS%" "%PREPARACION%\Documentos" /E /R:1 /W:1 /NFL /NDL /NJH /NJS /NP /XD .git .idea target .codex-* /XF .env .env.* *.local db.properties settings.xml *password* *secret* *credential* *.key *.pem *.pfx *.tmp *.temp *.bak *.log *.class ~* >nul
if errorlevel 8 goto error_copia

echo Preparando codigo fuente...
robocopy "%CODIGO%" "%PREPARACION%\Codigo" /E /R:1 /W:1 /NFL /NDL /NJH /NJS /NP /XD .git .idea target .codex-* /XF .env .env.* *.local db.properties settings.xml *password* *secret* *credential* *.key *.pem *.pfx *.tmp *.temp *.bak *.log *.class ~* >nul
if errorlevel 8 goto error_copia

echo Comprimiendo paquete limpio...
powershell -NoLogo -NoProfile -NonInteractive -Command "$ErrorActionPreference='Stop'; Compress-Archive -LiteralPath '%PREPARACION%\Documentos','%PREPARACION%\Codigo' -DestinationPath '%ZIP_TEMP%' -CompressionLevel Optimal"
if errorlevel 1 goto error_zip

powershell -NoLogo -NoProfile -NonInteractive -Command "$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.IO.Compression.FileSystem; $z=[IO.Compression.ZipFile]::OpenRead('%ZIP_TEMP%'); try { $bad=@($z.Entries | Where-Object { $_.FullName -match '(^|[\/\\])(\.git|\.idea|target|\.codex-[^\/\\]*)([\/\\]|$)' -or $_.Name -match '(^\.env(\..*)?$|\.local$|^db\.properties$|^settings\.xml$|password|secret|credential|\.(key|pem|pfx|tmp|temp|bak|log|class)$|^~)' }); $dup=@($z.Entries | Group-Object FullName | Where-Object Count -gt 1); if($bad.Count -or $dup.Count) { throw 'El ZIP contiene rutas excluidas o duplicadas.' } } finally { $z.Dispose() }"
if errorlevel 1 goto error_validacion

powershell -NoLogo -NoProfile -NonInteractive -Command "$ErrorActionPreference='Stop'; Move-Item -LiteralPath '%ZIP_TEMP%' -Destination '%SALIDA%' -Force"
if errorlevel 1 goto error_salida

call :limpiar
echo.
echo ZIP generado correctamente:
echo %SALIDA%
exit /b 0

:nombre_invalido
echo [ERROR] Usa solo el nombre de una carpeta de docs\entregas, por ejemplo LAB06.
exit /b 1

:error_preparacion
echo [ERROR] No se pudo crear el area temporal de trabajo.
goto fallo

:error_copia
echo [ERROR] No se pudieron copiar todos los archivos permitidos.
goto fallo

:error_zip
echo [ERROR] No se pudo crear el ZIP temporal.
goto fallo

:error_validacion
echo [ERROR] El ZIP no supero la validacion de seguridad.
goto fallo

:error_salida
echo [ERROR] No se pudo guardar el ZIP final.
goto fallo

:fallo
call :limpiar
exit /b 1

:limpiar
if exist "%ZIP_TEMP%" del /q "%ZIP_TEMP%" >nul 2>&1
powershell -NoLogo -NoProfile -NonInteractive -Command "$p=[IO.Path]::GetFullPath('%PREPARACION%'); $t=[IO.Path]::GetFullPath([IO.Path]::GetTempPath()); if($p.StartsWith($t,[StringComparison]::OrdinalIgnoreCase) -and [IO.Path]::GetFileName($p) -like 'EntreFunciones_paquete_*') { Remove-Item -LiteralPath $p -Recurse -Force -ErrorAction SilentlyContinue } else { throw 'Ruta temporal no segura.' }"
exit /b 0
