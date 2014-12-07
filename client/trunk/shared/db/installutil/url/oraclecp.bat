@echo off
set DBROOTDIR=
for /F "tokens=3 delims=	" %%A in ('REG QUERY "HKLM\SOFTWARE\Oracle\Berkeley DB\4.7.25" /v RootDirectory') do set DBROOTDIR=%%A
if ERRORLEVEL 2 goto MISSING
if not defined DBROOTDIR goto MISSING
set FN="%DBROOTDIR%dbvars.bat"
if not exist %FN% goto NOTFOUND
cmd /k "%DBROOTDIR%dbvars.bat"
goto END
:NOTFOUND
echo
echo  Error: The program does not appear to be installed.
echo
cmd /k
goto END
:MISSING
echo
echo NOTE:
echo   The Berkeley DB version could not be determined.
echo   If you are running on Windows 2000, make sure the
echo   REG.EXE program is installed from the Tools disk
echo
cmd /k
:END
