# Referral-System
DistributedRPCFramework is a high-performance, c++-based open-source RPC framework.  
See the [中文文档](https://github.com/cmdmspaint/Referral-System/edit/main/README-zh.md) for Chinese readme.
# Architecture
![image](https://user-images.githubusercontent.com/50624748/171139236-a3581c77-6589-427f-99dd-2c27b4c68555.png)

![image](https://user-images.githubusercontent.com/50624748/171136983-19564ae7-def1-4a9c-9c2f-d21e5564afcd.png)

# Components
ElasticSearch Kibana Logstach mysql spark

# How to Build
git clone git@github.com:cmdmspaint/DistributedRPCFramework.git  
cd build  
cmake ..  
make  
cd ../bin  
./provider -i config.conf  
./consumer -i config.conf  
# How to Use
First write the proto file to generate the corresponding .cc and .h files You can refer to the proto file in my example directory  
change the configuration information in the bin directory to your own server ip and port  
Then override the virtual functions in the base class  



