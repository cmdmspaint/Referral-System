# 智能推荐系统架构图
![image](https://user-images.githubusercontent.com/50624748/171990563-fdb23583-09e7-4a42-9839-952558343557.png)
# 推荐模块架构图
![image](https://user-images.githubusercontent.com/50624748/172138628-3ef28db9-cf00-43a4-b673-aca8eca0c047.png)
# 项目介绍
基于elasticsearch自定义分词器，构建高匹配度搜素服务.使用logstash-input-jdbc实现全量索引构建canal实现elasticsearch异步增量更新.通过构建含有相关性语义的智能理解的搜索模型，通过离线的召回算法模型和在线的排序算法模型，实现个性化推荐.
# 环境参数
后端业务：Java 1.8、 Spring Boot 2.1.6  
后端存储：MySQL 5.6、MyBatis 3.4.5  
搜索系统：Elasticsearch 7.3.0、Logstash 7.3.0、canal 1.1.3  
推荐系统：Spark MLlib 2.4.4  
# 客户端效果图展示
static/index.html  
![image](https://user-images.githubusercontent.com/50624748/172139584-79678609-18a6-404c-be00-0189b8404c74.png)
# 后台效果图展示
static/index.html  
![image](https://user-images.githubusercontent.com/50624748/172139887-3732f34b-846b-43fd-8844-1fe8cfb9d7b7.png)
![image](https://user-images.githubusercontent.com/50624748/172139909-df57320c-609c-46c9-9e0b-83d1710659ae.png)
![image](https://user-images.githubusercontent.com/50624748/172139934-9ceebccb-1782-4a76-a165-8bd7c73ef5ad.png)
![image](https://user-images.githubusercontent.com/50624748/172140097-979c871a-722e-44df-9d68-90edc00f1362.png)
