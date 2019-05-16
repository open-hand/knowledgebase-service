//package io.choerodon.kb.domain.kb.convertor;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.PageContentE;
//import io.choerodon.kb.infra.dataobject.PageContentDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class PageContentConvertor implements ConvertorI<PageContentE, PageContentDO, Object> {
//
//    @Override
//    public PageContentE doToEntity(PageContentDO pageContentDO) {
//        PageContentE pageContentE = new PageContentE();
//        BeanUtils.copyProperties(pageContentDO, pageContentE);
//        return pageContentE;
//    }
//
//    @Override
//    public PageContentDO entityToDo(PageContentE entity) {
//        PageContentDO pageContentDO = new PageContentDO();
//        BeanUtils.copyProperties(entity, pageContentDO);
//        return pageContentDO;
//    }
//}
