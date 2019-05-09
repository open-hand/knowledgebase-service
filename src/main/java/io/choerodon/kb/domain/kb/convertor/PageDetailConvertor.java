package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.domain.kb.entity.PageDetailE;
import io.choerodon.kb.infra.dataobject.PageDetailDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class PageDetailConvertor implements ConvertorI<PageDetailE, PageDetailDO, PageDTO> {

    @Override
    public PageDetailE doToEntity(PageDetailDO pageDetailDO) {
        PageDetailE pageDetailE = new PageDetailE();
        BeanUtils.copyProperties(pageDetailDO, pageDetailE);
        return pageDetailE;
    }

    @Override
    public PageDetailDO entityToDo(PageDetailE entity) {
        PageDetailDO pageDetailDO = new PageDetailDO();
        BeanUtils.copyProperties(entity, pageDetailDO);
        return pageDetailDO;
    }

    @Override
    public PageDTO entityToDto(PageDetailE entity) {
        PageDTO pageDTO = new PageDTO();
        BeanUtils.copyProperties(entity, pageDTO);
        return pageDTO;
    }
}
