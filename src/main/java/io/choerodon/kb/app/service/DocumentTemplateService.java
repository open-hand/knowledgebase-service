package io.choerodon.kb.app.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import io.choerodon.kb.api.vo.DocumentTemplateInfoVO;
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.PageUpdateVO;
import io.choerodon.kb.api.vo.SearchVO;
import org.springframework.data.domain.Pageable;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public interface DocumentTemplateService {
    /**
     * 在知识库下创建模板文档
     * @param projectId
     * @param organizationId
     * @param pageCreateVO
     * @return
     */
    DocumentTemplateInfoVO createTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO);

    /**
     * 在知识库下修改模板
     * @param organizationId
     * @param projectId
     * @param id
     * @param searchStr
     * @param pageUpdateVO
     * @return
     */
    DocumentTemplateInfoVO updateTemplate(Long organizationId, Long projectId, Long id, String searchStr, PageUpdateVO pageUpdateVO);

    /**
     * 分页查询知识库下面的模板文档
     * @param organizationId
     * @param projectId
     * @param pageable
     * @param searchVO
     * @return
     */
    PageInfo<DocumentTemplateInfoVO> listTemplate(Long organizationId, Long projectId, Long baseId, Pageable pageable, SearchVO searchVO);
}
