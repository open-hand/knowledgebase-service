package io.choerodon.kb.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.ProjectOperateProService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author superlee
 * @since 2022-05-12
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectOperateProServiceImpl implements ProjectOperateProService {

    @Autowired
    private IamFeignClient iamFeignClient;
    @Autowired
    private BaseFeignClient baseFeignClient;

    @Override
    public Page<ProjectDTO> pageProjectInfo(Long organizationId,
                                            Long projectId,
                                            PageRequest pageRequest,
                                            ProjectDTO project) {

        Set<Long> topProjectIds = project.getTopProjectIds();
        if (ObjectUtils.isEmpty(topProjectIds)) {
            return iamFeignClient.pagingQuery(organizationId, pageRequest.getPage(), pageRequest.getSize(), project.getName(), project.getCode(), project.getEnabled(), null, project.getParam())
                    .getBody();
        } else {
            //查所有
            Page<ProjectDTO> projects =
                    iamFeignClient.pagingQuery(organizationId, pageRequest.getPage(), 0, project.getName(), project.getCode(), project.getEnabled(), null, project.getParam())
                            .getBody();
            List<ProjectDTO> topProjects = baseFeignClient.queryProjectByIds(topProjectIds).getBody();
            List<ProjectDTO> projectList = projects.getContent();
            projectList = projectList.stream().filter(x -> !topProjectIds.contains(x.getId())).collect(Collectors.toList());
            int total = projectList.size();
            int size = pageRequest.getSize();
            if (size == 0) {
                topProjects.addAll(projectList);
                projects.setContent(topProjects);
                return projects;
            } else {
                int page = pageRequest.getPage();
                int totalPage = total / size;
                if (total % size != 0) {
                    totalPage = totalPage + 1;
                }

                int start = page * size;
                int end = (page + 1) * size;
                if (end > total) {
                    end = total;
                }
                List<ProjectDTO> subList;
                if (start <= end) {
                    subList = projectList.subList(start, end);
                } else {
                    subList = Collections.emptyList();
                }
                topProjects.addAll(subList);
                projects.setContent(topProjects);
                projects.setTotalPages(totalPage);
                projects.setTotalElements(total);
                projects.setSize(size);
                projects.setNumberOfElements(total);
                projects.setNumber(subList.size());
                return projects;
            }
        }
    }
}
