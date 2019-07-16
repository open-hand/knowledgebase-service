package io.choerodon.kb.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.utils.PdfUtil;
import io.choerodon.kb.infra.utils.TypeUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
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
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public WorkSpaceShareVO query(Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceRepository.selectById(workSpaceId);
        WorkSpaceShareDTO workSpaceShareDTO = workSpaceShareRepository.selectByWorkSpaceId(workSpaceDTO.getId());

        if (workSpaceShareDTO == null) {
            WorkSpaceShareDTO workSpaceShare = new WorkSpaceShareDTO();
            workSpaceShare.setWorkspaceId(workSpaceDTO.getId());
            workSpaceShare.setType(BaseStage.SHARE_CURRENT);
            //生成16位的md5编码
            String md5Str = DigestUtils.md5Hex(TypeUtil.objToString(workSpaceDTO.getId())).substring(8, 24);
            workSpaceShare.setToken(md5Str);
            return ConvertHelper.convert(workSpaceShareRepository.baseCreate(workSpaceShare), WorkSpaceShareVO.class);
        }

        return ConvertHelper.convert(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

    @Override
    public WorkSpaceShareVO update(Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO) {
        if (!INITDATA.contains(workSpaceShareUpdateVO.getType())) {
            throw new CommonException(ERROR_SHARE_TYPE);
        }
        WorkSpaceShareDTO workSpaceShareDTO = workSpaceShareRepository.selectById(id);
        if (!workSpaceShareDTO.getType().equals(workSpaceShareUpdateVO.getType())) {
            workSpaceShareDTO.setType(workSpaceShareUpdateVO.getType());
            workSpaceShareDTO.setObjectVersionNumber(workSpaceShareUpdateVO.getObjectVersionNumber());
            workSpaceShareDTO = workSpaceShareRepository.baseUpdate(workSpaceShareDTO);
        }
        return ConvertHelper.convert(workSpaceShareDTO, WorkSpaceShareVO.class);
    }

    @Override
    public Map<String, Object> queryTree(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = getWorkSpaceShare(token);
        if (workSpaceShareDTO.getType().equals(BaseStage.SHARE_CURRENT)) {
            return workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), false);
        } else if (workSpaceShareDTO.getType().equals(BaseStage.SHARE_INCLUDE)) {
            return workSpaceService.queryAllChildTreeByWorkSpaceId(workSpaceShareDTO.getWorkspaceId(), true);
        } else {
            throw new CommonException(NO_ACCESS);
        }
    }

    @Override
    public PageVO queryPage(Long workSpaceId, String token) {
        WorkSpacePageDTO workSpacePageDTO = workSpacePageRepository.selectByWorkSpaceId(workSpaceId);
        checkPermission(workSpacePageDTO.getPageId(), token);
        return workSpaceService.queryDetail(null, null, workSpaceId, null);
    }

    @Override
    public List<PageAttachmentVO> queryPageAttachment(Long pageId, String token) {
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
        WorkSpaceShareDTO workSpaceShareDTO = getWorkSpaceShare(token);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageRepository.selectByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
        switch (workSpaceShareDTO.getType()) {
            case BaseStage.SHARE_CURRENT:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                }
                break;
            case BaseStage.SHARE_INCLUDE:
                if (pageId.equals(workSpacePageDTO.getPageId())) {
                    flag = true;
                } else {
                    //查出所有子空间
                    List<WorkSpaceDTO> workSpaceList = workSpaceRepository.queryAllChildByWorkSpaceId(workSpaceShareDTO.getWorkspaceId());
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

    private WorkSpaceShareDTO getWorkSpaceShare(String token) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setToken(token);
        workSpaceShareDTO = workSpaceShareRepository.selectOne(workSpaceShareDTO);
        if (BaseStage.SHARE_DISABLE.equals(workSpaceShareDTO.getType())) {
            throw new CommonException(NO_ACCESS);
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
