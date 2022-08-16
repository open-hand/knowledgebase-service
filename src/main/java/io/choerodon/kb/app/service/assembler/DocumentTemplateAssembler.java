package io.choerodon.kb.app.service.assembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.kb.api.vo.DocumentTemplateInfoVO;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class DocumentTemplateAssembler {
    @Autowired
    private IamRemoteRepository iamRemoteRepository;

    public Map<Long, UserDO> findUsers(List<Long> users){
        List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(users, false);
        if(CollectionUtils.isEmpty(userDOList)){
            return new HashMap<>();
        }
        return userDOList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
    }

    public DocumentTemplateInfoVO toTemplateInfoVO(Map<Long, UserDO> users, DocumentTemplateInfoVO documentTemplateInfoVO){
        documentTemplateInfoVO.setCreatedUser(users.get(documentTemplateInfoVO.getCreatedBy()));
        documentTemplateInfoVO.setLastUpdatedUser(users.get(documentTemplateInfoVO.getLastUpdatedBy()));
        return documentTemplateInfoVO;
    }
}
