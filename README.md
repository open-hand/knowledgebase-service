# Knowledgebase_Service
Choerodon knowledge management is a content management tool with create, edit, navigation, link, search and other functions,can be combined with other services to help enterprises to do a good job in assisting management.


## Feature
- `Document Management `：Fast document operation, and support document upload attachments, comments, record operation log and other functions

- `Version Management `：Record the version record saved by the user every time, and support version rollback, comparison and deletion

- `Tree Document Management `：The document structure is presented in the form of a tree, with no limit on the hierarchy and unlimited creation of sub-levels. Through the tree presentation, you can view all the documents under your project, including the parent-child relationship between the documents

- `Simple and fast operation `

## Application scenarios

* `Knowledge of precipitation` —— Precipitation software development process requirements, design, specifications and other knowledge documents。

* `Project Coordination`—— Effectively manage project scheduling, meeting minutes, etc., and enhance cooperation among project members.

* `Product Documentation`—— Easy to write software product concept description, user manual, quick start product documentation。

* `Training Materials`—— Easy to write software function use and other training materials, even video tutorials.

## Environmental require

- Java8
- mysql 5.6+
- The project is a Eureka Client so need to register to `EurekaServer` after the project starts ，Local environment needs `eureka-server`，Online environment depend `go-register-server`
- ElasticSearch7.0

## Service Config
- `application.yml`
 ```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/knowledgebase_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true
    username: choerodon
    password: 123456
  servlet: #Set the maximum size of the uploaded file to 30M
    multipart:
      max-file-size: 30MB
      max-request-size: 30MB
choerodon:
  saga:
      consumer:
        thread-num: 5 # saga Message consumption thread pool size
        max-poll-size: 200 # Maximum number of messages per pull
        enabled: false # Stop consumer
        poll-interval-ms: 1000 # Pull interval, default 1000 ms
    schedule:
      consumer:
        enabled: false # Stop the task scheduling consumer
        thread-num: 1 # Task scheduling consumes the number of threads
        poll-interval-ms: 1000 # Pull interval, default 1000 ms
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
  configuration: #Database underline to camel case configuration
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
## Installation and startup steps
- Run `eureka-server`，[Coding is here](https://code.choerodon.com.cn/choerodon-framework/eureka-server.git)。

- Pull the current project to the loca

  ```shell
  git clone https://code.choerodon.com.cn/choerodon-agile/knowledgebase-service.git
  ```


- Create a database named `knowledgebase_service` in the Mysql database


```sql
CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
CREATE DATABASE knowledgebase_service DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON knowledgebase_service.* TO choerodon@'%';
FLUSH PRIVILEGES;
```


- Create `init-local-database.sh` data initialization script file in `knowledgebase_service` project root directory


- Execute the database initialization script

```sh
sh init-local-database.sh
```

- Startup `knowledgebase_service` project，run the cmd ：

```sh
mvn spring-boot:run
```

>Or in a local integration environment run the  `SpringBoot` Startup class
 `/src/main/java/io/choerodon/buzz/KnowledgeBaseServiceApplication.java`



## Report Problems
If you find any defects or bugs，Please describe it on[issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md)and submit it to us.

## How to Contribute
Push requests are welcome! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.
