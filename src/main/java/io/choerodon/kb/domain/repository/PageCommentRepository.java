package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.infra.dto.PageCommentDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageCommentRepository extends BaseRepository<PageCommentDTO> {

    PageCommentDTO baseCreate(PageCommentDTO pageCommentDTO);

    PageCommentDTO baseUpdate(PageCommentDTO pageCommentDTO);

    PageCommentDTO baseQueryById(Long id);

    void baseDelete(Long id);

    void deleteByPageId(Long pageId);

    List<PageCommentVO> queryByPageId(Long organizationId, Long projectId, Long pageId);

    PageCommentVO getCommentInfo(PageCommentDTO pageCommentDTO);
}
