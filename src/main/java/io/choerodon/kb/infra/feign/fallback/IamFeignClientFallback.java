package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.infra.feign.IamFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author superlee
 * @since 12/27/21
 */
public class IamFeignClientFallback implements IamFeignClient {

    private Throwable cause;

    public IamFeignClientFallback (Throwable cause) {
        Assert.notNull(cause, "throwable can not be null");
        this.cause = cause;
    }


    @Override
    public ResponseEntity<WatermarkVO> getWaterMarkConfig(Long organizationId) {
        throw new CommonException("error.query.watermark", cause);
    }


    @Override
    public ResponseEntity<List<ProjectDTO>> listProjectsByUserIdForSimple(Long organizationId, Long userId, String category, Boolean enabled) {
        throw new CommonException("error.query.org.projects");
    }
}
