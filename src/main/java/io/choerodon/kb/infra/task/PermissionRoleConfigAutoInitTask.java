package io.choerodon.kb.infra.task;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.app.service.PermissionRoleConfigService;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 知识库权限矩阵自动初始化任务
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
@Component
public class PermissionRoleConfigAutoInitTask /*测试专用implements CommandLineRunner*/ {

    @Autowired
    private PermissionRoleConfigService permissionRoleConfigService;
    private final ObjectReader objectReader;

    private static final String FILE_NAME = "platform-permission-role-config.csv";
    private static final String FILE_PATH = "/init-data/" + FILE_NAME;

    public PermissionRoleConfigAutoInitTask() {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        this.objectReader = new CsvMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readerFor(PermissionRoleConfig.class)
                .with(schema);
        ;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionRoleConfigAutoInitTask.class);

    @JobTask(maxRetryCount = 3,
            code = "permissionRoleConfigAutoInit",
            description = "知识库权限矩阵自动初始化")
    @TimedTask(name = "permissionRoleConfigAutoInit",
            description = "知识库权限矩阵自动初始化",
            oneExecution = true,
            repeatCount = 0,
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void doInit(Map<String, Object> param) {
        LOGGER.info("======================开始执行 知识库权限矩阵自动初始化=====================");
        List<PermissionRoleConfig> initDataList = this.loadInitData();
        LOGGER.info("======================知识库权限矩阵自动初始化 共加载到{}条数据=====================", initDataList.size());
        this.permissionRoleConfigService.batchCreateOrUpdate(PermissionConstants.EMPTY_ID_PLACEHOLDER, PermissionConstants.EMPTY_ID_PLACEHOLDER, initDataList);
        LOGGER.info("======================执行完成 知识库权限矩阵自动初始化====================");
    }

    /**
     * @return 从resource文件中加载初始化数据
     */
    private List<PermissionRoleConfig> loadInitData() {
        try {
            final MappingIterator<PermissionRoleConfig> objectMappingIterator = this.objectReader.readValues(PermissionRoleConfigAutoInitTask.class.getResourceAsStream(FILE_PATH));
            return objectMappingIterator.readAll();
        } catch (IOException ex) {
            throw new CommonException(ex.getMessage(), ex);
        }
    }
//    测试专用
//    @Override
//    public void run(String... args) throws Exception {
//        this.doInit(null);
//    }
}
