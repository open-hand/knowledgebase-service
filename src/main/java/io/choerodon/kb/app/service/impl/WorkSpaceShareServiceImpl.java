package io.choerodon.kb.app.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.WorkSpaceFirstTreeDTO;
import io.choerodon.kb.api.dao.WorkSpaceShareDTO;
import io.choerodon.kb.api.dao.WorkSpaceTreeDTO;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.kb.repository.WorkSpaceShareRepository;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareServiceImpl implements WorkSpaceShareService {

    private WorkSpaceService workSpaceService;
    private WorkSpaceRepository workSpaceRepository;
    private WorkSpaceShareRepository workSpaceShareRepository;

    public WorkSpaceShareServiceImpl(WorkSpaceService workSpaceService,
                                     WorkSpaceRepository workSpaceRepository,
                                     WorkSpaceShareRepository workSpaceShareRepository) {
        this.workSpaceService = workSpaceService;
        this.workSpaceRepository = workSpaceRepository;
        this.workSpaceShareRepository = workSpaceShareRepository;
    }

    @Override
    public WorkSpaceShareDTO create(Long workSpaceId, Boolean isContain) {
        WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceId);
        WorkSpaceShareDO workSpaceShareDO = new WorkSpaceShareDO();
        workSpaceShareDO.setWorkspaceId(workSpaceDO.getId());
        workSpaceShareDO.setContain(isContain);
        //生成16位的md5编码
        String md5Str = DigestUtils.md5Hex(TypeUtil.objToString(workSpaceDO.getId())).substring(8, 24);
        workSpaceShareDO.setToken(md5Str);
        return ConvertHelper.convert(workSpaceShareRepository.inset(workSpaceShareDO), WorkSpaceShareDTO.class);
    }

    @Override
    public WorkSpaceShareDTO query(Long id) {
        return ConvertHelper.convert(workSpaceShareRepository.selectById(id), WorkSpaceShareDTO.class);
    }

    @Override
    public WorkSpaceFirstTreeDTO queryTree(String token) {
        WorkSpaceShareDO workSpaceShareDO = new WorkSpaceShareDO();
        workSpaceShareDO.setToken(token);
        workSpaceShareDO = workSpaceShareRepository.selectOne(workSpaceShareDO);
        WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceShareDO.getWorkspaceId());
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.workSpacesByParentId(workSpaceDO.getId());
        WorkSpaceFirstTreeDTO workSpaceFirstTreeDTO = new WorkSpaceFirstTreeDTO();
        workSpaceFirstTreeDTO.setRootId(0L);
        workSpaceFirstTreeDTO.setItems(getWorkSpaceTopTreeList(workSpaceDOList, workSpaceTreeMap, workSpaceDO, workSpaceShareDO.getContain()));
        return workSpaceFirstTreeDTO;
    }

    @Override
    public PageDTO queryPage(String token) {
        WorkSpaceShareDO workSpaceShareDO = new WorkSpaceShareDO();
        workSpaceShareDO.setToken(token);
        workSpaceShareDO = workSpaceShareRepository.selectOne(workSpaceShareDO);
        return workSpaceService.queryDetail(workSpaceShareDO.getWorkspaceId());
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTopTreeList(List<WorkSpaceDO> workSpaceDOList,
                                                                Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                                WorkSpaceDO workSpaceDO,
                                                                Boolean contain) {
        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(workSpaceDO.getName());
        workSpaceTreeDTO.setData(data);
        workSpaceTreeDTO.setId(workSpaceDO.getId());
        workSpaceTreeDTO.setParentId(workSpaceDO.getParentId());
        workSpaceTreeDTO.setCreatedBy(workSpaceDO.getCreatedBy());
        if (workSpaceDOList.isEmpty() || !contain) {
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
}
