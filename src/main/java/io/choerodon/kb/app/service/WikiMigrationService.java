package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.MigrationVO;

/**
 * Created by Zenger on 2019/6/14.
 */
public interface WikiMigrationService {

    void controllerMigration();

    void migration();

    void levelMigration(MigrationVO migrationVO, Long resourceId, String type);

    void controllerMigrationFix();
}
