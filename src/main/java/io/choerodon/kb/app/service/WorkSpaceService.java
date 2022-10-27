package io.choerodon.kb.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.boot.file.dto.FileSimpleDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceService {

    WorkSpaceInfoVO createWorkSpaceAndPage(Long organizationId, Long projectId, PageCreateWithoutContentVO create, boolean initFlag);

    WorkSpaceInfoVO updateWorkSpaceAndPage(Long organizationId, Long projectId, Long id, String searchStr, PageUpdateVO pageUpdateVO, boolean checkPermission);

    void moveToRecycle(Long organizationId, Long projectId, Long workspaceId, Boolean isAdmin, boolean checkPermission);

    void deleteWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId);

    void restoreWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId, Long baseId);

    void moveWorkSpace(Long organizationId, Long projectId, Long id, MoveWorkSpaceVO moveWorkSpaceVO);

    /**
     * 将知识库下面的所有文件放入回收站
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param baseId
     */
    void removeWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId);

    /**
     * 将知识库下面的所有文件彻底删除
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param baseId
     */
    void deleteWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId);

    /**
     * 将回收站中知识库下面的所有文件恢复到项目下
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param baseId
     */
    void restoreWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId);

    /**
     * 复制页面
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param workSpaceId
     * @return
     */
    WorkSpaceInfoVO clonePage(Long organizationId, Long projectId, Long workSpaceId, Long parentId);

    /**
     * 查询项目最近更新的空间列表
     *
     * @param organizationId    组织ID 组织id
     * @param selfFlag       是否查询个人文档
     * @return 空间列表list
     */
    Page<WorkBenchRecentVO> selectProjectRecentList(PageRequest pageRequest, Long organizationId, Long projectId, boolean selfFlag);

    WorkSpaceInfoVO upload(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateWithoutContentVO);

    /**
     * 基于Multipart上传文件,返回key
     *
     * @param organizationId    组织ID
     * @param directory
     * @param fileName
     * @param docType
     * @param storageCode
     * @param multipartFile
     * @return
     */
    FileSimpleDTO uploadMultipartFileWithMD5(Long organizationId, String directory, String fileName, Integer docType, String storageCode, MultipartFile multipartFile);

    void renameWorkSpace(Long projectId, Long organizationId, Long id, String newName);

}
