/* eslint-disable no-unused-vars */
import { stores, axios } from '@choerodon/boot';

const { AppState } = stores;
const ImgUploadTimeout = 60000;

/**
 * 上传图片
 * @param {any} data
 */
export default function uploadImage(data) {
  const axiosConfig = {
    headers: { 'content-type': 'multipart/form-data' },
    timeout: ImgUploadTimeout,
  };
  const { type, id } = AppState.currentMenuType;
  const apiGateway = `/knowledge/v1/${type}s/${id}`;
  return axios.post(
    `${apiGateway}/page_attachment/upload_for_address`,
    data,
    axiosConfig,
  );
}

/**
 * 将以base64的图片url数据转换为Blob
 * @param {string} urlData 用url方式表示的base64图片数据
 */
export function convertBase64UrlToBlob(urlData) {
  const bytes = window.atob(urlData.split(',')[1]); // 去掉url的头，并转换为byte

  // 处理异常,将ascii码小于0的转换为大于0
  const buffer = new ArrayBuffer(bytes.length);
  const unit8Array = new Uint8Array(buffer);
  for (let i = 0; i < bytes.length; i += 1) {
    unit8Array[i] = bytes.charCodeAt(i);
  }

  return new Blob([buffer], { type: 'image/png' });
}

export function escape(str) {
  return str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');
}
export function getParams(url) {
  const theRequest = {};
  if (url.indexOf('?') !== -1) {
    const str = url.split('?')[1];
    const strs = str.split('&');
    for (let i = 0; i < strs.length; i += 1) {
      theRequest[strs[i].split('=')[0]] = decodeURI(strs[i].split('=')[1]);
    }
  }
  return theRequest;
}

/**
 * 生成指定长度的随机字符串
 * @param len 字符串长度
 * @returns {string}
 */
export function randomString(len = 32) {
  let code = '';
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const maxPos = chars.length;
  for (let i = 0; i < len; i += 1) {
    code += chars.charAt(Math.floor(Math.random() * (maxPos + 1)));
  }
  return code;
}

/**
 * randomWord 产生任意长度随机字母数字组合
 * @param randomFlag 是否任意长度 min-任意长度最小位[固定位数] max-任意长度最大位
 * @param min
 * @param max
 * @returns {string}
 */
export function randomWord(randomFlag, min, max) {
  let str = '';
  let range = min;
  const arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

  // 随机产生
  if (randomFlag) {
    range = Math.round(Math.random() * (max - min)) + min;
  }
  for (let i = 0; i < range; i += 1) {
    const pos = Math.round(Math.random() * (arr.length - 1));
    str += arr[pos];
  }
  return str;
}

// 获取文件名后缀
export function getFileSuffix(fileName) {
  return fileName.replace(/.+\./, '').toLowerCase();
}

// 转换url的param
export function paramConverter(url) {
  const reg = /[^?&]([^=&#]+)=([^&#]*)/g;
  const retObj = {};
  url.match(reg).forEach((item) => {
    const [tempKey, paramValue] = item.split('=');
    const paramKey = tempKey[0] !== '&' ? tempKey : tempKey.substring(1);
    Object.assign(retObj, {
      [paramKey]: paramValue,
    });
  });
  return retObj;
}
