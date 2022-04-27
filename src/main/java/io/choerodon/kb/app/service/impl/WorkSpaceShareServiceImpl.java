package io.choerodon.kb.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.enums.ShareType;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;
import io.choerodon.kb.infra.repository.*;
import io.choerodon.kb.infra.utils.EnumUtil;
import io.choerodon.kb.infra.utils.PdfProUtil;
import io.choerodon.kb.infra.utils.PdfUtil;
import io.choerodon.kb.infra.utils.TypeUtil;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareServiceImpl implements WorkSpaceShareService {

    private static final String ERROR_SHARETYPE_ILLEGAL = "error.shareType.illegal";
    private static final String ERROR_WORKSPACESHARE_INSERT = "error.workSpaceShare.insert";
    private static final String ERROR_WORKSPACESHARE_SELECT = "error.workSpaceShare.select";
    private static final String ERROR_WORKSPACESHARE_UPDATE = "error.workSpaceShare.update";
    private static final String ERROR_INVALID_URL = "error.invalid.url";
    private static final String ERROR_EMPTY_DATA = "error.empty.date";
    private static final String ERROR_UPDATE_ILLEGAL = "error.update.illegal";

    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceShareMapper workSpaceShareMapper;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public WorkSpaceShareDTO baseCreate(WorkSpaceShareDTO workSpaceShareDTO) {
        if (workSpaceShareMapper.insert(workSpaceShareDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACESHARE_INSERT);
        }
        return workSpaceShareMapper.selectByPrimaryKey(workSpaceShareDTO.getId());
    }

    @Override
    public WorkSpaceShareDTO baseUpdate(WorkSpaceShareDTO workSpaceShareDTO) {
        if (workSpaceShareMapper.updateByPrimaryKey(workSpaceShareDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACESHARE_UPDATE);
        }
        return workSpaceShareMapper.selectByPrimaryKey(workSpaceShareDTO.getId());
    }

    @Override
    public void deleteByWorkSpaceId(Long workSpaceId) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setWorkspaceId(workSpaceId);
        workSpaceShareMapper.delete(workSpaceShareDTO);
    }

    @Override
    public WorkSpaceShareDTO baseQueryById(Long id) {
        WorkSpaceShareDTO workSpaceShareDTO = workSpaceShareMapper.selectByPrimaryKey(id);
        if (workSpaceShareDTO == null) {
            throw new CommonException(ERROR_WORKSPACESHARE_SELECT);
        }
        return workSpaceShareDTO;
    }

    @Override
    public WorkSpaceShareDTO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setWorkspaceId(workSpaceId);
        return workSpaceShareMapper.selectOne(workSpaceShareDTO);
    }


    @Override
    public WorkSpaceShareVO queryShare(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceService.baseQueryById(organizationId, projectId, workSpaceId);
        WorkSpaceShareDTO workSpaceShareDTO = selectByWorkSpaceId(workSpaceDTO.getId());
        if (Objects.equals(null,workSpaceShareDTO)) {
            WorkSpaceShareDTO workSpaceShare = new WorkSpaceShareDTO();
            workSpaceShare.setWorkspaceId(workSpaceDTO.getId());
            workSpaceShare.setType("current_page");
            workSpaceShare.setEnabled(false);
            String md5Str = DigestUtils.md5Hex(TypeUtil.objToString(workSpaceDTO.getId())).substring(8, 24);
            workSpaceShare.setToken(md5Str);
            workSpaceShareDTO = baseCreate(workSpaceShare);
        }

        return modelMapper.map(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

    @Override
    public WorkSpaceShareVO updateShare(Long organizationId, Long projectId, Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO) {
        //判空,有且仅有enabled或者type为空,不可同时为空
        if(Objects.equals(null,workSpaceShareUpdateVO) || Objects.equals(null,workSpaceShareUpdateVO.getObjectVersionNumber()) || (StringUtils.isBlank(workSpaceShareUpdateVO.getType()) && Objects.equals(null,workSpaceShareUpdateVO.getEnabled()))){
            throw new CommonException(ERROR_EMPTY_DATA);
        }
        WorkSpaceShareDTO workSpaceShareDTO = baseQueryById(id);
        workSpaceService.checkById(organizationId, projectId, workSpaceShareDTO.getWorkspaceId());
        //enabled或者type必须有一个为空,不可同时传值
        if(!StringUtils.isBlank(workSpaceShareUpdateVO.getType()) && !Objects.equals(null,workSpaceShareUpdateVO.getEnabled())){
            throw new CommonException(ERROR_UPDATE_ILLEGAL);
        }else if(!StringUtils.isBlank(workSpaceShareUpdateVO.getType())){
            //非法字段判断
            if(Objects.equals(false,EnumUtil.contain(ShareType.class, workSpaceShareUpdateVO.getType()))){
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
            }
            //分享子页面状态修改
            if(!Objects.equals(workSpaceShareDTO.getType(),workSpaceShareUpdateVO.getType())){
                workSpaceShareDTO.setType(workSpaceShareUpdateVO.getType());
            }
        }else if(!Objects.equals(null,workSpaceShareUpdateVO.getEnabled())){
            //分享状态判断
            Boolean enabled = workSpaceShareUpdateVO.getEnabled();
            if (!Objects.equals(enabled,workSpaceShareDTO.getEnabled())){
                workSpaceShareDTO.setEnabled(enabled);
            }
        }
        workSpaceShareDTO.setObjectVersionNumber(workSpaceShareUpdateVO.getObjectVersionNumber());
        workSpaceShareDTO = baseUpdate(workSpaceShareDTO);
        return modelMapper.map(workSpaceShareDTO, WorkSpaceShareVO.class);
    }


    @Override
    public Map<String, Object> queryTree(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = queryByTokenPro(token);
        if(Objects.equals(null,workSpaceShareDTO.getEnabled())){
            throw new CommonException(ERROR_EMPTY_DATA);
        }
        Boolean enabled = workSpaceShareDTO.getEnabled();
        Map<String,Object> result = new HashMap<>();
        if(Boolean.FALSE.equals(enabled)){
            result.put("enabled",enabled);
            return result;
        }
        String type = workSpaceShareDTO.getType();
        switch (type) {
            case ShareType.CURRENT:
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), false);
                break;
            case ShareType.INCLUDE:
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), true);
                break;
            default:
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        result.put("enabled",enabled);
        result.put("shareType", type);
        return result;
    }

    @Override
    public WorkSpaceInfoVO queryWorkSpaceInfo(Long workSpaceId, String token) {
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceId);
        Boolean checkPermission = checkPermissionPro(workSpacePageDTO.getPageId(), token);
        //权限校验失败，抛出无效链接异常
        if (Objects.equals(false,checkPermission)) {
            throw new CommonException(ERROR_INVALID_URL);
        }
        WorkSpaceDTO workSpaceDTO = workSpaceService.selectById(workSpaceId);
        return this.workSpaceService.queryWorkSpaceInfo(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceId, (String) null);
    }

    @Override
    public List<PageAttachmentVO> queryPageAttachment(Long pageId, String token) {
        checkPermission(pageId, token);
        PageDTO pageDTO = pageRepository.selectById(pageId);
        return pageAttachmentService.queryByList(pageDTO.getOrganizationId(), pageDTO.getProjectId(), pageId);
    }

    /**
     * 校验pageId是否符合分享的权限
     *
     * @param token
     * @param pageId
     * @return
     */
    private void checkPermission(Long pageId, String token) {
        Boolean flag = false;
        WorkSpaceShareDTO workSpaceShareDTO = queryByToken(token);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
        switch (workSpaceShareDTO.getType()) {
            case ShareType.CURRENT:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                }
                break;
            case ShareType.INCLUDE:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                } else {
                    //查出所有子空间
                    List<WorkSpaceDTO> workSpaceList = workSpaceService.queryAllChildByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
                    if (workSpaceList != null && !workSpaceList.isEmpty()) {
                        if (workSpaceList.stream().anyMatch(workSpace -> pageId.equals(workSpace.getPageId()))) {
                            flag = true;
                        } else {
                            flag = false;
                        }
                    } else {
                        flag = false;
                    }
                }
                break;
            default:
                break;
        }
        if (Boolean.FALSE.equals(flag)) {
            throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
    }

    private WorkSpaceShareDTO queryByToken(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setToken(token);
        workSpaceShareDTO = workSpaceShareMapper.selectOne(workSpaceShareDTO);
        if (workSpaceShareDTO == null) {
            throw new CommonException(ERROR_WORKSPACESHARE_SELECT);
        }
        if (ShareType.DISABLE.equals(workSpaceShareDTO.getType())) {
            throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        return workSpaceShareDTO;
    }

    @Override
    public void exportMd2Pdf(Long pageId, String token, HttpServletResponse response) {
        checkPermissionPro(pageId, token);
        PageInfoVO pageInfoVO = pageRepository.queryShareInfoById(pageId);
        WatermarkVO waterMarkVO = new WatermarkVO();
        waterMarkVO.setDoWaterMark(false);
        PdfProUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response, waterMarkVO);
    }

    private WorkSpaceShareDTO queryByTokenPro(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setToken(token);
        workSpaceShareDTO = workSpaceShareMapper.selectOne(workSpaceShareDTO);
        if (Objects.equals(null,workSpaceShareDTO)) {
            throw new CommonException(ERROR_WORKSPACESHARE_SELECT);
        }
        return workSpaceShareDTO;
    }
    private Boolean checkPermissionPro(Long pageId, String token) {
        WorkSpaceShareDTO workSpaceShareDTO = queryByTokenPro(token);
        if(Objects.equals(null,workSpaceShareDTO.getEnabled())){
            throw new CommonException(ERROR_EMPTY_DATA);
        }
        Boolean flag;
        Boolean enabled = workSpaceShareDTO.getEnabled();
        if(Boolean.FALSE.equals(enabled)){
            return false;
        }
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
        switch (workSpaceShareDTO.getType()) {
            case ShareType.CURRENT:
                flag = pageId.equals(workSpacePageDTO.getPageId());
                break;
            case ShareType.INCLUDE:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                } else {
                    //查出所有子空间
                    List<WorkSpaceDTO> workSpaceList = workSpaceService.queryAllChildByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
                    flag = (workSpaceList != null && !workSpaceList.isEmpty()) && (workSpaceList.stream().anyMatch(workSpace -> pageId.equals(workSpace.getPageId())));
                }
                break;
            default:
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        return flag;
    }

}
