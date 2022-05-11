import React, { useLayoutEffect } from 'react';
import { message, Breadcrumb } from 'choerodon-ui';

import './index.less';

// eslint-disable-next-line no-underscore-dangle
const onlyofficeApi = window._env_.onlyofficeApi || 'http://onlyoffice.c7n.devops.hand-china.com';

let tryTime = 0;

const Index = ({
  style,
  fileType,
  key,
  title,
  url,
  organizationId,
  projectId,
  id,
  isEdit = false,
}: any) => {
  useLayoutEffect(() => {
    if (!isEdit) {
      initView();
    } else {
      initEdit();
    }
  }, [isEdit]);

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
        // eslint-disable-next-line no-underscore-dangle
        callbackUrl: `${window._env_.API_HOST}/knowledge/v1/choerodon/only_office/save/file?${organizationId ? `organization_id=${organizationId}&` : ''}${projectId ? `project_id=${projectId}&` : ''}${title ? `title=${title}&` : ''}${id ? `business_id=${id}` : ''}`,
      },
    };
    const docEditor = new window.DocsAPI.DocEditor('c7ncd-onlyoffice', config);
  };

  const initEdit = () => {
    goEdit();
  };

  const goEdit = () => {
    refreshNode();
    initEditOnlyOffice();
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

  const initOnlyOfficeApi = () => {
    if (!window.DocsAPI) {
      const script = document.createElement('script');
      script.type = 'text/javascript';
      script.src = `${onlyofficeApi}/web-apps/apps/api/documents/api.js`;
      document.getElementsByTagName('head')[0].appendChild(script);
    }
    initOnlyOfficeService();
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

  const goView = () => {
    refreshNode();
    initOnlyOfficeApi();
  };

  const initView = () => {
    goView();
  };

  return (
    <div
      className="c7ncd-knowledge-file"
      style={style || {}}
    />
  );
};

export default Index;
