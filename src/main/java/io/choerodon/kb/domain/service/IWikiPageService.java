package io.choerodon.kb.domain.service;

import io.choerodon.kb.infra.dataobject.MigrationDO;

/**
 * Created by Zenger on 2019/6/3.
 */
public interface IWikiPageService {

    String getWikiPageMigration(MigrationDO migrationDO);

    String getWikiPageAttachment(String data);
}
