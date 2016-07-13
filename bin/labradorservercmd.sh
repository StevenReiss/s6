#! /bin/bash
#this script will tell labrador querry the index

. labradorinitenv.sh

#command the server to do your bidding
java -classpath $DEPENDENCIES labrador.server.commandline.ServerExecutable $1

. labradoruninitenv.sh
