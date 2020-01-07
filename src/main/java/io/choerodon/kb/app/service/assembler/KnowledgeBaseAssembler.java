package io.choerodon.kb.app.service.assembler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
    private static final String RANGE_PROJECT= "range_project";
    private static final String RANGE_PUBLIC= "range_public";

    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private ModelMapper modelMapper;

    public KnowledgeBaseInfoVO dtoToInfoVO(KnowledgeBaseDTO knowledgeBaseDTO){
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        modelMapper.map(knowledgeBaseDTO,knowledgeBaseInfoVO);
        if(RANGE_PROJECT.equals(knowledgeBaseDTO.getOpenRange())){
            Long[] map = modelMapper.map(knowledgeBaseDTO.getRangeProject().split(","), new TypeToken<Long[]>() {
            }.getType());
            knowledgeBaseInfoVO.setRangeProjectIds(Arrays.asList(map));
        }
        // 查询最近更新的用户名
        return knowledgeBaseInfoVO;
    }

    public void docheage(List<KnowledgeBaseListVO> knowledgeBaseListVOList, Long organizationId, Long projectId) {
        if (CollectionUtils.isEmpty(knowledgeBaseListVOList)) {
            return;
        }
        List<Long> baseIds = knowledgeBaseListVOList.stream().map(KnowledgeBaseListVO::getId).collect(Collectors.toList());
        List<WorkSpaceRecentVO> querylatestWorkSpace = workSpaceMapper.querylatest(organizationId, projectId, baseIds);
        Map<Long, List<WorkSpaceRecentVO>> collect = querylatestWorkSpace.stream().collect(Collectors.groupingBy(WorkSpaceRecentVO::getBaseId));
        if (CollectionUtils.isEmpty(querylatestWorkSpace)) {
            return;
        }

        //查询组织/项目名称
        List<Long> userIds = querylatestWorkSpace.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        knowledgeBaseListVOList.forEach(baseListVO -> {
            OrganizationDTO organizationDTO = baseFeignClient.query(organizationId).getBody();
            if (baseListVO.getOpenRange().equals(RANGE_PUBLIC)) {
                baseListVO.setRangeName(organizationDTO.getName());
            }
            if (baseListVO.getOpenRange().equals(RANGE_PROJECT)) {
                List<ProjectDO> projectDOS = baseFeignClient.listProjectsByOrgId(organizationId).getBody();
                Map<Long, String> map = projectDOS.stream().collect(Collectors.toMap(ProjectDO::getId, ProjectDO::getName));
                baseListVO.setRangeName(map.get(baseListVO.getProjectId()));
            }
            for (Map.Entry<Long, List<WorkSpaceRecentVO>> workMap : collect.entrySet()) {
                if (baseListVO.getId().equals(workMap.getKey())) {
                    List<WorkSpaceRecentVO> value = workMap.getValue();
                    value.stream().forEach(work->handleWorkSpace(work,userDOMap,organizationId,projectId));
                    baseListVO.setWorkSpaceRecents(workMap.getValue());
                }
            }
        });
    }

    //处理route
    private void handleWorkSpace(WorkSpaceRecentVO workSpaceRecentVO, Map<Long, UserDO> userDOMap,Long organizationId,Long projectId) {
        workSpaceRecentVO.setLastUpdatedUser(userDOMap.get(workSpaceRecentVO.getLastUpdatedBy()));
        List<WorkSpaceDTO> workList = workSpaceMapper.queryAll(organizationId,projectId,null);
        if(!CollectionUtils.isEmpty(workList)){
            Map<Long, String> map = workList.stream().collect(Collectors.toMap(WorkSpaceDTO::getId, WorkSpaceDTO::getName));
            StringBuffer sb = new StringBuffer();
            String[] split = workSpaceRecentVO.getRoute().split("\\.");
            List<String> route = Arrays.asList(split);
            if (split.length > 1) {
                for (String id : route) {
                    sb.append(map.get(Long.valueOf(id)));
                    sb.append("-");
                }
                sb.replace(sb.length() - 1, sb.length(), "");
            } else {
                sb = new StringBuffer(workSpaceRecentVO.getTitle());
            }
            workSpaceRecentVO.setUpdateworkSpace(sb.toString());
        }
        }

        public void handleUserInfo(List<RecycleVO> recycleList){
            List<Long> userIds = recycleList.stream().map(RecycleVO::getLastUpdatedBy).collect(Collectors.toList());
            Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
            recycleList.stream().forEach(e->e.setLastUpdatedUser(userDOMap.get(e.getLastUpdatedBy())));
        }
}
