#! /bin/bash
#this script will tell labrador to synchronize now

. labradorinitenv.sh

#synchronize
java -d64 -Xmx8000m -classpath $DEPENDENCIES labrador.management.commandline.ManagementExecutable -i

. labradoruninitenv.sh
