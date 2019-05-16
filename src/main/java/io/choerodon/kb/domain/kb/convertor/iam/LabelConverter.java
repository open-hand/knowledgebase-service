//package io.choerodon.kb.domain.kb.convertor.iam;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.iam.LabelE;
//import io.choerodon.kb.infra.dataobject.iam.LabelDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class LabelConverter implements ConvertorI<LabelE, LabelDO, Object> {
//
//    @Override
//    public LabelE doToEntity(LabelDO dataObject) {
//        LabelE labelE = new LabelE();
//        BeanUtils.copyProperties(dataObject, labelE);
//        return labelE;
//    }
//
//    @Override
//    public LabelDO entityToDo(LabelE entity) {
//        LabelDO labelDO = new LabelDO();
//        BeanUtils.copyProperties(entity, labelDO);
//        return labelDO;
//    }
//}
