package io.choerodon.kb.infra.feign.fallback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.FeignException;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.AgileFeignClient;

/**
 * @author: 25499
 * @date: 2020/1/17 16:58
 * @description:
 */
@Component
public class AgileFeignClientFallback implements AgileFeignClient {
    private static final String DELETE_ERROR = "error.agile-service.delete";
    @Override
    public ResponseEntity<String> deleteByworkSpaceId(Long projectId, Long spaceId) {
        throw new FeignException(DELETE_ERROR);
    }
}
