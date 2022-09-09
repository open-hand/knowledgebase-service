package io.choerodon.kb.infra.repository.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.repository.AsgardRemoteRepository;
import io.choerodon.kb.infra.feign.AsgardFeignClient;
import io.choerodon.kb.infra.feign.vo.SagaInstanceDetails;

import org.hzero.core.util.ResponseUtils;

@Repository
public class AsgardRemoteRepositoryImpl extends AsgardRemoteRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsgardRemoteRepositoryImpl.class);

    @Autowired
    private AsgardFeignClient asgardFeignClient;

    @Override
    public List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        // 查询事务状态流程出错 不影响整体查询
        try {
            final List<SagaInstanceDetails> results = ResponseUtils.getResponse(
                    this.asgardFeignClient.queryByRefTypeAndRefIds(refType, refIds, sagaCode),
                    new TypeReference<List<SagaInstanceDetails>>() {
                    }
            );
            if (CollectionUtils.isEmpty(results)) {
                throw new CommonException("error.queryOrganizationById.saga");
            }
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
