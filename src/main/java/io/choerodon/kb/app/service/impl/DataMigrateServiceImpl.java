package io.choerodon.kb.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.kb.app.service.DataMigrateService;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

/**
 * @author: 25499
 * @date: 2020/1/6 18:05
 * @description:
 */
public class DataMigrateServiceImpl implements DataMigrateService {
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private BaseFeignClient baseFeignClient;
    @Override
    public void migrateWorkSpace() {

        //1.修复workspace
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllOrganization();
        //组织
       
        //项目
    }
}
