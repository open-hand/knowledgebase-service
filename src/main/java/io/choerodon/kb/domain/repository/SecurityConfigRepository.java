package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.permission.CollaboratorSearchVO;
import io.choerodon.kb.domain.entity.SecurityConfig;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库安全设置资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface SecurityConfigRepository extends BaseRepository<SecurityConfig> {

    List<SecurityConfig> selectByTarget(Long organizationId, Long projectId, CollaboratorSearchVO searchVO);
}
