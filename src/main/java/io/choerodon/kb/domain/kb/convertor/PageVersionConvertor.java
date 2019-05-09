package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.domain.kb.entity.PageVersionE;
import io.choerodon.kb.infra.dataobject.PageVersionDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class PageVersionConvertor implements ConvertorI<PageVersionE, PageVersionDO, Object> {

    @Override
    public PageVersionE doToEntity(PageVersionDO pageVersionDO) {
        PageVersionE pageVersionE = new PageVersionE();
        BeanUtils.copyProperties(pageVersionDO, pageVersionE);
        return pageVersionE;
    }

    @Override
    public PageVersionDO entityToDo(PageVersionE entity) {
        PageVersionDO pageVersionDO = new PageVersionDO();
        BeanUtils.copyProperties(entity, pageVersionDO);
        return pageVersionDO;
    }
}
