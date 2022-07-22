package io.choerodon.kb.infra.feign.operator;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.kb.infra.feign.vo.TenantWpsConfigVO;
import org.hzero.core.util.ResponseUtils;
import org.springframework.stereotype.Component;

/**
 * Copyright (c) 2022. Hand Enterprise Solution Company. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/7/22
 */
@Component
public class RemoteIamOperator {

    private final IamFeignClient iamFeignClient;

    public RemoteIamOperator(IamFeignClient iamFeignClient) {
        this.iamFeignClient = iamFeignClient;
    }

    public WatermarkVO getWaterMarkConfig(Long organizationId) {
        return ResponseUtils.getResponse(iamFeignClient.getWaterMarkConfig(organizationId), WatermarkVO.class);
    }

    public List<ProjectDTO> listProjectsByUserIdForSimple(Long organizationId, Long userId, String category, Boolean enabled) {
        return ResponseUtils.getResponse(iamFeignClient.listProjectsByUserIdForSimple(organizationId, userId, category, enabled),
                new TypeReference<List<ProjectDTO>>() {
                });
    }

    public TenantWpsConfigVO queryTenantWpsConfig(Long tenantId) {
        return ResponseUtils.getResponse(iamFeignClient.queryTenantWpsConfig(tenantId), TenantWpsConfigVO.class);
    }

}
