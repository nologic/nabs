;Written by Mikhail Sosonkin

!define CUSTOMER "gatekeeper"
!define PRODUCT_NAME "Network Abuse Detection System"
!define SHORT_PRODUCT_NAME "NABS"
!define LICENSE "lic.txt"

!define SENSOR_MODULE "NABFlow"
!define SENSOR_IP "127.0.0.1"
!define RECEPTOR_IP "127.0.0.1"

!define CUSTOM_MODULE_SET

!define MOD_NABFLOW
!define MOD_NABFLOWV2
!define MOD_STREAM_STATUS
!define MOD_PIE_CHART
!define MOD_HOST_DETAILS
!define MOD_LOSSY_HISTOGRAM
!define MOD_CUSTOMER_FEEDBACK
!define MOD_NETWORK_POLICY
!define MOD_NETWORK_STATUS
!define MOD_LIBB_JFREECHART
!define MOD_LIBB_JCOMMON

!define STARTUP_MODULES ""

!define CUSTOM_CONSOLE
!define CONSOLE_GATEKEEPER

!addincludedir "..\base"
!include "eunomia.nsi"
