package io.choerodon.kb.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.domain.kb.repository.WorkSpacePageRepository;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.kb.repository.WorkSpaceShareRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.utils.PdfUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
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

    private WorkSpaceService workSpaceService;
    private WorkSpaceRepository workSpaceRepository;
    private WorkSpaceShareRepository workSpaceShareRepository;
    private PageAttachmentService pageAttachmentService;
    private WorkSpacePageRepository workSpacePageRepository;
    private PageRepository pageRepository;

    public WorkSpaceShareServiceImpl(WorkSpaceService workSpaceService,
                                     WorkSpaceRepository workSpaceRepository,
                                     WorkSpaceShareRepository workSpaceShareRepository,
                                     PageAttachmentService pageAttachmentService,
                                     WorkSpacePageRepository workSpacePageRepository,
                                     PageRepository pageRepository) {
        this.workSpaceService = workSpaceService;
        this.workSpaceRepository = workSpaceRepository;
        this.workSpaceShareRepository = workSpaceShareRepository;
        this.pageAttachmentService = pageAttachmentService;
        this.workSpacePageRepository = workSpacePageRepository;
        this.pageRepository = pageRepository;
    }

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
            return ConvertHelper.convert(workSpaceShareRepository.inset(workSpaceShare), WorkSpaceShareDTO.class);
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
    public WorkSpaceFirstTreeDTO queryTree(String token) {
        WorkSpaceShareDO workSpaceShareDO = getWorkSpaceShare(token);
        WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceShareDO.getWorkspaceId());
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.workSpacesByParentId(workSpaceDO.getId());
        WorkSpaceFirstTreeDTO workSpaceFirstTreeDTO = new WorkSpaceFirstTreeDTO();
        workSpaceFirstTreeDTO.setRootId(0L);
        workSpaceFirstTreeDTO.setItems(getWorkSpaceTopTreeList(workSpaceDOList,
                workSpaceTreeMap,
                workSpaceDO,
                workSpaceShareDO.getType().equals(BaseStage.SHARE_INCLUDE)));
        return workSpaceFirstTreeDTO;
    }

    @Override
    public PageDTO queryPage(Long workSpaceId, String token) {
        PageDTO pageDTO = new PageDTO();
        WorkSpaceShareDO workSpaceShareDO = getWorkSpaceShare(token);
        if (BaseStage.SHARE_CURRENT.equals(workSpaceShareDO.getType())) {
            pageDTO = workSpaceService.queryDetail(workSpaceShareDO.getWorkspaceId());
        } else {
            if (!workSpaceId.equals(workSpaceShareDO.getWorkspaceId())) {
                WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceShareDO.getWorkspaceId());
                List<WorkSpaceDO> workSpaceList = workSpaceRepository.selectByRoute(workSpaceDO.getRoute());
                if (workSpaceList != null && !workSpaceList.isEmpty()) {
                    if (workSpaceList.stream().filter(WorkSpace -> WorkSpace.getId().equals(workSpaceId)).count() > 0) {
                        pageDTO = workSpaceService.queryDetail(workSpaceId);
                    } else {
                        throw new CommonException(NO_ACCESS);
                    }
                }
            } else {
                pageDTO = workSpaceService.queryDetail(workSpaceShareDO.getWorkspaceId());
            }
        }
        return pageDTO;
    }

    @Override
    public List<PageAttachmentDTO> queryPageAttachment(Long workSpaceId, String token) {
        List<PageAttachmentDTO> attachments = new ArrayList<>();
        WorkSpaceShareDO workSpaceShareDO = getWorkSpaceShare(token);
        if (BaseStage.SHARE_CURRENT.equals(workSpaceShareDO.getType())) {
            WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceShareDO.getWorkspaceId());
            attachments = pageAttachmentService.queryByList(workSpacePageDO.getPageId());
        } else {
            if (!workSpaceId.equals(workSpaceShareDO.getWorkspaceId())) {
                WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceShareDO.getWorkspaceId());
                List<WorkSpaceDO> workSpaceList = workSpaceRepository.selectByRoute(workSpaceDO.getRoute());
                if (workSpaceList != null && !workSpaceList.isEmpty()) {
                    if (workSpaceList.stream().filter(WorkSpace -> WorkSpace.getId().equals(workSpaceId)).count() > 0) {
                        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceId);
                        attachments = pageAttachmentService.queryByList(workSpacePageDO.getPageId());
                    } else {
                        throw new CommonException(NO_ACCESS);
                    }
                }
            } else {
                WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceShareDO.getWorkspaceId());
                attachments = pageAttachmentService.queryByList(workSpacePageDO.getPageId());
            }
        }
        return attachments;
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

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTopTreeList(List<WorkSpaceDO> workSpaceDOList,
                                                                Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                                WorkSpaceDO workSpaceDO,
                                                                Boolean include) {
        WorkSpaceTreeDTO parentTree = new WorkSpaceTreeDTO();
        WorkSpaceTreeDTO.Data parentData = new WorkSpaceTreeDTO.Data();
        parentData.setTitle("choerodon");
        parentTree.setData(parentData);
        parentTree.setId(0L);
        parentTree.setParentId(0L);
        parentTree.setCreatedBy(workSpaceDO.getCreatedBy());
        parentTree.setHasChildren(true);
        parentTree.setChildren(Stream.of(workSpaceDO.getId()).collect(toList()));
        workSpaceTreeMap.put(parentTree.getId(), parentTree);

        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(workSpaceDO.getName());
        workSpaceTreeDTO.setData(data);
        workSpaceTreeDTO.setId(workSpaceDO.getId());
        workSpaceTreeDTO.setParentId(workSpaceDO.getParentId());
        workSpaceTreeDTO.setCreatedBy(workSpaceDO.getCreatedBy());
        if (workSpaceDOList.isEmpty() || !include) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeDTO.setHasChildren(true);
            List<Long> children = workSpaceDOList.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
            workSpaceTreeDTO.setChildren(children);
            getWorkSpaceTreeList(workSpaceDOList, workSpaceTreeMap, Collections.emptyList());
        }
        workSpaceTreeMap.put(workSpaceTreeDTO.getId(), workSpaceTreeDTO);
        return workSpaceTreeMap;
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTreeList(List<WorkSpaceDO> workSpaceDOList,
                                                             Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                             List<Long> routes) {
        for (WorkSpaceDO w : workSpaceDOList) {
            WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
            WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
            workSpaceTreeDTO.setId(w.getId());
            workSpaceTreeDTO.setParentId(w.getParentId());
            workSpaceTreeDTO.setCreatedBy(w.getCreatedBy());
            if (routes.contains(w.getId())) {
                workSpaceTreeDTO.setIsExpanded(true);
            }
            data.setTitle(w.getName());
            List<WorkSpaceDO> list = workSpaceRepository.workSpacesByParentId(w.getId());
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
                workSpaceTreeDTO.setChildren(children);
            }
            workSpaceTreeDTO.setData(data);
            workSpaceTreeMap.put(workSpaceTreeDTO.getId(), workSpaceTreeDTO);
            getWorkSpaceTreeList(list, workSpaceTreeMap, routes);
        }
        return workSpaceTreeMap;
    }

    @Override
    public void exportMd2Pdf(Long pageId, HttpServletResponse response) {
        PageInfo pageInfo = pageRepository.queryShareInfoById(pageId);
        PdfUtil.markdown2Pdf(pageInfo.getTitle(), pageInfo.getContent(), response);
    }
}
