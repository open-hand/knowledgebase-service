package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.enums.ShareType;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.EnumUtil;
import io.choerodon.kb.infra.utils.PdfProUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
@Primary
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceShareServiceProImpl extends WorkSpaceShareServiceImpl {

    private static final String ERROR_SHARETYPE_ILLEGAL = "error.shareType.illegal";
    private static final String ERROR_WORKSPACESHARE_SELECT = "error.workSpaceShare.select";
    private static final String ERROR_INVALID_URL = "error.invalid.url";
    private static final String ERROR_EMPTY_DATA = "error.empty.data";

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceShareMapper workSpaceShareMapper;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 导出pdf
     *
     * @param pageId
     * @param token
     * @param response
     */
    @Override
    public void exportMd2Pdf(Long pageId, String token, HttpServletResponse response) {
        checkPermissionPro(pageId, token);
        PageInfoVO pageInfoVO = pageRepository.queryShareInfoById(pageId);
        WatermarkVO waterMarkVO = new WatermarkVO();
        waterMarkVO.setDoWaterMark(false);
        PdfProUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response, waterMarkVO);
    }

    /**
     * 校验pageId是否符合分享的权限
     *
     * @param token
     * @param pageId
     * @return
     */
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

    private WorkSpaceShareDTO queryByTokenPro(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setToken(token);
        workSpaceShareDTO = workSpaceShareMapper.selectOne(workSpaceShareDTO);
        if (Objects.equals(null,workSpaceShareDTO)) {
            throw new CommonException(ERROR_WORKSPACESHARE_SELECT);
        }
        return workSpaceShareDTO;
    }

    /**
     * 增加是否分享链接判断
     *
     * @param token
     * @return
     */
    @Override
    public Map<String, Object> queryTree(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = queryByTokenPro(token);
        if(Objects.equals(null,workSpaceShareDTO.getEnabled())){
            throw new CommonException(ERROR_EMPTY_DATA);
        }
        Boolean enabled = workSpaceShareDTO.getEnabled();
        Map result = new HashMap();
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

    /**
     * 查询知识库
     *
     * @param workSpaceId
     * @param token
     * @return
     */
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

    /**
     * 更新分享状态
     *
     * @param organizationId
     * @param projectId
     * @param id
     * @param workSpaceShareUpdateVO
     * @return
     */
    @Override
    public WorkSpaceShareVO updateShare(Long organizationId, Long projectId, Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO) {
        //非法字段判断
        if(workSpaceShareUpdateVO.getType().isEmpty() || Objects.equals(false,EnumUtil.contain(ShareType.class, workSpaceShareUpdateVO.getType()))){
            throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        WorkSpaceShareDTO workSpaceShareDTO = baseQueryById(id);
        workSpaceService.checkById(organizationId, projectId, workSpaceShareDTO.getWorkspaceId());
        Boolean enabled = workSpaceShareUpdateVO.getEnabled();
        //分享状态修改
        if(!Objects.equals(enabled,workSpaceShareDTO.getEnabled())){
            workSpaceShareDTO.setEnabled(enabled);
            workSpaceShareDTO.setObjectVersionNumber(workSpaceShareUpdateVO.getObjectVersionNumber());
            workSpaceShareDTO = baseUpdate(workSpaceShareDTO);
        }
        //分享子页面状态修改
        if(!Objects.equals(workSpaceShareDTO.getType(),workSpaceShareUpdateVO.getType())){
            workSpaceShareDTO.setType(workSpaceShareUpdateVO.getType());
            workSpaceShareDTO.setObjectVersionNumber(workSpaceShareUpdateVO.getObjectVersionNumber());
            workSpaceShareDTO = baseUpdate(workSpaceShareDTO);
        }
        return modelMapper.map(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

}
