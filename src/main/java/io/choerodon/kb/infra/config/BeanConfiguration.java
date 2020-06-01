package io.choerodon.kb.infra.config;

import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import org.hzero.boot.file.feign.FileRemoteService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shinan.chen
 * @date 2018/9/27
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public ExpandFileClient expandFileClient(FileRemoteService fileRemoteService,
                                             CustomFileRemoteService customFileRemoteService) {
        return new ExpandFileClient(fileRemoteService, customFileRemoteService);
    }

}