package io.choerodon.kb.infra.repository.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;

import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * @author superlee
 * @since 2022-09-28
 */
@Service
public class KnowledgeBaseRepositoryImpl extends BaseRepositoryImpl<KnowledgeBaseDTO> implements KnowledgeBaseRepository {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private IamRemoteRepository iamRemoteRepository;

    @Override
    public KnowledgeBaseDTO findKnowledgeBaseByCondition(KnowledgeBaseDTO queryParam) {
        return this.knowledgeBaseMapper.findKnowledgeBaseByCondition(queryParam);
    }

    @Override
    public List<KnowledgeBaseDTO> listKnowledgeBase(Long organizationId, Long projectId) {
        if(organizationId == null || projectId == null) {
            return Collections.emptyList();
        }
        return this.knowledgeBaseMapper.listKnowledgeBase(organizationId, projectId);
    }

    @Override
    public boolean checkOpenRangeCanAccess(Long organizationId, Long knowledgeBaseId) {
        if(organizationId == null || knowledgeBaseId == null) {
            // 无效的数据, 返回false
            return false;
        }
        final KnowledgeBaseDTO knowledgeBaseDTO = this.knowledgeBaseMapper.selectByPrimaryKey(knowledgeBaseId);
        if(knowledgeBaseDTO == null) {
            // 查不到知识库, 返回false
            return false;
        }

        final String openRange = knowledgeBaseDTO.getOpenRange();
        if(OpenRangeType.RANGE_PUBLIC.getType().equals(openRange)) {
            // 公开知识库, 返回true
            return true;
        }
        if(OpenRangeType.RANGE_PRIVATE.getType().equals(openRange)) {
            // 非公开知识库, 返回false
            return false;
        }
        if(OpenRangeType.RANGE_PROJECT.getType().equals(openRange)) {
            final String rangeProjectCsv = knowledgeBaseDTO.getRangeProject();
            if(StringUtils.isBlank(rangeProjectCsv)) {
                // 没有设置公开范围, 返回false
                return false;
            }
            final Set<Long> rangeProjectIds = Arrays.stream(StringUtils.split(rangeProjectCsv, BaseConstants.Symbol.COMMA))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            if(CollectionUtils.isEmpty(rangeProjectIds)) {
                // 没有设置公开范围, 返回false
                return false;
            }
            final CustomUserDetails userDetails = DetailsHelper.getUserDetails();
            if(userDetails == null || userDetails.getUserId() == null) {
                // 用户未登录, 返回false
                return false;
            }
            final Long currentUserId = userDetails.getUserId();
            final List<ProjectDTO> projectDTOS = this.iamRemoteRepository.queryOrgProjects(organizationId, currentUserId);
            if(CollectionUtils.isEmpty(projectDTOS)) {
                // 用户在该组织下没有项目, 返回false
                return false;
            }
            // 知识库公开范围和用户项目权限范围做交集
            final Set<Long> projectIds = projectDTOS.stream().map(ProjectDTO::getId).collect(Collectors.toSet());
            final Set<Long> intersection = SetUtils.intersection(rangeProjectIds, projectIds);
            // 有交集返回true否则返回false
            return !CollectionUtils.isEmpty(intersection);
        } else {
            // 无效的公开范围, 报错
            throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
        }
    }
}
