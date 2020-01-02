import axios from 'axios';
import { stores } from '@choerodon/boot';

const { AppState } = stores;

export const getProjectId = () => AppState.currentMenuType.id;
export const getProjectName = () => AppState.currentMenuType.name;
export const getOrganizationId = () => AppState.currentMenuType.organizationId;

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

class Request {
  constructor() {
    // 请求队列，相同请求时，取消前一个请求
    // this.requestQueue = [];
    ['get', 'post', 'options', 'delete', 'put'].forEach((type) => {
      this[type] = (...args) => new Promise((resolve, reject) => {
        // const CancelToken = axios.CancelToken;
        // const source = CancelToken.source();       
        let url = args[0];
  
        // const preSameRequest = _.find(this.requestQueue, { url, type });       
        // if (preSameRequest) {
        //   this.requestQueue.splice(_.findIndex(this.requestQueue, { url, type }), 1);
        //   preSameRequest.cancel(`Request canceled ${url} ${type}`);
        // }
        // const requestObject = {
        //   url,
        //   type,
        //   cancel: source.cancel,
        // };       
        // this.requestQueue.push(requestObject);       
        if (Object.keys(getParams(url)).length > 0) {
          url += `&organizationId=${getOrganizationId()}`;
        } else {
          url += `?organizationId=${getOrganizationId()}`;
        }
        // eslint-disable-next-line no-param-reassign
        args[0] = url;
        // const cancelToken = source.token;        
        // args.push({
        //   cancelToken,
        // });
        axios[type](...args).then((data) => {
          // if (data && data.failed) {
          //   // Choerodon.prompt(data.message);
          //   resolve(data);
          // } else {
          resolve(data);
          // }
        }).catch((error) => {
          // if (axios.isCancel(error)) {
          //   console.log('Rquest canceled', error.message); // 请求如果被取消，这里是返回取消的message
          // } else {
          // Choerodon.prompt(error.message);
          reject(error);
          // }
        }).finally(() => {
          // this.requestQueue.splice(_.findIndex(this.requestQueue, { 
          //   url,
          //   type,  
          // }), 1);
        });
      });
    });
  }
}
export const request = new Request();
