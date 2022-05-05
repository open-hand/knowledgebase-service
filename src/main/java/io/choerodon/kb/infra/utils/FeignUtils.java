package io.choerodon.kb.infra.utils;

import org.springframework.http.ResponseEntity;

import io.choerodon.core.exception.CommonException;

/**
 * 判断ResponseEntity调用结果
 *
 * @author ying.xie@hand-china.com
 * @date 2020/3/19
 */
public class FeignUtils {
    public static <T> T handleResponseEntity(ResponseEntity<T> responseEntity) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new CommonException("error.feign.call");
        }
    }
}
