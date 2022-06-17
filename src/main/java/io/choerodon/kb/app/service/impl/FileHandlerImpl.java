package io.choerodon.kb.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yqcloud.wps.base.Context;
import com.yqcloud.wps.dto.WpsFileDTO;
import com.yqcloud.wps.dto.WpsFileVersionDTO;
import com.yqcloud.wps.dto.WpsUserDTO;
import com.yqcloud.wps.maskant.adaptor.WPSFileAdaptor;
import com.yqcloud.wps.service.impl.AbstractFileHandler;
import com.yqcloud.wps.util.JsonHelper;
import java.util.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.boot.file.dto.FileSimpleDTO;
import org.hzero.core.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.kb.api.vo.OnlineUserVO;
import io.choerodon.kb.infra.dto.FileVersionDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.FileFeignClient;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.feign.vo.TenantWpsConfigVO;
import io.choerodon.kb.infra.mapper.FileVersionMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.ExpandFileClient;

/**
 * Created by wangxiang on 2022/5/5
 */
@Service
public class FileHandlerImpl extends AbstractFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerImpl.class);
    private static final Long DEFAULT_EXPIRES = 1800L;
    private static final String ONLINE_USERS_KEY_PREFIX = "knowledge:tenant:";

    @Autowired
    private ExpandFileClient expandFileClient;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private WPSFileAdaptor wpsFileAdaptor;
    @Autowired
    private FileFeignClient fileFeignClient;
    @Autowired
    private FileVersionMapper fileVersionMapper;
    @Autowired
    private IamFeignClient iamFeignClient;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    protected void createFile() {

    }

    @Override
    public WpsFileDTO queryFileByFileKey(String fileKey, String fileSourceType, String sourceId) {
        String tenantId = Context.getTenantId();
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(Long.parseLong(tenantId), fileKey);
        WpsFileDTO wpsFileDTO = new WpsFileDTO();
        wpsFileDTO.setFileName(wpsFileAdaptor.getFileName(fileKey));
        wpsFileDTO.setFileKey(fileKey);
        FileVersionDTO maxVersion = fileVersionMapper.findMaxVersion(Context.getFileId());
        Integer currentVersion = null == maxVersion ? 1 : maxVersion.getVersion();
        wpsFileDTO.setVersion(currentVersion);
        wpsFileDTO.setCreate_time(fileDTOByFileKey.getCreationDate());
        wpsFileDTO.setCreator(String.valueOf(fileDTOByFileKey.getCreatedBy()));
        wpsFileDTO.setModifier(String.valueOf(fileDTOByFileKey.getLastUpdatedBy()));
        wpsFileDTO.setModify_time(fileDTOByFileKey.getLastUpdateDate());
        wpsFileDTO.setFileUrl(fileDTOByFileKey.getFileUrl());
        return wpsFileDTO;
    }

    @Override
    public WpsFileVersionDTO saveCurrentVersion(String fileName, String fileId, String fileKey, MultipartFile mFile, String bucketName, WpsUserDTO userInfo) {
        WorkSpaceDTO spaceDTO = new WorkSpaceDTO();
        spaceDTO.setFileKey(fileKey);
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectOne(spaceDTO);
        if (workSpaceDTO == null) {
            LOGGER.error("error.save.file.new.version :fileKey:{},fileId:{}", fileKey, fileId);
        }

        Long tenantId = workSpaceDTO.getOrganizationId();
        //上传一份新的文件 上传一份文件以后，除了fileId不变以外其他都要变  fileKey  和fileUrl都是新的
        FileSimpleDTO fileSimpleDTO = wpsFileAdaptor.uploadMultipartFileWithMD5(tenantId, bucketName, "", mFile.getOriginalFilename(), 0, null, mFile, Context.getToken());

        Long fileSize = getFileSize(tenantId, fileSimpleDTO.getFileKey(), Context.getToken());
        //查询最大的版本
        FileVersionDTO maxVersion = fileVersionMapper.findMaxVersion(fileId);
        //通过旧的fileKey找到旧的file,用于存版本
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(tenantId, fileKey);
        Integer currentVersion = null == maxVersion ? 1 : maxVersion.getVersion() + 1;

        WpsFileVersionDTO fileVersionDTO = new WpsFileVersionDTO();
        fileVersionDTO.setFileId(fileId);
        fileVersionDTO.setFileKey(fileSimpleDTO.getFileKey());
        fileVersionDTO.setVersion(currentVersion);
        fileVersionDTO.setFileUrl(fileDTOByFileKey.getFileUrl());
        fileVersionDTO.setName(fileName);
        fileVersionDTO.setSize(fileSize);
        fileVersionDTO.setMd5(fileSimpleDTO.getMd5());
        fileVersionDTO.setCreatedBy(Long.valueOf(userInfo.getId()));
        fileVersionDTO.setLastUpdatedBy(Long.valueOf(userInfo.getId()));


        FileVersionDTO versionDTO = new FileVersionDTO();
        BeanUtils.copyProperties(fileVersionDTO, versionDTO);
        versionDTO.setFileSize(fileVersionDTO.getSize());
        fileVersionMapper.insertSelective(versionDTO);

        //这个fileKey 还是要按照传过来的跟新回去，不然历史记录出错
        //跟新kb表
        workSpaceDTO.setFileKey(fileKey);
        workSpaceMapper.updateByPrimaryKey(workSpaceDTO);

        return fileVersionDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(String fileKey, String userId, String newFileName) {
        //feign 修改file数据库的地址
//        WorkSpaceDTO spaceDTO = new WorkSpaceDTO();
//        spaceDTO.setFileKey(fileKey);
//        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectOne(spaceDTO);
//        if (workSpaceDTO == null) {
//            LOGGER.error("error.rename.file.work.space.is.null");
//            return;
//        }
//        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(workSpaceDTO.getOrganizationId(), fileKey);
//        if (fileDTOByFileKey == null) {
//            LOGGER.error("error.rename.file.is.null");
//            return;
//        }
//        fileDTOByFileKey.setFileName(newFileName);
//        fileFeignClient.updateFile(workSpaceDTO.getOrganizationId(), fileDTOByFileKey);
//        workSpaceDTO.setName(newFileName);
//        workSpaceMapper.updateByPrimaryKeySelective(workSpaceDTO);
        LOGGER.info("》》》》》》》》》》》》》》》》》》》》》》》》》》》回调到rename 方法");
    }

    @Override
    public List<WpsFileVersionDTO> findAllByFileId(String fileId, Integer count, Integer offset) {
        List<FileVersionDTO> fileVersionDTOS = fileVersionMapper.findAllByFileId(fileId, count, offset);
        return ConvertUtils.convertList(fileVersionDTOS, this::toWpsFileVersionDTO);
    }

    @Override
    public WpsFileVersionDTO findFileVersionByVersionAndFileId(Integer version, String fileId) {
        //通过fileKey找到fileId 根据fileId找到所有的版本
        FileVersionDTO fileVersionDTO = new FileVersionDTO();
        fileVersionDTO.setVersion(version);
        fileVersionDTO.setFileId(fileId);
        List<FileVersionDTO> fileVersionDTOS = fileVersionMapper.select(fileVersionDTO);
        if (CollectionUtils.isEmpty(fileVersionDTOS)) {
            return null;
        }
        WpsFileVersionDTO wpsFileVersionDTO = new WpsFileVersionDTO();
        BeanUtils.copyProperties(fileVersionDTOS.get(0), wpsFileVersionDTO);
        wpsFileVersionDTO.setSize(fileVersionDTOS.get(0).getFileSize());
        return wpsFileVersionDTO;
    }

    @Override
    public Long getFileSize(Long tenantId, String fileKey, String token) {
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(tenantId, fileKey);
        return fileDTOByFileKey == null ? 0L : fileDTOByFileKey.getFileSize();
    }

    @Override
    public JSONObject businessProcessing(String fileKey, String userId, String tenantId, String type) {
        super.businessProcessing(fileKey, userId, tenantId, type);
        //请求编辑链接之前，需要进行判断，是否超出这个组织的编辑数量的限制
        //查询该组织的限制
        ResponseEntity<TenantWpsConfigVO> wpsConfigVOResponseEntity = iamFeignClient.queryTenantWpsConfig(Long.parseLong(tenantId));
        if (wpsConfigVOResponseEntity.getStatusCode().is2xxSuccessful() && wpsConfigVOResponseEntity.getBody() != null) {
            TenantWpsConfigVO tenantWpsConfigVO = wpsConfigVOResponseEntity.getBody();
            if (tenantWpsConfigVO == null || tenantWpsConfigVO.getEnableWpsEdit() == null) {
                LOGGER.info("tenant wps config is null, tenantId:{}", tenantId);
                return null;
            }
            if (!tenantWpsConfigVO.getEnableWpsEdit() || tenantWpsConfigVO.getConnectionNumber() == null) {
                //如果配置没有开启则中断请求
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("result", 1);
                jsonObject.put("msg", "WPS editing service is not enabled");
                return jsonObject;
            }
            //如果存在配置并且开启了配置，则判断人数
            return getJsonObject(tenantId, tenantWpsConfigVO);
        }
        return null;
    }

    private JSONObject getJsonObject(String tenantId, TenantWpsConfigVO tenantWpsConfigVO) {
        String key = ONLINE_USERS_KEY_PREFIX + tenantId;
        Map<String, String> stringStringMap = redisHelper.hshGetAll(key);
        if (MapUtils.isEmpty(stringStringMap)) {
            return null;
        }
        List<String> ids = new ArrayList<>();
        stringStringMap.forEach((keys, values) -> {
            OnlineUserVO onlineUser = JSONObject.parseObject(values, OnlineUserVO.class);
            if (onlineUser != null && !org.springframework.util.CollectionUtils.isEmpty(onlineUser.getIds())) {
                ids.addAll(onlineUser.getIds());
            }
        });
        if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
            return null;
        }
        if (tenantWpsConfigVO.getConnectionNumber() <= ids.size()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", 2);
            jsonObject.put("msg", "Connection limit exceeded");
            return jsonObject;
        } else {
            return null;
        }
    }

    private WpsFileVersionDTO toWpsFileVersionDTO(FileVersionDTO fileVersionDTO) {
        WpsFileVersionDTO wpsFileVersionDTO = ConvertUtils.convertObject(fileVersionDTO, WpsFileVersionDTO.class);
        wpsFileVersionDTO.setCreate_time(fileVersionDTO.getCreationDate());
        wpsFileVersionDTO.setModify_time(fileVersionDTO.getLastUpdateDate());
        wpsFileVersionDTO.setCreator(String.valueOf(fileVersionDTO.getCreatedBy()));
        wpsFileVersionDTO.setSize(fileVersionDTO.getFileSize());
        FileSimpleDTO fileUrl = wpsFileAdaptor.getFileUrl(fileVersionDTO.getTenantId(), fileVersionDTO.getFileKey(), DEFAULT_EXPIRES, Context.getToken());
        wpsFileVersionDTO.setFileUrl(fileUrl.getFileTokenUrl());
        return wpsFileVersionDTO;
    }

}
