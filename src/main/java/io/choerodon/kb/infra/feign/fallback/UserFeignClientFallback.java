package io.choerodon.kb.infra.feign.fallback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.dataobject.UserDO;
import io.choerodon.kb.infra.feign.UserFeignClient;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class UserFeignClientFallback implements UserFeignClient {

    private static final String BATCH_QUERY_ERROR = "error.UserFeign.queryList";

    @Override
    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new CommonException(BATCH_QUERY_ERROR);
    }
}