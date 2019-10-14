# 知识库服务
猪齿鱼知识管理是一种内容管理工具，具有创建、编辑、导航、链接、搜索等功能，可以与猪齿鱼其他服务结合共同来帮助企业做好协助管理工作。

## 特性
- `文档管理 `：可以快速操作文档，并支持文档上传附件、评论、记录操作日志等功能，支持word导入及pdf导出

- `版本管理 `：记录用户每次保存文档的版本记录，并支持版本回滚、比较、自动保存等

- `树状文档结构 `：文档结构以树的形式进行展示，不限制层级，可以无限向下创建子级。通过树形的展示方式，可以查看你所在的项目下的所有文档，包括文档之间的父子关系

- `全文检索`：根据关键词对搜索结果进行权重排序

- `操作简单快速 `

## 应用场景

* `知识沉淀`——沉淀软件开发过程中的需求、设计、规范等知识文档。

* `项目协同`——有效管理项目中的计划安排，会议记录等，加强项目成员之间的合作。

* `产品文档`——便捷地编写软件产品的概念说明、用户手册、快速入门等产品文档。

* `培训教材`——方便地编写软件功能使用等培训材料，甚至视频教程等。

## 环境需求

- Java8
- mysql 5.6+
- 该项目是一个 Eureka Client 项目启动后需要注册到 `EurekaServer`，本地环境需要 `eureka-server`，线上环境需要使用 `go-register-server`
- ElasticSearch7.0

## 服务配置
- `application.yml`
 ```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/knowledgebase_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true
    username: choerodon
    password: 123456
  servlet: #设置上传文件最大为30M
    multipart:
      max-file-size: 30MB
      max-request-size: 30MB
choerodon:
  saga:
    consumer:
      thread-num: 5 # saga消息消费线程池大小
      max-poll-size: 200 # 每次拉取消息最大数量
      enabled: false # 关闭消费端
      poll-interval-ms: 1000 # 拉取间隔，默认1000毫秒
  schedule:
    consumer:
      enabled: false # 关闭任务调度消费端
      thread-num: 1 # 任务调度消费线程数
      poll-interval-ms: 1000 # 拉取间隔，默认1000毫秒
services:
  attachment:
    url: http://minio.example.com/knowledgebase-service/
feign:
  hystrix:
    shareSecurityContext: true
    command:
      default:
        execution:
          isolation:
            thread:
              timeoutInMilliseconds: 10000
ribbon:
  ConnectTimeout: 5000
  ReadTimeout: 5000
eureka:
  instance:
    preferIpAddress: true
    leaseRenewalIntervalInSeconds: 10
    leaseExpirationDurationInSeconds: 30
  client:
    serviceUrl:
      defaultZone: ${EUREKA_DEFAULT_ZONE:http://localhost:8000/eureka/}
    registryFetchIntervalSeconds: 10
mybatis:
  mapperLocations: classpath*:/mapper/*.xml
  configuration: # 数据库下划线转驼峰配置
    mapUnderscoreToCamelCase: true
elasticsearch:
  ip: 127.0.0.1:9200
```

- `bootstrap.yml`

  ```yaml
  server:
    port: 8280
  spring:
    application:
      name: knowledgebase-service
    mvc:
      static-path-pattern: /**
    resources:
      static-locations: classpath:/static,classpath:/public,classpath:/resources,classpath:/META-INF/resources,file:/dist
    cloud:
      config:
        failFast: true
        retry:
          maxAttempts: 6
          multiplier: 1.5
          maxInterval: 2000
        uri: localhost:8010
        enabled: false
  management:
    server:
      port: 8281
    endpoints:
      web:
        exposure:
          include: '*'
      health:
        show-details: "ALWAYS"
  ```
## 安装和启动
- 运行 `eureka-server`，[代码在这里](https://code.choerodon.com.cn/choerodon-framework/eureka-server.git)。

- 拉取当前项目到本地

  ```shell
  git clone https://code.choerodon.com.cn/choerodon-agile/knowledgebase-service.git
  ```

- 初始化数据库，本地创建 `knowledgebase_service` 数据表，代码如下：


```sql
CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
CREATE DATABASE knowledgebase_service DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON knowledgebase_service.* TO choerodon@'%';
FLUSH PRIVILEGES;
```
- 初始化 `knowledgebase_service` 数据表数据，运行项目根目录下的 `init-local-database.sh`，该脚本默认初始化数据库的地址为 `localhost`，若有变更需要修改脚本文件

  ```sh
  sh init-local-database.sh
  ```

- 启动项目，项目根目录下运行 `mvn clean spring-boot:run` 命令，或者在本地集成环境中运行 `SpringBoot` 启动类 `/src/main/java/io/choerodon/kb/KnowledgeBaseServiceApplication.java`

## 报告问题
如果你发现任何缺陷或者bugs，请在[issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md)上面描述并提交给我们。

## 贡献
我们十分欢迎您的参与！ [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) 去获得更多关于提交贡献的信息。
