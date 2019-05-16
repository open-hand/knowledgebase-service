package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class PageAttachmentConvertor implements ConvertorI<Object, PageAttachmentDO, PageAttachmentDTO> {

    @Override
    public PageAttachmentDTO doToDto(PageAttachmentDO dataObject) {
        PageAttachmentDTO pageAttachmentDTO = new PageAttachmentDTO();
        BeanUtils.copyProperties(dataObject, pageAttachmentDTO);
        return pageAttachmentDTO;
    }
}
