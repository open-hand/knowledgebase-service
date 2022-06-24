package io.choerodon.kb.infra.feign.operator;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.AsgardFeignClient;
import io.choerodon.kb.infra.feign.vo.SagaInstanceDetails;


@Component
public class AsgardServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsgardServiceClientOperator.class);


    @Autowired
    private AsgardFeignClient asgardFeignClient;


    public List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        ResponseEntity<List<SagaInstanceDetails>> listResponseEntity;
        // 查询事务状态流程出错 不影响整体查询
        try {
            listResponseEntity = asgardFeignClient.queryByRefTypeAndRefIds(refType, refIds, sagaCode);
            if (listResponseEntity == null) {
                throw new CommonException("error.query.saga");
            }
            return listResponseEntity.getBody();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
