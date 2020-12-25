package io.choerodon.kb;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
@EnableEurekaClient
@EnableChoerodonResourceServer
public class KnowledgeBaseServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseServiceApplication.class, args);
    }


    /**
     * 自定义异步线程池
     *
     * @return
     */
    @Bean
    @Qualifier("xwiki-sync")
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("xwiki-sync");
        executor.setMaxPoolSize(3);
        executor.setCorePoolSize(2);
        return executor;
    }
    
}
