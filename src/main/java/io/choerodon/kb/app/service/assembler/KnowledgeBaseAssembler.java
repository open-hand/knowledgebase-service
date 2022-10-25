package io.choerodon.kb.app.service.assembler;

import java.util.*;
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

    /**
     * 处理最后更新人等附件信息
     * @param knowledgeBaseList 待处理的数据
     * @param organizationId    组织ID
     */
    public void addUpdateUser(List<KnowledgeBaseListVO> knowledgeBaseList, Long organizationId) {
        if(organizationId == null || CollectionUtils.isEmpty(knowledgeBaseList)) {
            return;
        }
        List<Long> baseIds = knowledgeBaseList.stream().map(KnowledgeBaseListVO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(baseIds)) {
            return;
        }
        // 查询组织名称
        OrganizationDTO organizationDTO = iamRemoteRepository.queryOrganizationById(organizationId);
        Assert.notNull(organizationDTO, BaseConstants.ErrorCode.NOT_NULL);
        // 查询项目名称
        final Set<Long> projectIds = knowledgeBaseList.stream()
                .filter(Objects::nonNull)
                .map(KnowledgeBaseListVO::getProjectId)
                .collect(Collectors.toSet());
        final List<ProjectDTO> projects = Optional.ofNullable(this.iamRemoteRepository.queryProjectByIds(projectIds)).orElse(Collections.emptyList());
        final Map<Long, ProjectDTO> projectIdToEntityMap = projects.stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));

        for (KnowledgeBaseListVO knowledgeBase : knowledgeBaseList) {
            final Long projectId = knowledgeBase.getProjectId();
            if (projectId == null) {
                knowledgeBase.setSource(organizationDTO.getTenantName());
            } else {
                final ProjectDTO projectInfo = projectIdToEntityMap.get(projectId);
                knowledgeBase.setSource(projectInfo != null ? projectInfo.getName() : null);
            }
        }
        List<WorkSpaceSimpleVO> queryLatestWorkSpace = workSpaceMapper.queryLatest(organizationId, baseIds);
        if (CollectionUtils.isEmpty(queryLatestWorkSpace)) {
            return;
        }
        Map<Long, List<WorkSpaceSimpleVO>> collect = queryLatestWorkSpace.stream().collect(Collectors.groupingBy(WorkSpaceSimpleVO::getBaseId));
        final List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(
                queryLatestWorkSpace.stream().map(WorkSpaceSimpleVO::getLastUpdatedBy).collect(Collectors.toList()),
                false
        );
        if(CollectionUtils.isEmpty(userDOList)) {
            return;
        }
        Map<Long, UserDO> userDOMap = userDOList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
        // 设置最近更新的文档
        for (KnowledgeBaseListVO baseListVO : knowledgeBaseList) {
            List<WorkSpaceSimpleVO> workSpaceSimpleVOS = collect.get(baseListVO.getId());
            if (CollectionUtils.isNotEmpty(workSpaceSimpleVOS)) {
                for (WorkSpaceSimpleVO work : workSpaceSimpleVOS) {
                    handleWorkSpace(work, userDOMap);
                }
                baseListVO.setWorkSpaceRecents(workSpaceSimpleVOS);
            }
        }
    }

    //处理route
    private void handleWorkSpace(WorkSpaceSimpleVO workSpaceSimpleVO, Map<Long, UserDO> userDOMap) {
        workSpaceSimpleVO.setLastUpdatedUser(userDOMap.get(workSpaceSimpleVO.getLastUpdatedBy()));
        StringBuilder sb = new StringBuilder();
        String[] split = workSpaceSimpleVO.getRoute().split("\\.");
        List<String> list = Arrays.asList(split);
        List<Long> spaceIds = list.stream().map(Long::valueOf).collect(Collectors.toList());
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectSpaceByIds(null, spaceIds);
        workSpaceDTOS.forEach(e -> sb.append(e.getName()).append("-"));
        if (sb.length() > 0) {
            String substring = sb.substring(0, sb.length() - 1);
            workSpaceSimpleVO.setUpdateworkSpace(substring);
        }

    }

    public void handleUserInfo(Collection<RecycleVO> recycleList) {
        final List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(
                recycleList.stream().map(RecycleVO::getLastUpdatedBy).collect(Collectors.toSet()),
                false
        );
        Map<Long, UserDO> userDOMap = userDOList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
        for (RecycleVO e : recycleList) {
            e.setLastUpdatedUser(userDOMap.get(e.getLastUpdatedBy()));
        }
    }
}
