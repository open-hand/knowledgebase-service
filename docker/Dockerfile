FROM registry.cn-shanghai.aliyuncs.com/choerodon-tools/javabase:0.8.2
COPY app.jar /knowledgebase-service.jar
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap $JAVA_OPTS $SKYWALKING_OPTS  -jar /knowledgebase-service.jar