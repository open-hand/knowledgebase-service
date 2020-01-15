package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.enums.ShareType;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;
import io.choerodon.kb.infra.utils.EnumUtil;
import io.choerodon.kb.infra.utils.PdfUtil;
import io.choerodon.kb.infra.utils.TypeUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareServiceImpl implements WorkSpaceShareService {

    private static final String ERROR_SHARETYPE_ILLEGAL = "error.shareType.illegal";
    private static final String ERROR_WORKSPACESHARE_INSERT = "error.workSpaceShare.insert";
    private static final String ERROR_WORKSPACESHARE_SELECT = "error.workSpaceShare.select";
    private static final String ERROR_WORKSPACESHARE_UPDATE = "error.workSpaceShare.update";

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
        //不存在分享记录则创建
        if (workSpaceShareDTO == null) {
            WorkSpaceShareDTO workSpaceShare = new WorkSpaceShareDTO();
            workSpaceShare.setWorkspaceId(workSpaceDTO.getId());
            workSpaceShare.setType(ShareType.CURRENT);
            //生成16位的md5编码
            String md5Str = DigestUtils.md5Hex(TypeUtil.objToString(workSpaceDTO.getId())).substring(8, 24);
            workSpaceShare.setToken(md5Str);
            workSpaceShareDTO = baseCreate(workSpaceShare);
        }
        return modelMapper.map(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

    @Override
    public WorkSpaceShareVO updateShare(Long organizationId, Long projectId, Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO) {
        if (Boolean.FALSE.equals(EnumUtil.contain(ShareType.class, workSpaceShareUpdateVO.getType()))) {
            throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        WorkSpaceShareDTO workSpaceShareDTO = baseQueryById(id);
        workSpaceService.checkById(organizationId, projectId, workSpaceShareDTO.getWorkspaceId());
        if (!workSpaceShareDTO.getType().equals(workSpaceShareUpdateVO.getType())) {
            workSpaceShareDTO.setType(workSpaceShareUpdateVO.getType());
            workSpaceShareDTO.setObjectVersionNumber(workSpaceShareUpdateVO.getObjectVersionNumber());
            workSpaceShareDTO = baseUpdate(workSpaceShareDTO);
        }
        return modelMapper.map(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

    @Override
    public Map<String, Object> queryTree(String token) {
        Map<String, Object> result;
        WorkSpaceShareDTO workSpaceShareDTO = queryByToken(token);
        switch (workSpaceShareDTO.getType()) {
            case ShareType.CURRENT:
                result = workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), false);
                break;
            case ShareType.INCLUDE:
                result = workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), true);
                break;
            default:
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        return result;
    }

    @Override
    public WorkSpaceInfoVO queryWorkSpaceInfo(Long workSpaceId, String token) {
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceId);
        checkPermission(workSpacePageDTO.getPageId(), token);
        WorkSpaceDTO workSpaceDTO = workSpaceService.selectById(workSpaceId);
        return workSpaceService.queryWorkSpaceInfo(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceId, null);
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
        checkPermission(pageId, token);
        PageInfoVO pageInfoVO = pageRepository.queryShareInfoById(pageId);
        PdfUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response);
    }
}
