//package io.choerodon.kb.domain.kb.convertor;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.WorkSpaceE;
//import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class WorkSpaceConvertor implements ConvertorI<WorkSpaceE, WorkSpaceDO, Object> {
//
//    @Override
//    public WorkSpaceE doToEntity(WorkSpaceDO workSpaceDO) {
//        WorkSpaceE workSpaceE = new WorkSpaceE();
//        BeanUtils.copyProperties(workSpaceDO, workSpaceE);
//        return workSpaceE;
//    }
//
//    @Override
//    public WorkSpaceDO entityToDo(WorkSpaceE entity) {
//        WorkSpaceDO workSpaceDO = new WorkSpaceDO();
//        BeanUtils.copyProperties(entity, workSpaceDO);
//        return workSpaceDO;
//    }
//}
