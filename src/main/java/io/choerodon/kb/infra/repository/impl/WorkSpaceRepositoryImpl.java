package io.choerodon.kb.infra.repository.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * Copyright (c) 2022. Hand Enterprise Solution Company. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/8/29
 */
@Repository
public class WorkSpaceRepositoryImpl extends BaseRepositoryImpl<WorkSpaceDTO> implements WorkSpaceRepository {

    private final WorkSpaceMapper workSpaceMapper;

    public WorkSpaceRepositoryImpl(WorkSpaceMapper workSpaceMapper) {
        this.workSpaceMapper = workSpaceMapper;
    }

    @Override
    public List<WorkSpaceDTO> selectErrorRoute() {
        return workSpaceMapper.selectErrorRoute();
    }

    @Override
    public List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds) {
        if(CollectionUtils.isEmpty(workSpaceIds)) {
            return Collections.emptyList();
        }
        return this.workSpaceMapper.selectWorkSpaceNameByIds(workSpaceIds);
    }
}
