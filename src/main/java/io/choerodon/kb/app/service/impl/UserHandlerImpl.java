package io.choerodon.kb.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yqcloud.wps.dto.WpsUserDTO;
import com.yqcloud.wps.service.impl.AbstractUserHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.core.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * Created by wangxiang on 2022/5/5
 */
@Service
public class UserHandlerImpl extends AbstractUserHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandlerImpl.class);
    private static final String ONLINE_USERS_KEY_PREFIX = "knowledge:tenant:";

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public WpsUserDTO getWpsUserInfo(Long tenantId, String userId) {
        Long[] userIds = {Long.parseLong(userId)};
        ResponseEntity<List<UserDO>> listResponseEntity = baseFeignClient.listUsersByIds(userIds, false);
        List<UserDO> userDOS = listResponseEntity.getBody();
        if (CollectionUtils.isNotEmpty(userDOS)) {
            UserDO userVO = userDOS.get(0);
            WpsUserDTO wpsUserDTO = getWpsUserDTO(userVO);
            return wpsUserDTO;
        } else {
            return new WpsUserDTO();
        }
    }

    @Override
    public List<WpsUserDTO> listUsersByIds(long tenantId, List<String> userIds) {
        List<WpsUserDTO> wpsUserDTOS = new ArrayList<>();
        if (org.springframework.util.CollectionUtils.isEmpty(userIds)) {
            return wpsUserDTOS;
        }
        List<Long> ids = userIds.stream().map(aLong -> Long.parseLong(aLong)).collect(Collectors.toList());
        ResponseEntity<List<UserDO>> listResponseEntity = baseFeignClient.listUsersByIds(ids.toArray(new Long[ids.size()]), null);
        if (listResponseEntity.getStatusCode().is2xxSuccessful() && org.springframework.util.CollectionUtils.isEmpty(listResponseEntity.getBody())) {
            List<UserDO> entityBody = listResponseEntity.getBody();
            List<WpsUserDTO> userDTOS = ConvertUtils.convertList(entityBody, userDO -> {
                WpsUserDTO wpsUserDTO = getWpsUserDTO(userDO);
                return wpsUserDTO;
            });
            return userDTOS;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void onlineUsers(JSONObject obj, String tenantId, String fileId) {
        super.onlineUsers(obj, tenantId, fileId);
        //{"ids":["1"]}
        // 通知此文件目前有哪些人正在协作param:1419{"ids":["1"]}
        //
        // fileId:0ff55ced179043deaa63a04be90706f9
        //
        //tenantId:1419
        //感知用户进入退出编辑页面，这里将用户数量刷新进缓存
        //根据fileId查询fileVersion
        redisHelper.hshPut(ONLINE_USERS_KEY_PREFIX + tenantId, fileId, obj.toJSONString());
        redisHelper.setExpire(ONLINE_USERS_KEY_PREFIX + tenantId, 86400);
    }

    private WpsUserDTO getWpsUserDTO(UserDO userDO) {
        WpsUserDTO wpsUserDTO = new WpsUserDTO();
        wpsUserDTO.setName(userDO.getRealName());
        wpsUserDTO.setId(String.valueOf(userDO.getId()));
        wpsUserDTO.setAvatarUrl(userDO.getImageUrl());
        return wpsUserDTO;
    }
}
