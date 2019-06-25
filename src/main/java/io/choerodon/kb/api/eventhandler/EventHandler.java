package io.choerodon.kb.api.eventhandler;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.kb.infra.feign.AgileFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

    @Autowired
    private AgileFeignClient agileFeignClient;

    @SagaTask(code = "move-wiki-relation",
            description = "wiki迁移关联关系",
            sagaCode = "agile-move-wiki-relation",
            seq = 1)
    public String moveWikiRelation(String message) {
        agileFeignClient.moveWikiRelation();
        return message;
    }

}
