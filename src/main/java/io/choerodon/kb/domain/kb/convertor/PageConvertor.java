//package io.choerodon.kb.domain.kb.convertor;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.PageE;
//import io.choerodon.kb.infra.dataobject.PageDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class PageConvertor implements ConvertorI<PageE, PageDO, Object> {
//
//    @Override
//    public PageE doToEntity(PageDO pageDO) {
//        PageE pageE = new PageE();
//        BeanUtils.copyProperties(pageDO, pageE);
//        return pageE;
//    }
//
//    @Override
//    public PageDO entityToDo(PageE entity) {
//        PageDO pageDO = new PageDO();
//        BeanUtils.copyProperties(entity, pageDO);
//        return pageDO;
//    }
//}
