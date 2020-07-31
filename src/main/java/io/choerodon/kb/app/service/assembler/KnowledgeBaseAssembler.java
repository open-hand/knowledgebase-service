package io.choerodon.kb.app.service.assembler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.kb.infra.enums.OpenRangeType;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.WorkSpaceRecentVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class KnowledgeBaseAssembler {
    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private ModelMapper modelMapper;

    public KnowledgeBaseInfoVO dtoToInfoVO(KnowledgeBaseDTO knowledgeBaseDTO){
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        modelMapper.map(knowledgeBaseDTO,knowledgeBaseInfoVO);
        if(OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseDTO.getOpenRange())){
            Long[] map = modelMapper.map(knowledgeBaseDTO.getRangeProject().split(","), new TypeToken<Long[]>() {
            }.getType());
            knowledgeBaseInfoVO.setRangeProjectIds(Arrays.asList(map));
        }
        // 查询最近更新的用户名
        return knowledgeBaseInfoVO;
    }

    public void addUpdateUser(List<KnowledgeBaseListVO> knowledgeBaseListVOList, Long organizationId, Long projectId) {
        List<Long> baseIds = knowledgeBaseListVOList.stream().map(KnowledgeBaseListVO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(baseIds)) {
            return;
        }
        //查询组织/项目名称
        OrganizationDTO organizationDTO = baseFeignClient.query(organizationId).getBody();
        List<ProjectDO> projectDOS = baseFeignClient.listProjectsByOrgId(organizationId).getBody();
        Map<Long, String> map = projectDOS.stream().collect(Collectors.toMap(ProjectDO::getId, ProjectDO::getName));
        knowledgeBaseListVOList.forEach(baseListVO -> {
            if (baseListVO.getProjectId()==null) {
                baseListVO.setSource(organizationDTO.getTenantName());
            }else{
                baseListVO.setSource(map.get(baseListVO.getProjectId()));
            }
        });
        List<WorkSpaceRecentVO> querylatestWorkSpace = workSpaceMapper.querylatest(organizationId, projectId, baseIds);
        Map<Long, List<WorkSpaceRecentVO>> collect = querylatestWorkSpace.stream().collect(Collectors.groupingBy(WorkSpaceRecentVO::getBaseId));
        if (CollectionUtils.isEmpty(querylatestWorkSpace)) {
            return;
        }
        List<Long> userIds = querylatestWorkSpace.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        // 设置最近更新的文档
        knowledgeBaseListVOList.forEach(baseListVO -> {
            List<WorkSpaceRecentVO> workSpaceRecentVOS = collect.get(baseListVO.getId());
            if(!CollectionUtils.isEmpty(workSpaceRecentVOS)){
                workSpaceRecentVOS.forEach(work->handleWorkSpace(work,userDOMap,organizationId,projectId));
                baseListVO.setWorkSpaceRecents(workSpaceRecentVOS);
            }
        });
    }

    //处理route
    private void handleWorkSpace(WorkSpaceRecentVO workSpaceRecentVO, Map<Long, UserDO> userDOMap, Long organizationId, Long projectId) {
        workSpaceRecentVO.setLastUpdatedUser(userDOMap.get(workSpaceRecentVO.getLastUpdatedBy()));
        StringBuilder sb = new StringBuilder();
        String[] split = workSpaceRecentVO.getRoute().split("\\.");
        List<String> list = Arrays.asList(split);
        List<Long> spaceIds = list.stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectSpaceByIds(null, spaceIds);
        workSpaceDTOS.forEach(e -> sb.append(e.getName()).append("-"));
        if (sb.length() > 0) {
            String substring = sb.substring(0, sb.length() - 1);
            workSpaceRecentVO.setUpdateworkSpace(substring);
        }

    }

        public void handleUserInfo(List<RecycleVO> recycleList){
            List<Long> userIds = recycleList.stream().map(RecycleVO::getLastUpdatedBy).collect(Collectors.toList());
            Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
            recycleList.stream().forEach(e->e.setLastUpdatedUser(userDOMap.get(e.getLastUpdatedBy())));
        }
}
