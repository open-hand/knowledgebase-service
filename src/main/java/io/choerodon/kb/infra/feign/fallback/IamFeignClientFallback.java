package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.IamFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * @author superlee
 * @since 12/27/21
 */
@Component
public class IamFeignClientFallback implements IamFeignClient {


    @Override
    public ResponseEntity<Page<ProjectDTO>> pagingQuery(Long organizationId, Integer page, Integer size, String name, String code, Boolean enabled, Boolean withAdditionInfo, String params) {
        throw new CommonException("error.feign.iam.pagingQuery");
    }
}
