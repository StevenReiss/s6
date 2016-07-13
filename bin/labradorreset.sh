#! /bin/bash
#this script will do its level best to blank everything about labrador to give you a clean slate

. labradorinitenv.sh

java -classpath $DEPENDENCIES labrador.util.ResetExecutable

. labradoruninitenv.sh
