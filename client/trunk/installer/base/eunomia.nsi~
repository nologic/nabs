;Written by Mikhail Sosonkin

; Global Defines.
!ifndef CUSTOMER
!define CUSTOMER "Base"
!endif

!ifndef PRODUCT_NAME
!define PRODUCT_NAME "Network Abuse Detector"
!endif

!ifndef SHORT_PRODUCT_NAME
!define SHORT_PRODUCT_NAME "NABS"
!endif

!ifndef LICENSE
!define LICENSE "lgpl.txt"
!endif


!ifndef SENSOR_NAME
!define SENSOR_NAME "RemoteSensor"
!endif

!ifndef SENSOR_MODULE
!define SENSOR_MODULE "NABFlow"
!endif

!ifndef SENSOR_IP
!define SENSOR_IP "128.238.35.91"
!endif

!ifndef SENSOR_PORT
!define SENSOR_PORT "1986"
!endif

!ifndef RECEPTOR_NAME
!define RECEPTOR_NAME "Sieve"
!endif

!ifndef RECEPTOR_IP
!define RECEPTOR_IP "128.238.35.91"
!endif

!ifndef RECEPTOR_PORT
!define RECEPTOR_PORT "4185"
!endif

!ifndef STARTUP_MODULES
!define STARTUP_MODULES "lossyHistogram, pieChart"
!endif

; Module Definitions.
!ifndef CUSTOM_MODULE_SET
!define MOD_NABFLOW
!define MOD_NABFLOWV2
!define MOD_STREAM_STATUS
!define MOD_LOSSY_HISTOGRAM
!define MOD_PIE_CHART
!define MOD_HOST_VIEW
!define MOD_HOST_DETAILS
!define MOD_RECORD_COUNTER
!define MOD_NABFLOW_COLLECTOR
!define MOD_NETWORK_POLICY
!define MOD_NETWORK_STATUS
!define MOD_ATAS
!define MOD_NABFLOWV2_COLLECTOR
!define MOD_CUSTOMER_FEEDBACK
!define MOD_LIBB_JFREECHART
!define MOD_LIBB_JCOMMON

!define DB_MYSQL
!define DB_POSTGRESQL
!endif

; Console Configuration
!ifndef CUSTOM_CONSOLE
!define CONSOLE_BASE

;Other Consoles
;!define CONSOLE_GATEKEEPER
!endif


;--------------------------------
;Include Modern UI

  !include "MUI.nsh"
  !include Sections.nsh
  !include 'TextFunc.nsh'
  !include 'FileFunc.nsh'
  
  SetCompress auto

;--------------------------------
;General

  !cd "../base/"

  ;Name and file
  Name "${PRODUCT_NAME}"
  BrandingText "Vivic Networks"
  Caption "Vivic Networks software installation"

  !define /date MyTIMESTAMP "%Y%m%d"
  OutFile "..\build\nabs-${MyTIMESTAMP}-${CUSTOMER}.exe"

  XPStyle on
  CRCCheck on
  ShowInstDetails show
  ShowUninstDetails show

  ;Default installation folder
  InstallDir "$PROGRAMFILES\Vivic\${SHORT_PRODUCT_NAME}"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\Vivic\${SHORT_PRODUCT_NAME}" ""

  InstType "Full"
  InstType "Console"
  InstType "Minimal Console"

;--------------------------------
;Variables

  Var STARTMENU_FOLDER


;--------------------------------
;Interface Settings

  !define MUI_LICENSEPAGE_RADIOBUTTONS
  !define MUI_ABORTWARNING

  !Define MODULE_DIR "$INSTDIR\Modules"
  !Define SENSOR_INI "sensor.ini"
  !Define RECEPTOR_INI "receptor.ini"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "../${CUSTOMER}/${LICENSE}"
  !insertmacro MUI_PAGE_COMPONENTS
  Page custom ShowCustom
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"
 
  ReserveFile ${SENSOR_INI}
  ReserveFile ${RECEPTOR_INI}
  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

