package io.choerodon.kb.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.utils.PdfUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareServiceImpl implements WorkSpaceShareService {

    private static final List<String> INITDATA = Stream.of(BaseStage.SHARE_CURRENT, BaseStage.SHARE_INCLUDE, BaseStage.SHARE_DISABLE).collect(toList());
    private static final String NO_ACCESS = "No access!";
    private static final String ERROR_SHARE_TYPE = "error.share.type";

    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private WorkSpaceShareRepository workSpaceShareRepository;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageContentRepository pageContentRepository;

    @Override
    public WorkSpaceShareDTO query(Long workSpaceId) {
        WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceId);
        WorkSpaceShareDO workSpaceShareDO = workSpaceShareRepository.selectByWorkSpaceId(workSpaceDO.getId());

        if (workSpaceShareDO == null) {
            WorkSpaceShareDO workSpaceShare = new WorkSpaceShareDO();
            workSpaceShare.setWorkspaceId(workSpaceDO.getId());
            workSpaceShare.setType(BaseStage.SHARE_CURRENT);
            //生成16位的md5编码
            String md5Str = DigestUtils.md5Hex(TypeUtil.objToString(workSpaceDO.getId())).substring(8, 24);
            workSpaceShare.setToken(md5Str);
            return ConvertHelper.convert(workSpaceShareRepository.insert(workSpaceShare), WorkSpaceShareDTO.class);
        }

        return ConvertHelper.convert(workSpaceShareDO, WorkSpaceShareDTO.class);
    }

    @Override
    public WorkSpaceShareDTO update(Long id, WorkSpaceShareUpdateDTO workSpaceShareUpdateDTO) {
        if (!INITDATA.contains(workSpaceShareUpdateDTO.getType())) {
            throw new CommonException(ERROR_SHARE_TYPE);
        }
        WorkSpaceShareDO workSpaceShareDO = workSpaceShareRepository.selectById(id);
        if (!workSpaceShareDO.getType().equals(workSpaceShareUpdateDTO.getType())) {
            workSpaceShareDO.setType(workSpaceShareUpdateDTO.getType());
            workSpaceShareDO.setObjectVersionNumber(workSpaceShareUpdateDTO.getObjectVersionNumber());
            workSpaceShareDO = workSpaceShareRepository.update(workSpaceShareDO);
        }
        return ConvertHelper.convert(workSpaceShareDO, WorkSpaceShareDTO.class);
    }

    @Override
    public Map<String, Object> queryTree(String token) {
        WorkSpaceShareDO workSpaceShareDO = getWorkSpaceShare(token);
        if (workSpaceShareDO.getType().equals(BaseStage.SHARE_CURRENT)) {
            return workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDO.getWorkspaceId(), false);
        } else if (workSpaceShareDO.getType().equals(BaseStage.SHARE_INCLUDE)) {
            return workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDO.getWorkspaceId(), true);
        } else {
            throw new CommonException(NO_ACCESS);
        }
    }

    @Override
    public PageDTO queryPage(Long workSpaceId, String token) {
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceId);
        checkPermission(workSpacePageDO.getPageId(), token);
        return workSpaceService.queryDetail(null, null, workSpaceId, null);
    }

    @Override
    public List<PageAttachmentDTO> queryPageAttachment(Long pageId, String token) {
        checkPermission(pageId, token);
        return pageAttachmentService.queryByList(pageId);
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
        WorkSpaceShareDO workSpaceShareDO = getWorkSpaceShare(token);
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceShareDO.getWorkspaceId());
        switch (workSpaceShareDO.getType()) {
            case BaseStage.SHARE_CURRENT:
                if (pageId.equals(workSpacePageDO.getPageId())) {
                    flag = true;
                }
                break;
            case BaseStage.SHARE_INCLUDE:
                if (pageId.equals(workSpacePageDO.getPageId())) {
                    flag = true;
                } else {
                    //查出所有子空间
                    List<WorkSpaceDO> workSpaceList = workSpaceRepository.queryAllChildByWorkSpaceId(workSpaceShareDO.getWorkspaceId());
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
            case BaseStage.SHARE_DISABLE:
                flag = false;
                break;
        }
        if (!flag) {
            throw new CommonException(NO_ACCESS);
        }
    }

    private WorkSpaceShareDO getWorkSpaceShare(String token) {
        WorkSpaceShareDO workSpaceShareDO = new WorkSpaceShareDO();
        workSpaceShareDO.setToken(token);
        workSpaceShareDO = workSpaceShareRepository.selectOne(workSpaceShareDO);
        if (BaseStage.SHARE_DISABLE.equals(workSpaceShareDO.getType())) {
            throw new CommonException(NO_ACCESS);
        }
        return workSpaceShareDO;
    }

    @Override
    public void exportMd2Pdf(Long pageId, String token, HttpServletResponse response) {
        checkPermission(pageId, token);
        PageInfo pageInfo = pageRepository.queryShareInfoById(pageId);
        PdfUtil.markdown2Pdf(pageInfo.getTitle(), pageInfo.getContent(), response);
    }
}
