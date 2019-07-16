package io.choerodon.kb.domain.service;

import io.choerodon.kb.infra.dto.MigrationDTO;

/**
 * Created by Zenger on 2019/6/3.
 */
public interface IWikiPageService {

    String getWikiPageMigration(MigrationDTO migrationDTO);

    String getWikiPageAttachment(String data);
}
