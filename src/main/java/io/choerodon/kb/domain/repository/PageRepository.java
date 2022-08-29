package io.choerodon.kb.domain.repository;

import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.infra.dto.PageDTO;
import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository extends BaseRepository<PageDTO> {

    PageDTO selectById(Long id);

    PageDTO baseUpdate(PageDTO pageDTO, Boolean flag);

    void createOrUpdateEs(Long pageId);

    void deleteEs(Long pageId);

    void baseDelete(Long id);

    PageDTO baseCreate(PageDTO create);

    PageDTO baseQueryById(Long organizationId, Long projectId, Long pageId);

    PageDTO baseQueryByIdWithOrg(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryInfoById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryShareInfoById(Long pageId);

    void checkById(Long organizationId, Long projectId, Long pageId);
}
