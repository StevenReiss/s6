#! /bin/bash
#this script will tell labrador to stop the server

. labradorinitenv.sh

#tell the server to shutdown
java -classpath $DEPENDENCIES labrador.server.commandline.ServerExecutable -k

. labradoruninitenv.sh


















