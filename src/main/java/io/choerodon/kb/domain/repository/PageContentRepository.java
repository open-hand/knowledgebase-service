package io.choerodon.kb.domain.repository;

import io.choerodon.kb.infra.dto.PageContentDTO;

import org.hzero.mybatis.base.BaseRepository;

public interface PageContentRepository extends BaseRepository<PageContentDTO> {
    PageContentDTO selectLatestByPageId(Long pageId);
}
