package io.choerodon.kb.infra.repository.impl;

import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.repository.PageContentRepository;
import io.choerodon.kb.infra.dto.PageContentDTO;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

@Repository
public class PageContentRepositoryImpl extends BaseRepositoryImpl<PageContentDTO> implements PageContentRepository {
}
