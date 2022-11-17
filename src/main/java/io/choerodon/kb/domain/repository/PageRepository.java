package io.choerodon.kb.domain.repository;

import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository extends BaseRepository<PageDTO> {

    PageDTO selectById(Long id);

    /**
     * 更新Page数据
     * @param pageDTO   Page数据
     * @param logUpdateAction   是否记录更新日志, 在切面中使用, 请勿移除此参数
     * @return 处理结果
     */
    PageDTO baseUpdate(PageDTO pageDTO, boolean logUpdateAction);

    void createOrUpdateEs(Long pageId);

    void deleteEs(Long pageId);

    void baseDelete(Long id);

    PageDTO baseCreate(PageDTO create);

    PageDTO baseQueryById(Long organizationId, Long projectId, Long pageId);

    PageDTO baseQueryByIdWithOrg(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryInfoById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryShareInfoById(Long pageId);

    void checkById(Long organizationId, Long projectId, Long pageId);

    PageContentDTO queryDraftContent(Long organizationId, Long projectId, Long pageId);

    /**
     * 更新Page标题
     * @param page   Page数据, 包含标题/是否同步ES标记/最后版本--可选
     * @param logUpdateAction   是否记录更新日志, 在切面中使用, 请勿移除此参数
     */
    void updatePageTitle(PageDTO page, boolean logUpdateAction);
}
