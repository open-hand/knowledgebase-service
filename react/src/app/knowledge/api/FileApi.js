import { stores, axios } from '@choerodon/boot';

const { AppState } = stores;

/**
 * 上传图片
 * @param {any} data
 */
export default function uploadImage(data) {
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
