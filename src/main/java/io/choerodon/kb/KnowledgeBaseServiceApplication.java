package io.choerodon.kb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@SpringBootApplication
@EnableEurekaClient
@EnableChoerodonResourceServer
@EnableFeignClients("io.choerodon")
public class KnowledgeBaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseServiceApplication.class, args);
    }
}
