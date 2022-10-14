package io.choerodon.kb.domain.service;

import org.apache.commons.lang3.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
public interface IWorkSpaceService {


    /**
     * 文件夹最大长度
     */
    int LENGTH_LIMIT = 40;

    WorkSpaceType handleSpaceType();

    void renameWorkSpace(WorkSpaceDTO workSpaceDTO, String newName);

    default void checkFolderNameLength(String title) {
        if (StringUtils.isBlank(title) && title.length() > LENGTH_LIMIT) {
            throw new CommonException("error.folder.name.length.limit.exceeded", LENGTH_LIMIT);
        }
    }

}
