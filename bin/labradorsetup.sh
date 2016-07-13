#! /bin/bash
#this script will tell labrador to monitor all the files

export FILES_TO_MONITOR="/map/aux0fred/javasrc1.6 /map/aux0fred/sol8src /map/aux0fred/unixsrc /map/aux0fred/open	/pro/kona /pro/tea /pro/java /pro/bloom /pro/veld /pro/ivy /pro/clime /pro/taiga /pro/wadi /pro/bubbles /pro/s6 /home/spr/sampler /home/spr/solar /home/spr/crawler"
#setenv FILES_TO_MONITOR "/u/jtwebb/TestSrc/ /pro/ivy/"

. labradorinitenv.sh

#add the directories
java -d64 -Xmx4000m -classpath $DEPENDENCIES labrador.management.commandline.ManagementExecutable -m $FILES_TO_MONITOR

. labradoruninitenv.sh

unset FILES_TO_MONITOR
