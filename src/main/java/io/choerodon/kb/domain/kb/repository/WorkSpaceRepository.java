package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageDetailDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository {

    WorkSpaceDTO baseCreate(WorkSpaceDTO workSpaceDTO);

    WorkSpaceDTO baseUpdate(WorkSpaceDTO workSpaceDTO);

    WorkSpaceDTO selectById(Long id);

    WorkSpaceDTO baseQueryById(Long organizationId, Long projectId, Long workSpaceId);

    void checkById(Long organizationId, Long projectId, Long workSpaceId);

    PageDetailDTO queryDetail(Long id);

    PageDetailDTO queryReferenceDetail(Long id);

    List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId);
}
