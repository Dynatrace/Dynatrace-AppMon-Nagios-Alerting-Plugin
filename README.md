# Nagios Alerting Plugin

![images_community/download/attachments/17958239/icon.png](images_community/download/attachments/17958239/icon.png) This plugin allows **sending dynaTrace alerts to a Nagios server** using passive
checks. Alerts can be posted only remotely (when Nagios and dynaTrace is on different hosts) using the NSCA (NSCA daemon on the Nagios side and the NSCA client on the dynaTrace side).

| Name | Nagios Alerting Plugin 
| :--- | :---
| Author |Balazs Bakai (balazs.bakai@telvice.hu)
| Supported dynaTrace Versions | >= 5.5
| Support | [Not Supported](https://community.compuwareapm.com/community/display/DL/Support+Levels#SupportLevels-Community)
| License | LGPL v3
| Release history | 1.0.0 (2014.06.26)
| Download | Plugin binary: [hu.telvice.TelviceNagiosAlerting_1.0.0.jar](hu.telvice.TelviceNagiosAlerting_1.0.0.jar)
| | Plugin script: [dt_telvice_nagios.sh](dt_telvice_nagios.sh)
| | Sample config file : [send_nsca.cfg](send_nsca.cfg)

## Installation

  * On the dynaTrace server side, import the plugin jar file and activate the plugin. Then, you need to add the Nagios Action Plugin in the 'Extended Actions' of each of the incidents you want to generate Nagios alerts for. You also need to install the nsca client (download here for [Linux](http://exchange.nagios.org/directory/Addons/Passive-Checks/NSCA--2D-Nagios-Service-Check-Acceptor/details) or [Win32](http://exchange.nagios.org/directory/Addons/Passive-Checks/NSCA-Win32-Client/details)) on the dynaTrace server. 

  * On the Nagios side, you need to declare the host and the services which you will generate alerts for. The host is arbitrary as you will be able to configure it for each alert you will generate from dynaTrace. The service description on the other hand must match the name of the incident that will trigger the alert. 

**Incident Configuration**

Important to set up the execution list to: **on incident begin and end**, so dynaTrace can inform Nagios when the incidents end.

![images_community/download/attachments/17958239/telvice_nagios1.jpg](images_community/download/attachments/17958239/telvice_nagios1.jpg)

**Plugin Script Location**

Setup dt_telvice_nagios.sh unix script location.  You should copy this script to this location on the dynaTraceServer host.

  
![images_community/download/attachments/17958239/telvice_nagios2.jpg](images_community/download/attachments/17958239/telvice_nagios2.jpg)

**Plugin Script Parameters**

Setup these variables: NAGIOS_SERVER, NAGIOS_PORT, NAGIOS_BIN, NAGIOS_CFG in the dt_telvice_nagios.sh script.

![images_community/download/attachments/17958239/telvice_nagios3.jpg](images_community/download/attachments/17958239/telvice_nagios3.jpg)

**  
**

**Nagios Service/Host configuration**
    
    
    # HOST DEFINITION
    define host {
        use                  linux-server
        host_name            hegarlinux.dynatrace.local       ; MATCH THE HOSTNAME IN THE ALERT RULE DEFINITION
        alias                dynatrace-server
        address              127.0.0.1
    }
    # SERVICE TEMPLATE DEFINITION
    # Template for the service : dynaTrace alerts from the command file
    define service {
            name                       passive_checkservice
            use                        generic-service
            active_checks_enabled      0
            passive_checks_enabled     1
            normal_check_interval      1
            check_period               24x7
            check_interval             1
            retry_interval             1
            }
    # SERVICE DEFINITION
    # Define each dynaTrace alert that we want to be processed by Nagios
    define service {
            use                        passive_checkservice
            host_name                  hegarlinux.dynatrace.local       ; MATCH THE HOST DEFINED EARLIER
            service_description        Warning: LastMinute Search   ; MATCH THE IINCIDENT NAME
            register                   1
            check_command              check_ping             ;not used  but mandatory command
    }

**Nagios Overview**  
![images_community/download/attachments/17958239/Nagios.PNG](images_community/download/attachments/17958239/Nagios.PNG)



