package io.choerodon.kb.app.service.impl;

import com.yqcloud.wps.dto.WpsUserDTO;
import com.yqcloud.wps.service.impl.AbstractUserHandler;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * Created by wangxiang on 2022/5/5
 */
@Service
public class UserHandlerImpl extends AbstractUserHandler {

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Override
    public WpsUserDTO getWpsUserInfo(Long tenantId, String userId) {
        Long[] userIds = {Long.parseLong(userId)};
        ResponseEntity<List<UserDO>> listResponseEntity = baseFeignClient.listUsersByIds(userIds, false);
        List<UserDO> userDOS = listResponseEntity.getBody();
        if (CollectionUtils.isNotEmpty(userDOS)) {
            UserDO userVO = userDOS.get(0);
            WpsUserDTO wpsUserDTO = new WpsUserDTO();
            wpsUserDTO.setName(userVO.getRealName());
            wpsUserDTO.setId(String.valueOf(userVO.getId()));
            wpsUserDTO.setAvatarUrl(userVO.getImageUrl());
            return wpsUserDTO;
        } else {
            return new WpsUserDTO();
        }
    }

    @Override
    public List<WpsUserDTO> listUsersByIds(long tenantId, List<String> userIds) {
        return null;
    }
}
