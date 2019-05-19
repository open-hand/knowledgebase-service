import { stores, axios } from '@choerodon/boot';

const { AppState } = stores;

/**
 * 删除附件
 * @param id
 * @returns {boolean|void|*}
 */
export function deleteFile(id) {
  const orgId = AppState.currentMenuType.organizationId;
  return axios.delete(`/knowledge/v1/organizations/${orgId}/page_attachment/${id}`);
}

/**
 * 上传图片
 * @param {any} data
 */
export function uploadImage(data) {
  const axiosConfig = {
    headers: { 'content-type': 'multipart/form-data' },
  };
  const orgId = AppState.currentMenuType.organizationId;
  return axios.post(
    `/knowledge/v1/organizations/${orgId}/page_attachment/upload_for_address`,
    data,
    axiosConfig,
  );
}
