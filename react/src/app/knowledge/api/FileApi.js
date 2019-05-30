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
  const { type, id } = AppState.currentMenuType;
  const apiGetway = `/knowledge/v1/${type}s/${id}`;
  return axios.post(
    `${apiGetway}/page_attachment/upload_for_address`,
    data,
    axiosConfig,
  );
}