;--------------------------------
;Installer Sections
Section ""
  SectionIn 1 2 3
  ;Store installation folder
  WriteRegStr HKCU "Software\Vivic\${SHORT_PRODUCT_NAME}" "" $INSTDIR

  SetOutPath "$INSTDIR"
  !Define STARTMENU_FOLDER "Vivic"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ;Make config files.
  call WriteFiles

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END  
SectionEnd

Section "" ;Modules
  SectionIn 1 2 3
  SetOutPath "$INSTDIR\modules"
  FileOpen $0 "$INSTDIR\modules.nab" "w"

  !ifdef MOD_NABFLOW
  File ..\..\modules\NABFlow\dist\NABFlow.jar
  FileWrite $0 "modules\NABFlow.jar$\r$\n"
  !endif

  !ifdef MOD_NABFLOWV2
  File ..\..\modules\NABFlowV2\dist\NABFlowV2.jar
  FileWrite $0 "modules\NABFlowV2.jar$\r$\n"
  !endif

  !ifdef MOD_STREAM_STATUS
  File ..\..\modules\streamStatus\dist\streamStatus.jar
  FileWrite $0 "modules\streamStatus.jar$\r$\n"
  !endif

  !ifdef MOD_LOSSY_HISTOGRAM
  File ..\..\modules\lossyHistogram\dist\lossyHistogram.jar
  FileWrite $0 "modules\lossyHistogram.jar$\r$\n"
  !endif

  !ifdef MOD_PIE_CHART
  File ..\..\modules\pieChart\dist\pieChart.jar
  FileWrite $0 "modules\pieChart.jar$\r$\n"
  !endif

  !ifdef MOD_HOST_VIEW
  File ..\..\modules\hostView\dist\hostView.jar
  FileWrite $0 "modules\hostView.jar$\r$\n"
  !endif

  !ifdef MOD_HOST_DETAILS
  File ..\..\modules\hostDetails\dist\hostDetails.jar
  FileWrite $0 "modules\hostDetails.jar$\r$\n"
  !endif

  !ifdef MOD_RECORD_COUNTER
  File ..\..\modules\RecordCounter\dist\RecordCounter.jar 
  FileWrite $0 "modules\RecordCounter.jar$\r$\n"
  !endif

  !ifdef MOD_NABFLOW_COLLECTOR
  File ..\..\modules\NABFlowCollector\dist\NABFlowCollector.jar
  FileWrite $0 "modules\NABFlowCollector.jar$\r$\n"
  !endif

  !ifdef MOD_NETWORK_POLICY
  File ..\..\modules\NetworkPolicy\dist\NetworkPolicy.jar
  FileWrite $0 "modules\NetworkPolicy.jar$\r$\n"
  !endif

  !ifdef MOD_NETWORK_STATUS
  File ..\..\modules\NetworkStatus\dist\NetworkStatus.jar
  FileWrite $0 "modules\NetworkStatus.jar$\r$\n"
  !endif

  !ifdef MOD_ATAS
  File ..\..\modules\Atas\dist\Atas.jar
  FileWrite $0 "modules\Atas.jar$\r$\n"
  !endif

  !ifdef MOD_NABFLOWV2_COLLECTOR
  File ..\..\modules\NABFlowV2Collector\dist\NABFlowV2Collector.jar
  FileWrite $0 "modules\NABFlowV2Collector.jar$\r$\n"
  !endif

  !ifdef MOD_CUSTOMER_FEEDBACK
  File ..\..\modules\CustomerFeedback\dist\CustomerFeedback.jar
  FileWrite $0 "modules\CustomerFeedback.jar$\r$\n"
  !endif

  !ifdef MOD_LIBB_JFREECHART
  FILE ..\..\shared\libraries\jfreechart-1.0.5.jar
  FileWrite $0 "modules\jfreechart-1.0.5.jar$\r$\n"
  !endif

  !ifdef MOD_LIBB_JCOMMON
  FILE ..\..\shared\libraries\jcommon-1.0.10.jar 
  FileWrite $0 "modules\jcommon-1.0.10.jar$\r$\n"
  !endif

  FileClose $0
SectionEnd

Section "JRE 1.6" SecJRE
  SectionIn 1 2
  SetOutPath "$INSTDIR"
  
  File /r ..\jre
  
SectionEnd

