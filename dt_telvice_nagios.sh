#!/bin/sh
NAGIOS_SERVER=nagios-acc.khh.hu
NAGIOS_PORT=5667
NAGIOS_BIN=/opt/nsca/send_nsca
NAGIOS_CFG=/opt/nsca/send_nsca.cfg

echo $* | $NAGIOS_BIN -H $NAGIOS_SERVER -p $NAGIOS_PORT -c $NAGIOS_CFG -d '#'