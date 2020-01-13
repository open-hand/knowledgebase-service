import { getProjectId, request, getOrganizationId } from '../common/utils';

// 获取项目知识库
export const getProjectBaseList = () => request.get(`/knowledge/v1/projects/${getProjectId()}/knowledge_base/query/list`);

// 获取组织知识库
export const getOrgBaseList = () => request.get(`/knowledge/v1/organizations/${getOrganizationId()}/knowledge_base/query/list`);

/**
 * 项目层创建知识库
 */
export const createBase = (data) => request.post(`/knowledge/v1/projects/${getProjectId()}/knowledge_base/create`, data);

/**
 * 组织层创建知识库
 * @param {*} data
 */
export const createOrgBase = (data) => request.post(`/knowledge/v1/organizations/${getOrganizationId()}/knowledge_base/create`, data);

/**
 * 组织层设置知识库
 * @param {*} data
 */
export const editBase = (data) => request.put(`/knowledge/v1/projects/${getProjectId()}/knowledge_base/update`, data);

/**
 * 组织层设置知识库
 * @param {*} data
 */
export const editOrgBase = (data) => request.put(`/knowledge/v1/organizations/${getOrganizationId()}/knowledge_base/update`, data);

// 将项目下的知识库或文档移动到回收站
export const moveToBin = (id) => request.put(`/knowledge/v1/projects/${getProjectId()}/knowledge_base/remove_my/${id}`);

// 将组织下的知识库或文档移动到回收站
export const orgMoveToBin = (id) => request.put(`/knowledge/v1/organizations/${getOrganizationId()}/knowledge_base/remove_my/${id}`);

// 判断项目层文档所属知识库是否存在
export const judgeBelongBaseIsExist = (docId) => request.get(`/knowledge/v1/projects/${getProjectId()}/work_space/belong_base_exist/${docId}`);
// 判断组织层文档所属知识库是否存在
export const judgeOrgBelongBaseIsExist = (docId) => request.get(`/knowledge/v1/organizations/${getOrganizationId()}/work_space/belong_base_exist/${docId}`);

// 恢复回收站中的知识库或文档
export const recoverFromBin = (id, type, baseId) => request.put(`/knowledge/v1/projects/${getProjectId()}/recycle/restore/${id}?type=${type}&baseId=${baseId || ''}`);

// 恢复组织层回收站中的知识库或文档
export const recoverOrgFromBin = (id, type, baseId) => request.put(`/knowledge/v1/organizations/${getOrganizationId()}/recycle/restore/${id}?type=${type}&baseId=${baseId || ''}`);

// 删除回收站的知识库或文档
export const deleteDocOrBase = (id, type) => request.delete(`/knowledge/v1/projects/${getProjectId()}/recycle/delete/${id}?type=${type}`);

// 删除组织层回收站的知识库或文档
export const deleteOrgDocOrBase = (id, type) => request.delete(`/knowledge/v1/organizations/${getOrganizationId()}/recycle/delete/${id}?type=${type}`);

export const getOrgBinList = (data) => request.post(`/knowledge/v1/organizations/${getOrganizationId()}/recycle/page_by_options`, data);

// 获取文档内容
export const getPageInfo = (workSpaceId) => request.get(`/knowledge/v1/projects/${getProjectId()}/work_space/${workSpaceId}`);

// 获取组织层文档内容
export const getOrgPageInfo = (workSpaceId) => request.get(`/knowledge/v1/organizations/${getOrganizationId()}/work_space/${workSpaceId}`);
