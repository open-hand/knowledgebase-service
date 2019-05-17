//package io.choerodon.kb.domain.kb.convertor.iam;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.iam.OrganizationE;
//import io.choerodon.kb.domain.kb.entity.iam.UserE;
//import io.choerodon.kb.infra.dataobject.iam.UserDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class UserConverter implements ConvertorI<UserE, UserDO, Object> {
//
//    @Override
//    public UserE doToEntity(UserDO dataObject) {
//        UserE userE = new UserE();
//        BeanUtils.copyProperties(dataObject, userE);
//        userE.setOrganization(new OrganizationE(dataObject.getOrganizationId()));
//        return userE;
//    }
//
//}
