#! /bin/bash
#this script will tell labrador querry the index

. labradorinitenv.sh

#command the client to do your bidding
java -classpath $DEPENDENCIES labrador.management.commandline.ManagementExecutable $1

. labradoruninitenv.sh