Section "Sieve Console" SecConsole
  SectionIn 1 2 3
  SetOutPath "$INSTDIR"

  SetOutPath "$INSTDIR\console"

  FileOpen $0 "$INSTDIR\Console.cmd" "w"
  FileWrite $0 "echo off$\r$\n$\r$\n"
  FileWrite $0 "PATH=.\jre\bin;%PATH%$\r$\n"
  FileWrite $0 "cd console$\r$\n$\r$\n"
  FileWrite $0 "start javaw -Xmx256M "
  
  !ifdef CONSOLE_BASE
  File /r ..\..\front-end(gui)\dist\*

  FileWrite $0 "-Dswing.metalTheme=steel -jar NabsClient_GUI_.jar"
  !endif

  !ifdef CONSOLE_GATEKEEPER
  File /r ..\..\Consoles\GateKeeper\dist\*

  FileWrite $0 "-jar GateKeeper.jar"
  !endif

  FileWrite $0 "$\r$\nexit"
  FileClose $0

  SetOutPath "$INSTDIR"
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Console.lnk" "$INSTDIR\console.cmd"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section "Sieve Engine" SecSieve
  SectionIn 1
  SetOutPath "$INSTDIR"
  File Sieve.cmd
  File StartAll.cmd
  File config.nab

  SetOutPath "$INSTDIR\db_drivers"
  FileOpen $0 "$INSTDIR\dbdrivers.nab" "w"
  !ifdef DB_MYSQL
  File ..\..\shared\libraries\mysql-connector-java-3.1.10-bin.jar
  FileWrite $0 "mysql com.mysql.jdbc.Driver db_drivers/mysql-connector-java-3.1.10-bin.jar$\r$\n"
  !endif

  !ifdef DB_POSTGRESQL
  File ..\..\shared\libraries\postgresql-8.2-504.jdbc3.jar
  FileWrite $0 "postgresql org.postgresql.Driver db_drivers/postgresql-8.2-504.jdbc3.jar$\r$\n"
  !endif
  FileClose $0

  SetOutPath "$INSTDIR\sieve"
  File /r ..\..\middleware\dist\*

  SetOutPath "$INSTDIR"
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Sieve.lnk" "$INSTDIR\sieve.cmd"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Start All.lnk" "$INSTDIR\startall.cmd"
  !insertmacro MUI_STARTMENU_WRITE_END  
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecSieve ${LANG_ENGLISH} "Seive Engine files"
  LangString DESC_SecConsole ${LANG_ENGLISH} "Seive Console files"
  LangString DESC_SecJRE ${LANG_ENGLISH} "Sun Microsystems Java Runtime Environment 1.6"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecSieve} $(DESC_SecSieve)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecConsole} $(DESC_SecConsole)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecJRE} $(DESC_SecJRE)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"
  Delete "$INSTDIR\Sieve.cmd"
  Delete "$INSTDIR\StartAll.cmd"
  Delete "$INSTDIR\Console.cmd"
  Delete "$INSTDIR\config.nab"
  Delete "$INSTDIR\dbdrivers.nab"
  Delete "$INSTDIR\modules.nab"
  Delete "$INSTDIR\console\install.config"

  RMDir /r "$INSTDIR\db_drivers"
  RMDir /r "$INSTDIR\modules"
  RMDir /r "$INSTDIR\sieve"
  RMDir /r "$INSTDIR\console"
  RMDir /r "$INSTDIR\jre"
  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\Vivic\${SHORT_PRODUCT_NAME}"

SectionEnd

