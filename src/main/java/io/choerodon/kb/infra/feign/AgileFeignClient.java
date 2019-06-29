package io.choerodon.kb.infra.feign;

import io.choerodon.kb.infra.feign.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "agile-service", fallback = UserFeignClientFallback.class)
public interface AgileFeignClient {

    @PostMapping("/v1/fix_data/move_wiki_relation")
    ResponseEntity moveWikiRelation();
}
