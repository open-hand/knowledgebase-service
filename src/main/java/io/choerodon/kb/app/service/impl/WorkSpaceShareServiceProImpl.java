package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.enums.ShareType;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.PdfProUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
@Primary
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceShareServiceProImpl extends WorkSpaceShareServiceImpl {

    private static final String ERROR_SHARETYPE_ILLEGAL = "error.shareType.illegal";
    private static final String ERROR_WORKSPACESHARE_SELECT = "error.workSpaceShare.select";
    private static final String ERROR_INVALID_URL="error.invalid.url";

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceShareMapper workSpaceShareMapper;

    /**
     * 导出pdf
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
        Boolean flag ;
        WorkSpaceShareDTO workSpaceShareDTO = queryByTokenPro(token);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
        switch (workSpaceShareDTO.getType()) {
            case ShareType.CURRENT:
                flag=pageId.equals(workSpacePageDTO.getPageId());
                break;
            case ShareType.INCLUDE:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                } else {
                    //查出所有子空间
                    List<WorkSpaceDTO> workSpaceList = workSpaceService.queryAllChildByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
                    flag=(workSpaceList != null && !workSpaceList.isEmpty())&&(workSpaceList.stream().anyMatch(workSpace -> pageId.equals(workSpace.getPageId())));
                }
                break;
            case ShareType.DISABLE:
                flag=false;
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
        if (workSpaceShareDTO == null) {
            throw new CommonException(ERROR_WORKSPACESHARE_SELECT);
        }
        return workSpaceShareDTO;
    }

    /**
     * 增加是否分享链接判断
     * @param token
     * @return
     */
    @Override
    public Map<String, Object> queryTree(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = queryByTokenPro(token);
        String type = workSpaceShareDTO.getType();
        Map result=new HashMap();
        switch (type){
            case ShareType.CURRENT:
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), false);
                break;
            case ShareType.INCLUDE:
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), true);
                break;
            case ShareType.DISABLE:
                break;
            default:
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        result.put("shareType",type);
        return result;
    }

    /**
     *查询知识库
     * @param workSpaceId
     * @param token
     * @return
     */
    @Override
    public WorkSpaceInfoVO queryWorkSpaceInfo(Long workSpaceId, String token) {
        WorkSpacePageDTO workSpacePageDTO = this.workSpacePageService.selectByWorkSpaceId(workSpaceId);
        Boolean checkPermission=checkPermissionPro(workSpacePageDTO.getPageId(), token);
        //权限校验失败，抛出无效链接异常
        if(Boolean.FALSE.equals(checkPermission)){
            throw new CommonException(ERROR_INVALID_URL);
        }
        WorkSpaceDTO workSpaceDTO = this.workSpaceService.selectById(workSpaceId);
        return this.workSpaceService.queryWorkSpaceInfo(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceId, (String)null);
    }
}