Function ShowCustom
  Push $R0
  Push $R1

  WriteINIStr "$PLUGINSDIR\${SENSOR_INI}" "Field 5" "State" "${SENSOR_NAME}"   ; Sensor name
  WriteINIStr "$PLUGINSDIR\${SENSOR_INI}" "Field 6" "State" "${SENSOR_IP}"     ; Sensor Ip
  WriteINIStr "$PLUGINSDIR\${SENSOR_INI}" "Field 7" "State" "${SENSOR_PORT}"   ; Sensor Port
  WriteINIStr "$PLUGINSDIR\${SENSOR_INI}" "Field 8" "State" "${RECEPTOR_PORT}" ; Sensor Sieve Port

  WriteINIStr "$PLUGINSDIR\${RECEPTOR_INI}" "Field 4" "State" "${RECEPTOR_NAME}" ; receptor Name
  WriteINIStr "$PLUGINSDIR\${RECEPTOR_INI}" "Field 5" "State" "${RECEPTOR_IP}"   ; receptor IP
  WriteINIStr "$PLUGINSDIR\${RECEPTOR_INI}" "Field 6" "State" "${RECEPTOR_PORT}" ; receptor Port

  SectionGetFlags ${SecSieve} $R0
  IntOp $R0 $R0 & ${SF_SELECTED}
  StrCmp $R0 ${SF_SELECTED} sieveSelected
  goto noSieve

  sieveSelected:
    !insertmacro MUI_HEADER_TEXT "Configure Sensor Information" "Sensor Configuration"
    !insertmacro MUI_INSTALLOPTIONS_DISPLAY ${SENSOR_INI}
    goto endF

  noSieve:
    SectionGetFlags ${SecConsole} $R0
    IntOp $R0 $R0 & ${SF_SELECTED}
    StrCmp $R0 ${SF_SELECTED} consoleSelected
    goto endF
 
  consoleSelected:
    !insertmacro MUI_HEADER_TEXT "Configure Sieve Information" "Sieve Configuration"
    !insertmacro MUI_INSTALLOPTIONS_DISPLAY ${RECEPTOR_INI}

  endF:
    Pop $R1
    Pop $R0
FunctionEnd

Function WriteFiles
  Push $R0
  Push $R1

  CreateDirectory "$INSTDIR\console"

  ReadINIStr $1 "$PLUGINSDIR\${SENSOR_INI}" "Field 5" "State" ; Sensor name
  ReadINIStr $2 "$PLUGINSDIR\${SENSOR_INI}" "Field 6" "State" ; Sensor Ip
  ReadINIStr $3 "$PLUGINSDIR\${SENSOR_INI}" "Field 7" "State" ; Sensor Port
  ReadINIStr $4 "$PLUGINSDIR\${SENSOR_INI}" "Field 8" "State" ; Sensor Sieve Port

  ReadINIStr $5 "$PLUGINSDIR\${RECEPTOR_INI}" "Field 4" "State" ; receptor Name
  ReadINIStr $6 "$PLUGINSDIR\${RECEPTOR_INI}" "Field 5" "State" ; receptor IP
  ReadINIStr $7 "$PLUGINSDIR\${RECEPTOR_INI}" "Field 6" "State" ; receptor Port

  StrCpy $8 "false" ; do Sensor

  SectionGetFlags ${SecSieve} $R0
  IntOp $R0 $R0 & ${SF_SELECTED}
  StrCmp $R0 ${SF_SELECTED} sieveSelected
  goto noSieve

  sieveSelected:
	StrCpy $5 "Sieve"
	StrCpy $6 "127.0.0.1"
	StrCpy $7 "4185"
        StrCpy $8 "true"

	; Write Sieve config file
  	FileOpen $0 "$INSTDIR\config.nab" "w"
 	FileWrite $0 "Port = $4"
	FileClose $0

  noSieve:
	FileOpen $0 "$INSTDIR\console\install.config" "w"
	FileWrite $0 "Sensor=$8$\r$\n"
	FileWrite $0 "Sensor.IP=$2$\r$\n"
	FileWrite $0 "Sensor.Port=$3$\r$\n"
	FileWrite $0 "Sensor.Name=$1$\r$\n"
	FileWrite $0 "Sensor.Module=${SENSOR_MODULE}$\r$\n"
	FileWrite $0 "Receptor=true$\r$\n"
	FileWrite $0 "Receptor.IP=$6$\r$\n"
	FileWrite $0 "Receptor.Port=$7$\r$\n"
	FileWrite $0 "Receptor.Name=$5$\r$\n"
        FileWrite $0 "Modules=${STARTUP_MODULES}$\r$\n"
	FileClose $0

  Pop $R1
  Pop $R0
FunctionEnd

Function .onInit
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT ${RECEPTOR_INI}
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT ${SENSOR_INI}
FunctionEnd

  !cd "../${CUSTOMER}"
