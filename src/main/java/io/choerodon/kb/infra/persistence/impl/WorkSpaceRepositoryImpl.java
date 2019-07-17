package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class WorkSpaceRepositoryImpl implements WorkSpaceRepository {



    private WorkSpaceMapper workSpaceMapper;

    public WorkSpaceRepositoryImpl(WorkSpaceMapper workSpaceMapper) {
        this.workSpaceMapper = workSpaceMapper;
    }

}
