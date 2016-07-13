#! /bin/sh -f
#this script will tell labrador querry the index

. $BROWN_S6_S6/bin/labradorinitenv.sh

#querry!
java -classpath $DEPENDENCIES labrador.management.commandline.ManagementExecutable -s "$1"

. labradoruninitenv.sh
