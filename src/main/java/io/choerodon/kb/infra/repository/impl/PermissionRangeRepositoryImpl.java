package io.choerodon.kb.infra.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;
import io.choerodon.kb.infra.mapper.PermissionRangeMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限应用范围 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Component
public class PermissionRangeRepositoryImpl extends BaseRepositoryImpl<PermissionRange> implements PermissionRangeRepository {

    @Autowired
    private PermissionRangeMapper permissionRangeMapper;

}
