# Choerodon KnowledgeBase Service

猪齿鱼知识管理是一种内容管理工具，具有创建、编辑、导航、链接、搜索等功能，可以与猪齿鱼其他服务结合共同来帮助企业做好协助管理工作。
                             
## Introduction

## Add Helm chart repository

``` bash    
helm repo add choerodon https://openchart.choerodon.com.cn/choerodon/c7n
helm repo update
```

## Installing the Chart

```bash
$ helm install c7n/knowledgebase-service --name knowledgebase-service
```

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

## Uninstalling the Chart

```bash
$ helm delete knowledgebase-service
```

## Configuration

Parameter | Description	| Default
--- |  ---  |  ---  
`replicaCount` | pod运行数量 | `1`
`image.repository` | 镜像库地址 | `registry.cn-hangzhou.aliyuncs.com/choerodon-agile/knowledgebase-service`
`image.pullPolicy` | 镜像拉取策略 | `IfNotPresent`
`preJob.timeout` | job超时时间 | `300`
`preJob.image` | job镜像库地址 | `registry.cn-hangzhou.aliyuncs.com/choerodon-tools/dbtool:0.6.4`
`preJob.preConfig.enabled`| 是否初始manager_service数据库 | `true`
`preJob.preConfig.configFile` | 初始化到配置中心文件名 | `application.yml`
`preJob.preConfig.configType` | 初始化到配置中心存储方式 | `k8s`
`preJob.preConfig.updatePolicy` | 初始化配置策略: not/add/override/update | `add`
`preJob.preConfig.registerHost` | 注册中心地址 | `http://register-server.c7n-system:8000`
`preJob.preConfig.datasource.url` | manager_service数据库连接地址 | `jdbc:mysql://localhost:3306/manager_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true`
`preJob.preConfig.datasource.username` | manager_service数据库用户名 | `choerodon`
`preJob.preConfig.datasource.password` | manager_service数据库密码 | `password`
`preJob.preInitDB.enabled` | 是否初始knowledgebase_service数据库 | `true`
`preJob.preInitDB.datasource.url` | knowledgebase_service数据库连接地址 | `jdbc:mysql://localhost:3306/knowledgebase_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true`
`preJob.preInitDB.datasource.username` | knowledgebase_service数据库用户名 | `choerodon`
`preJob.preInitDB.datasource.password` | knowledgebase_service数据库密码 | `password`
`metrics.path` | 收集应用的指标数据路径 | `/actuator/prometheus`
`metrics.group` | 性能指标应用分组 | `spring-boot`
`logs.parser` | 日志收集格式 | `spring-boot`
`deployment.managementPort` | 服务管理端口 | `8031`
`ingress.enabled` | 是否创建k8s ingress | `false`
`env.open.SPRING_CLOUD_CONFIG_ENABLED` | 是否启用配置中心 | `true`
`env.open.SPRING_CLOUD_CONFIG_URI` | 配置中心地址 | `http://register-server:8000`
`env.open.SPRING_DATASOURCE_URL` | 数据库连接地址 | `jdbc:mysql://localhost/knowledgebase_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true`
`env.open.SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `choerodon`
`env.open.SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `password`
`env.open.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | 注册服务地址 | `http://register-server.io-choerodon:8000/eureka/`
`service.enabled` | 是否创建k8s service | `false`
`service.type` |  service类型 | `ClusterIP`
`service.port` | service端口 | `8030`
`service.name` | service名称 | `knowledgebase-service`
`resources.limits` | k8s中容器能使用资源的资源最大值 | `3Gi`
`resources.requests` | k8s中容器使用的最小资源需求 | `2Gi`

### SkyWalking Configuration
Parameter | Description
--- |  --- 
`javaagent` | SkyWalking 代理jar包(添加则开启 SkyWalking，删除则关闭)
`skywalking.agent.application_code` | SkyWalking 应用名称
`skywalking.agent.sample_n_per_3_secs` | SkyWalking 采样率配置
`skywalking.agent.namespace` | SkyWalking 跨进程链路中的header配置
`skywalking.agent.authentication` | SkyWalking 认证token配置
`skywalking.agent.span_limit_per_segment` | SkyWalking 每segment中的最大span数配置
`skywalking.agent.ignore_suffix` | SkyWalking 需要忽略的调用配置
`skywalking.agent.is_open_debugging_class` | SkyWalking 是否保存增强后的字节码文件
`skywalking.collector.backend_service` | SkyWalking OAP 服务地址和端口配置

```bash
$ helm install c7n/knowledgebase-service \
    --set env.open.SKYWALKING_OPTS="-javaagent:/agent/skywalking-agent.jar -Dskywalking.agent.application_code=knowledgebase-service  -Dskywalking.agent.sample_n_per_3_secs=-1 -Dskywalking.collector.backend_service=oap.skywalking:11800" \
    --name knowledgebase-service
```

## 验证部署
```bash
curl -s $(kubectl get po -n c7n-system -l choerodon.io/release=knowledgebase-service -o jsonpath="{.items[0].status.podIP}"):8281/actuator/health | jq -r .status
```
出现以下类似信息即为成功部署

```bash
UP
```
