package io.choerodon.kb.app.service.assembler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

import org.hzero.core.base.BaseConstants;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class KnowledgeBaseAssembler {
    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private IamRemoteRepository iamRemoteRepository;

    @Autowired
    private ModelMapper modelMapper;

    public KnowledgeBaseInfoVO dtoToInfoVO(KnowledgeBaseDTO knowledgeBaseDTO) {
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        modelMapper.map(knowledgeBaseDTO, knowledgeBaseInfoVO);
        if (OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseDTO.getOpenRange())) {
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
        // 查询组织/项目名称
        OrganizationDTO organizationDTO = iamRemoteRepository.queryOrganizationById(organizationId);
        Assert.notNull(organizationDTO, BaseConstants.ErrorCode.NOT_NULL);
        // 对于汉得信息的项目很多 不能这样去查询
        // List<ProjectDO> projectDOS = iamFeignClient.listProjectsByOrgId(organizationId).getBody();
        // Map<Long, String> map = projectDOS.stream().collect(Collectors.toMap(ProjectDO::getId, ProjectDO::getName));
        ProjectDTO projectDTO = null;
        if (projectId != null) {
            projectDTO = iamRemoteRepository.queryProjectById(projectId);
        }
        final ProjectDTO finalProjectDTO = projectDTO;
        for (KnowledgeBaseListVO knowledgeBaseListVO : knowledgeBaseListVOList) {
            if (knowledgeBaseListVO.getProjectId() == null) {
                knowledgeBaseListVO.setSource(organizationDTO.getTenantName());
            } else {
                knowledgeBaseListVO.setSource(finalProjectDTO != null ? finalProjectDTO.getName() : null);
            }
        }
        List<WorkSpaceRecentVO> queryLatestWorkSpace = workSpaceMapper.querylatest(organizationId, projectId, baseIds);
        if (CollectionUtils.isEmpty(queryLatestWorkSpace)) {
            return;
        }
        Map<Long, List<WorkSpaceRecentVO>> collect = queryLatestWorkSpace.stream().collect(Collectors.groupingBy(WorkSpaceRecentVO::getBaseId));
        final List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(
                queryLatestWorkSpace.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList()),
                false
        );
        if(CollectionUtils.isEmpty(userDOList)) {
            return;
        }
        Map<Long, UserDO> userDOMap = userDOList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
        // 设置最近更新的文档
        for (KnowledgeBaseListVO baseListVO : knowledgeBaseListVOList) {
            List<WorkSpaceRecentVO> workSpaceRecentVOS = collect.get(baseListVO.getId());
            if (CollectionUtils.isNotEmpty(workSpaceRecentVOS)) {
                for (WorkSpaceRecentVO work : workSpaceRecentVOS) {
                    handleWorkSpace(work, userDOMap);
                }
                baseListVO.setWorkSpaceRecents(workSpaceRecentVOS);
            }
        }
    }

    //处理route
    private void handleWorkSpace(WorkSpaceRecentVO workSpaceRecentVO, Map<Long, UserDO> userDOMap) {
        workSpaceRecentVO.setLastUpdatedUser(userDOMap.get(workSpaceRecentVO.getLastUpdatedBy()));
        StringBuilder sb = new StringBuilder();
        String[] split = workSpaceRecentVO.getRoute().split("\\.");
        List<String> list = Arrays.asList(split);
        List<Long> spaceIds = list.stream().map(Long::valueOf).collect(Collectors.toList());
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectSpaceByIds(null, spaceIds);
        workSpaceDTOS.forEach(e -> sb.append(e.getName()).append("-"));
        if (sb.length() > 0) {
            String substring = sb.substring(0, sb.length() - 1);
            workSpaceRecentVO.setUpdateworkSpace(substring);
        }

    }

    public void handleUserInfo(List<RecycleVO> recycleList) {
        final List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(
                recycleList.stream().map(RecycleVO::getLastUpdatedBy).collect(Collectors.toList()),
                false
        );
        Map<Long, UserDO> userDOMap = userDOList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
        for (RecycleVO e : recycleList) {
            e.setLastUpdatedUser(userDOMap.get(e.getLastUpdatedBy()));
        }
    }
}
