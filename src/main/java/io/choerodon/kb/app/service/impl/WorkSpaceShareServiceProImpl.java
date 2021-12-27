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
import io.choerodon.kb.infra.utils.PdfProUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
@Primary
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceShareServiceProImpl extends WorkSpaceShareServiceImpl {

    private static final String ERROR_SHARETYPE_ILLEGAL = "error.shareType.illegal";
    private static final String ERROR_WORKSPACESHARE_SELECT = "error.workSpaceShare.select";

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceShareMapper workSpaceShareMapper;
    

    @Override
    public void exportMd2Pdf(Long pageId, String token, HttpServletResponse response) {
        checkPermission(pageId, token);
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
}
