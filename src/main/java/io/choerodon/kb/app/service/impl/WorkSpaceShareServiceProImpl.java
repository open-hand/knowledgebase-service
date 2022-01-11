package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.api.vo.WorkSpaceTreeVO;
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
    private Boolean checkPermission(Long pageId, String token) {
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
            case ShareType.DISABLE:
                flag=false;
                break;
            default:
                throw new CommonException(ERROR_SHARETYPE_ILLEGAL);
        }
        return flag;
    }

    private WorkSpaceShareDTO queryByToken(String token) {
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
        WorkSpaceShareDTO workSpaceShareDTO = queryByToken(token);
        String type = workSpaceShareDTO.getType();
        Map result;
        switch (type){
            case "include_page":
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), false);
                break;
            case "current_page":
                result = this.workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), true);
                break;
            case "disabled":
                //链接取消分享，返回一个默认值
                result=new HashMap();
                Map<Long, WorkSpaceTreeVO> workSpaceTreeMap = new HashMap(1);
                WorkSpaceTreeVO topSpace = new WorkSpaceTreeVO();
                WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
                data.setTitle("choerodon");
                topSpace.setData(data);
                topSpace.setParentId(0L);
                topSpace.setId(0L);
                workSpaceShareDTO.setId(0L);
                workSpaceTreeMap.put(0L,topSpace);
                result.put("rootId",0L);
                result.put("items",workSpaceTreeMap);
                break;
            default:
                throw new CommonException("error.shareType.illegal", new Object[0]);
        }
        return result;
    }

    /**
     *
     * @param workSpaceId
     * @param token
     * @return
     */
    @Override
    public WorkSpaceInfoVO queryWorkSpaceInfo(Long workSpaceId, String token) {
        WorkSpacePageDTO workSpacePageDTO = this.workSpacePageService.selectByWorkSpaceId(workSpaceId);
        Boolean checkPermission=checkPermission(workSpacePageDTO.getPageId(), token);
        WorkSpaceDTO workSpaceDTO = this.workSpaceService.selectById(workSpaceId);
        if(Boolean.TRUE.equals(checkPermission)){
            return this.workSpaceService.queryWorkSpaceInfo(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceId, (String)null);
        }
        //权限校验失败，无法分享，返回默认值
        WorkSpaceInfoVO workSpaceInfoVO=new WorkSpaceInfoVO();
        workSpaceInfoVO.setRoute(workSpaceDTO.getRoute());
        workSpaceInfoVO.setId(workSpaceDTO.getWorkPageId());
        return workSpaceInfoVO;
    }
}
