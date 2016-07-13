#! /bin/bash
#this script will set up labrador's enviorment variables

export LABRADOR_HOME="$BROWN_S6_S6/lib/Labrador"
export LIB="$LABRADOR_HOME/lib"
export RESOURCES="$LABRADOR_HOME/resources"

LIBFILES=$(ls $LIB)
for FILE in $LIBFILES; do
      JARLIST="$JARLIST:$LIB/$FILE"
done

export DEPENDENCIES="$RESOURCES/:$LABRADOR_HOME/:$LABRADOR_HOME/labrador.jar$JARLIST"

echo "resources.resources=$RESOURCES" > $LABRADOR_HOME/resources.properties
echo "resources.codefilemapping=$LABRADOR_HOME/codefile.cpm.xml" >> $LABRADOR_HOME/resources.properties
