package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.MigrationDTO;

/**
 * Created by Zenger on 2019/6/14.
 */
public interface WikiMigrationService {

    void controllerMigration();

    void migration();

    void levelMigration(MigrationDTO migrationDTO, Long resourceId, String type);

    void controllerMigrationFix();
}
