package io.choerodon.kb.infra.feign.operator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.ServiceUnavailableException;
import io.choerodon.core.utils.FeignClientUtils;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.AgileFeignClient;

/**
 * @author zhaotianxin
 * @date 2021-01-21 13:50
 */
@Component
public class AgileFeignOperator {
    @Autowired
    private AgileFeignClient agileFeignClient;

    public List<ProjectDTO> deleteByworkSpaceId(Long projectId, Long spaceId){
       try {
           return FeignClientUtils.doRequest(() -> agileFeignClient.deleteByworkSpaceId(projectId, spaceId), new TypeReference<List<ProjectDTO>>() {});
       } catch (ServiceUnavailableException e) {
           return new ArrayList<>();
       }
    }
}
