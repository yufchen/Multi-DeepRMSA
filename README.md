# Multi-DeepRMSA
Detailed design & results are in  Multi_DeepRMSA.pptx

## Environment set up
All three controllers’ implementations are based on Java, DRL modules are in Python.

### Java part
This project needs jdk14 to enable several dependencies.

#### Install OpenJDK14 on Ubuntu18.04
```
curl -O https://download.java.net/java/GA/jdk14/076bab302c7b4508975440c56f6cc26a/36/GPL/openjdk-14_linux-x64_bin.tar.gz
tar xvf openjdk-14_linux-x64_bin.tar.gz
sudo mkdir /usr/lib/jvm
sudo cp -r ./jdk-14 /usr/lib/jvm
```
Add env variables to ~/.bashrc
```
sudo gedit ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/jdk-14(exact version)
export JRE_HOME=${JAVA_HOME}/jre  
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib  
export PATH=${JAVA_HOME}/bin:$PATH 
source ~/.bashrc
```

### Python part
See /ONOS_Project/DRL_py/requirements.txt

## Configurations
See /ONOS_Project/conf.cfg

## Run
Startup sequence:
Broker->Broker_DRL->Serverp->Serverp_DRL->Server->Server_DRL
(Serverp means passive)
```
cd somewhere/ONOS_Project
java -jar ./out/artifacts/ONOS_Project_jar/ONOS_Project.jar Broker
java -jar ./out/artifacts/ONOS_Project_jar/ONOS_Project.jar Serverp
java -jar ./out/artifacts/ONOS_Project_jar/ONOS_Project.jar Server
```

