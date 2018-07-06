#!/usr/bin/env bash

WAS_APPSERVER_HOME=/opt/IBM/WebSphere/AppServer/
WAS_ENDPT_PROP=endptEnabler.properties
WAS_ENDPT_EAR=SimpleSoapJmsServer.ear
WAS_ENDPT_EAR_TEMPDIR=${WAS_ENDPT_EAR}_temp

${WAS_APPSERVER_HOME}/bin/endptEnabler.sh -v -properties ${WAS_ENDPT_PROP} ${WAS_ENDPT_EAR}

## post processing to update the JMSRouter Container transaction (since UMRA does not support it)
echo "Starting Post processing to update the JMSRouter Transaction Type to Bean"

## create backup first
echo "First, let's backup ${WAS_ENDPT_EAR} to ${WAS_ENDPT_EAR}.bak"
cp -prf ${WAS_ENDPT_EAR} ${WAS_ENDPT_EAR}.bak

#create working dir and work in it
mkdir ${WAS_ENDPT_EAR_TEMPDIR}

cp ${WAS_ENDPT_EAR} ${WAS_ENDPT_EAR_TEMPDIR}
cd ${WAS_ENDPT_EAR_TEMPDIR}

## expand ear
jar -xf ${WAS_ENDPT_EAR}

## expand JMS router
jar -xf SimpleSoapJmsServer-ejb_JMSRouter.jar

## replace transaction type
echo "Updating JMSRouter ejb-jar.xml file with transaction-type=Bean"
sed -i 's/<transaction-type>Container<\/transaction-type>/<transaction-type>Bean<\/transaction-type>/g' META-INF/ejb-jar.xml
echo "Done Updating JMSRouter ejb-jar.xml file..."

##TODO: Add the missing ConnectionFactory too...
## In META-INF/ibm-ejb-jar-bnd.xml, add within <message-driven name="WebServicesJMSRouter">
## <resource-env-ref name="jms/WebServicesReplyQCF" binding-name="SimpleJmsSendConnectionFactory" />

## repackage
echo "Repackaging the EAR..."

jar -uf SimpleSoapJmsServer-ejb_JMSRouter.jar META-INF/ejb-jar.xml

jar -uf ${WAS_ENDPT_EAR} SimpleSoapJmsServer-ejb_JMSRouter.jar

echo "Done Repackaging the EAR..."

#move final ear back
mv -f ${WAS_ENDPT_EAR} ../

#delete working dir
cd ..
rm -Rf ${WAS_ENDPT_EAR_TEMPDIR}

echo "Done!! ${WAS_ENDPT_EAR} is updated."