package io.choerodon.kb.infra.feign.operator;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.AgileFeignClient;
import org.hzero.core.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhaotianxin
 * @date 2021-01-21 13:50
 */
@Component
public class AgileFeignOperator {
    @Autowired
    private AgileFeignClient agileFeignClient;

    public List<ProjectDTO> deleteByworkSpaceId(Long projectId, Long spaceId) {
        return ResponseUtils.getResponse(agileFeignClient.deleteByworkSpaceId(projectId, spaceId), new TypeReference<List<ProjectDTO>>() {
        });
    }
}
