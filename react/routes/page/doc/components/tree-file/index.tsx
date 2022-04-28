import React, { useLayoutEffect } from 'react';
import { message } from 'choerodon-ui';

const onlyofficeApi = 'http://101.132.253.252';

let tryTime = 0;

const Index = () => {
  const initOnlyOfficeApi = () => {
    if (!window.DocsAPI) {
      const script = document.createElement('script');
      script.type = 'text/javascript';
      script.src = `${onlyofficeApi}/web-apps/apps/api/documents/api.js`;
      document.getElementsByTagName('head')[0].appendChild(script);
    }
    initOnlyOfficeService();
  };

  const initOnlyOfficeService = () => {
    if (!window.DocsAPI) {
      if (tryTime < 3) {
        setTimeout(() => {
          tryTime += 1;
          initOnlyOfficeService();
        }, 500);
      } else {
        message.error('onlyOffice加载失败，请重试');
      }
    } else {
      const config = {
        document: {
          fileType: 'docx',
          key: '950ee435b2004ab8b2238c74de5305a0',
          title: 'wxword.docx',
          url: 'https://zkc7n-agile-service.obs.cn-east-3.myhuaweicloud.com:443/671/CHOERODON-HUAWEI/950ee435b2004ab8b2238c74de5305a0@wxword.docx',
          permissions: {
            edit: false,
          },
        },
        documentType: 'word',
        // editorConfig: {
        //   callbackUrl: 'https://example.com/url-to-callback.ashx',
        // },
      };
      const docEditor = new window.DocsAPI.DocEditor('c7ncd-onlyoffice', config);
    }
  };

  useLayoutEffect(() => {
    initOnlyOfficeApi();
  }, []);

  return (
    <div id="c7ncd-onlyoffice" />
  );
};

export default Index;
