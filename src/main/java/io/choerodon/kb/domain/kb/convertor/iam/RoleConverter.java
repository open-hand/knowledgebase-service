//package io.choerodon.kb.domain.kb.convertor.iam;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertHelper;
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.iam.LabelE;
//import io.choerodon.kb.domain.kb.entity.iam.RoleE;
//import io.choerodon.kb.infra.dataobject.iam.RoleDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class RoleConverter implements ConvertorI<RoleE, RoleDO, Object> {
//
//    @Override
//    public RoleE doToEntity(RoleDO dataObject) {
//        RoleE roleE = new RoleE();
//        BeanUtils.copyProperties(dataObject, roleE);
//        roleE.setLabels(ConvertHelper.convertList(dataObject.getLabels(), LabelE.class));
//        return roleE;
//    }
//
//    @Override
//    public RoleDO entityToDo(RoleE entity) {
//        RoleDO roleDO = new RoleDO();
//        BeanUtils.copyProperties(entity, roleDO);
//        return roleDO;
//    }
//}
