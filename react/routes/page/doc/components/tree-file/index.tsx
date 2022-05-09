import React, { useLayoutEffect, useImperativeHandle } from 'react';
import { inject } from 'mobx-react';
import { message } from 'choerodon-ui';

import './index.less';

const onlyofficeApi = 'http://onlyoffice.c7n.devops.hand-china.com';

let tryTime = 0;

const Index = inject('AppState')((props: any) => {
  const {
    data,
    cRef,
    AppState: {
      currentMenuType: {
        projectId,
        organizationId,
      },
    },
  } = props;

  const {
    fileType,
    key,
    title,
    url,
  } = data;

  const initEditOnlyOffice = () => {
    const config = {
      lang: 'zh-CN',
      document: {
        fileType,
        key,
        title,
        url,
        permissions: {
          edit: true,
        },
      },
      // documentType: 'word',
      editorConfig: {
        mode: 'edit',
        lang: 'zh-CN',
        callbackUrl: `${onlyofficeApi}/knowledge/v1/choerodon/only_office/save/file?${organizationId ? `organization_id=${organizationId}&` : ''}${projectId ? `project_id=${projectId}` : ''}`,
      },
    };
    const docEditor = new window.DocsAPI.DocEditor('c7ncd-onlyoffice', config);
  };

  useImperativeHandle((cRef), () => ({
    goEdit: () => {
      refreshNode();
      initEditOnlyOffice();
    },
  }));

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
      message.success('onlyOffice加载成功');
      const config = {
        lang: 'zh-CN',
        document: {
          fileType,
          key,
          title,
          url,
          permissions: {
            edit: false,
          },
        },
        // documentType: 'word',
        editorConfig: {
          mode: 'view',
          lang: 'zh-CN',
          // callbackUrl: 'https://example.com/url-to-callback.ashx',
        },
      };
      const docEditor = new window.DocsAPI.DocEditor('c7ncd-onlyoffice', config);
    }
  };

  const refreshNode = () => {
    const parent = document.querySelector('.c7ncd-knowledge-file');
    // const target = document.querySelector('#c7ncd-onlyoffice');
    const createNode = () => {
      const div = document.createElement('div');
      div.id = 'c7ncd-onlyoffice';
      parent?.appendChild(div);
    };
    if (parent?.innerHTML) {
      parent.innerHTML = '';
    }
    createNode();
  };

  useLayoutEffect(() => {
    refreshNode();
    initOnlyOfficeApi();
  }, [data]);

  return (
    <div className="c7ncd-knowledge-file">
      {/* <div
        id="c7ncd-onlyoffice"
      /> */}
    </div>
  );
});

export default Index;
