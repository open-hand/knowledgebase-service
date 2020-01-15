package io.choerodon.kb.app.service.assembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.choerodon.kb.api.vo.DocumentTemplateInfoVO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class DocumentTemplateAssembler {
    @Autowired
    private BaseFeignClient baseFeignClient;

    public Map<Long, UserDO> findUsers(List<Long> users){
        List<UserDO> usersDO = baseFeignClient.listUsersByIds(users.toArray(new Long[users.size()]), false).getBody();
        if(CollectionUtils.isEmpty(usersDO)){
            return new HashMap<>();
        }
        Map<Long, UserDO> collect = usersDO.stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        return collect;
    }

    public DocumentTemplateInfoVO toTemplateInfoVO(Map<Long, UserDO> users, DocumentTemplateInfoVO documentTemplateInfoVO){
        documentTemplateInfoVO.setCreatedUser(users.get(documentTemplateInfoVO.getCreatedBy()));
        documentTemplateInfoVO.setLastUpdatedUser(users.get(documentTemplateInfoVO.getLastUpdatedBy()));
        return documentTemplateInfoVO;
    }
}
