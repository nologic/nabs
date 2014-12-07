;Written by Mikhail Sosonkin

!define CUSTOMER "Army"
!define PRODUCT_NAME "Atas"
!define SHORT_PRODUCT_NAME "atas"
!define LICENSE "lic.txt"

!define SENSOR_MODULE "NABFlowV2"
!define SENSOR_IP "127.0.0.1"
!define RECEPTOR_IP "127.0.0.1"

!define CUSTOM_MODULE_SET

!define MOD_NABFLOW
!define MOD_NABFLOWV2
!define MOD_STREAM_STATUS
!define MOD_PIE_CHART
!define MOD_HOST_DETAILS
!define MOD_ATAS
!define MOD_NABFLOWV2_COLLECTOR

!define DB_MYSQL
!define DB_POSTGRESQL

!define STARTUP_MODULES "pieChart, hostDetails, atas"

!addincludedir "..\base"
!include "eunomia.nsi"
