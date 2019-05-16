//package io.choerodon.kb.domain.kb.convertor.iam;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.convertor.ConvertorI;
//import io.choerodon.kb.domain.kb.entity.iam.ProjectE;
//import io.choerodon.kb.infra.dataobject.iam.ProjectDO;
//
///**
// * Created by Zenger on 2019/4/30.
// */
//@Component
//public class ProjectConverter implements ConvertorI<ProjectE, ProjectDO, Object> {
//
//
//    @Override
//    public ProjectE doToEntity(ProjectDO dataObject) {
//        ProjectE projectE = new ProjectE();
//        BeanUtils.copyProperties(dataObject, projectE);
//        projectE.initOrganizationE(dataObject.getOrganizationId());
//        return projectE;
//    }
//}