#! /bin/bash
#this script will tell labrador to start the server and schedule synchronizations

. labradorinitenv.sh

#start the server
java -classpath $DEPENDENCIES labrador.server.commandline.ServerExecutable -s &
#give the server time to boot
sleep 2
#tell the server to synchronize at 1am everyday
java -Xmx1600m -classpath $DEPENDENCIES labrador.server.commandline.ServerExecutable -a standardSynchronization '0 0 1 * * ? *'
#ask the server for its schedule to make sure all is well
java -classpath $DEPENDENCIES labrador.server.commandline.ServerExecutable -l

. labradoruninitenv.sh
