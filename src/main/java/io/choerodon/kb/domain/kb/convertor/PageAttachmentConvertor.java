package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.api.dao.PageAttachmentVO;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class PageAttachmentConvertor implements ConvertorI<Object, PageAttachmentDTO, PageAttachmentVO> {

    @Override
    public PageAttachmentVO doToDto(PageAttachmentDTO dataObject) {
        PageAttachmentVO pageAttachmentVO = new PageAttachmentVO();
        BeanUtils.copyProperties(dataObject, pageAttachmentVO);
        return pageAttachmentVO;
    }
}
