//package io.choerodon.kb.domain.kb.convertor.iam;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.iam.OrganizationE;
//import io.choerodon.kb.infra.dataobject.iam.OrganizationDTO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class OrganizationConverter implements ConvertorI<OrganizationE, OrganizationDTO, Object> {
//
//    @Override
//    public OrganizationE doToEntity(OrganizationDTO dataObject) {
//        OrganizationE organizationE = new OrganizationE();
//        BeanUtils.copyProperties(dataObject, organizationE);
//        return organizationE;
//    }
//
//}
