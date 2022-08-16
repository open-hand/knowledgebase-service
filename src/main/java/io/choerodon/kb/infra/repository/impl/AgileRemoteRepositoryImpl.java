package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.domain.repository.AgileRemoteRepository;
import io.choerodon.kb.infra.feign.AgileFeignClient;

import org.hzero.core.util.ResponseUtils;

/**
 * @author zhaotianxin
 * @date 2021-01-21 13:50
 */
@Repository
public class AgileRemoteRepositoryImpl implements AgileRemoteRepository {
    @Autowired
    private AgileFeignClient agileFeignClient;

    @Override
    public List<ProjectDTO> deleteByWorkSpaceId(Long projectId, Long spaceId) {
        return ResponseUtils.getResponse(
                this.agileFeignClient.deleteByWorkSpaceId(projectId, spaceId),
                new TypeReference<List<ProjectDTO>>() {}
        );
    }
}
