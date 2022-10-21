package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;
import io.choerodon.kb.infra.utils.RankUtil;

import org.hzero.core.base.BaseConstants;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    @Autowired
    private PageService pageService;

    @Autowired
    private KnowledgeBaseAssembler knowledgeBaseAssembler;
    @Autowired
    private BaseFeignClient baseFeignClient;


    @Override
    public KnowledgeBaseDTO baseInsert(KnowledgeBaseDTO knowledgeBaseDTO) {
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.insert.knowledge.base.is.null");
        }
        if (knowledgeBaseMapper.insertSelective(knowledgeBaseDTO) != 1) {
            throw new CommonException("error.insert.knowledge.base");
        }
        return knowledgeBaseMapper.selectByPrimaryKey(knowledgeBaseDTO.getId());
    }

    @Override
    public KnowledgeBaseDTO baseUpdate(KnowledgeBaseDTO knowledgeBaseDTO) {
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.update.knowledge.base.is.null");
        }
        if (knowledgeBaseMapper.updateByPrimaryKeySelective(knowledgeBaseDTO) != 1) {
            throw new CommonException("error.update.knowledge.base");
        }
        return knowledgeBaseMapper.selectByPrimaryKey(knowledgeBaseDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseInfoVO create(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        KnowledgeBaseDTO knowledgeBaseDTO = modelMapper.map(knowledgeBaseInfoVO, KnowledgeBaseDTO.class);
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setOrganizationId(organizationId);
        // 公开范围
        if (OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseInfoVO.getOpenRange())) {
            List<Long> rangeProjectIds = knowledgeBaseInfoVO.getRangeProjectIds();
            if (CollectionUtils.isEmpty(rangeProjectIds)) {
                throw new CommonException("error.range.project.of.at.least.one.project");
            }
            knowledgeBaseDTO.setRangeProject(StringUtils.join(rangeProjectIds, ","));
        }
        // 插入数据库
        KnowledgeBaseDTO knowledgeBaseDTO1 = baseInsert(knowledgeBaseDTO);
        // 是否按模板创建知识库
        if (knowledgeBaseInfoVO.getTemplateBaseId() != null) {
            pageService.createByTemplate(organizationId, projectId, knowledgeBaseDTO1.getId(), knowledgeBaseInfoVO.getTemplateBaseId());
        }
        //创建知识库的同时需要创建一个默认的文件夹
        createDefaultFolder(organizationId, projectId, knowledgeBaseDTO1);
        //返回给前端
        return knowledgeBaseAssembler.dtoToInfoVO(knowledgeBaseDTO1);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDefaultFolder(Long organizationId, Long projectId, KnowledgeBaseDTO knowledgeBaseDTO1) {
        WorkSpaceDTO spaceDTO = new WorkSpaceDTO();
        spaceDTO.setParentId(0L);
        spaceDTO.setType(WorkSpaceType.FOLDER.getValue());
        spaceDTO.setOrganizationId(organizationId);
        spaceDTO.setProjectId(projectId);
        spaceDTO.setName(knowledgeBaseDTO1.getName());
        spaceDTO.setBaseId(knowledgeBaseDTO1.getId());
        spaceDTO.setDescription(knowledgeBaseDTO1.getDescription());
        spaceDTO.setRank(RankUtil.mid());
        String route = "";
        WorkSpaceDTO workSpaceDTO = workSpaceService.baseCreate(spaceDTO);
        //设置新的route
        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        workSpaceDTO.setRoute(realRoute);
        workSpaceService.baseUpdate(workSpaceDTO);
    }

    @Override
    public KnowledgeBaseInfoVO update(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        knowledgeBaseInfoVO.setProjectId(projectId);
        knowledgeBaseInfoVO.setOrganizationId(organizationId);
        KnowledgeBaseDTO knowledgeBaseDTO = modelMapper.map(knowledgeBaseInfoVO, KnowledgeBaseDTO.class);
        if (OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseInfoVO.getOpenRange())) {
            List<Long> rangeProjectIds = knowledgeBaseInfoVO.getRangeProjectIds();
            if (CollectionUtils.isEmpty(rangeProjectIds)) {
                throw new CommonException("error.range.project.of.at.least.one.project");
            }
            knowledgeBaseDTO.setRangeProject(StringUtils.join(rangeProjectIds, ","));
        }
        return knowledgeBaseAssembler.dtoToInfoVO(baseUpdate(knowledgeBaseDTO));
    }

    @Override
    public void removeKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selectOne(knowledgeBaseDTO);
        knowledgeBaseDTO.setDelete(true);
        baseUpdate(knowledgeBaseDTO);

    }

    @Override
    public void deleteKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        // 彻底删除知识库下面所有的文件
        workSpaceService.deleteWorkSpaceByBaseId(organizationId, projectId, baseId);
        // 删除知识库
        knowledgeBaseMapper.deleteByPrimaryKey(baseId);

    }

    @Override
    public List<List<KnowledgeBaseListVO>> queryKnowledgeBaseWithRecent(Long organizationId, Long projectId) {
        List<KnowledgeBaseListVO> knowledgeBaseListVOS = knowledgeBaseMapper.queryKnowledgeBaseList(projectId, organizationId);
        knowledgeBaseAssembler.addUpdateUser(knowledgeBaseListVOS, organizationId, projectId);
        List<List<KnowledgeBaseListVO>> lists = new ArrayList<>();
        if (projectId != null) {
            List<KnowledgeBaseListVO> projectlist = knowledgeBaseListVOS.stream().
                    filter(e -> projectId.equals(e.getProjectId())).collect(Collectors.toList());
            List<KnowledgeBaseListVO> otherProjectList = knowledgeBaseListVOS.stream().
                    filter(e -> !projectId.equals(e.getProjectId())).collect(Collectors.toList());
            lists.add(projectlist);
            lists.add(otherProjectList);
        } else {
            lists.add(knowledgeBaseListVOS);
        }

        return lists;
    }

    @Override
    public void restoreKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selectOne(knowledgeBaseDTO);
        knowledgeBaseDTO.setDelete(false);
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.update.knowledge.base.is.null");
        }
        baseUpdate(knowledgeBaseDTO);

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
            final List<ProjectDTO> projectDTOS = this.baseFeignClient.queryOrgProjects(organizationId, currentUserId).getBody();
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
