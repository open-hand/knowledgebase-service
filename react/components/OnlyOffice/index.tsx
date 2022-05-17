import React, { useLayoutEffect } from 'react';
import { message, Breadcrumb } from 'choerodon-ui';
import Cookies from 'universal-cookie';

import './index.less';

const cookies = new Cookies();

const getAccessToken = () => cookies.get('access_token');

// eslint-disable-next-line no-underscore-dangle
const { onlyofficeApi } = window._env_;

let tryTime = 0;

const normalConfig = ({
  fileType,
  onlyOfficeKey,
  title,
  url,
  organizationId,
  projectId,
  id,
  userInfo,
  isEdit,
}: any): any => ({
  lang: 'zh-CN',
  document: {
    fileType,
    key: onlyOfficeKey,
    title,
    url,
    permissions: {
      edit: !!isEdit,
      download: false,
      print: false,
    },
    print: false,
  },
  // documentType: 'word',
  editorConfig: {
    mode: isEdit ? 'edit' : 'view',
    lang: 'zh-CN',
    // eslint-disable-next-line no-underscore-dangle
    ...isEdit ? {
      callbackUrl: `${window._env_.API_HOST}/knowledge/v1/choerodon/only_office/save/file?${organizationId ? `organization_id=${organizationId}&` : ''}${projectId ? `project_id=${projectId}&` : ''}${title ? `title=${title}&` : ''}${id ? `business_id=${id}&` : ''}${userInfo?.id ? `user_id=${userInfo?.id}&` : ''}token=${getAccessToken()}`,
    } : {},
    user: {
      name: userInfo?.realName || '',
      id: userInfo?.id || '',
    },
    customization: {
      loaderName: 'Choerodon',
      chat: false,
      help: false,
      forcesave: true,
      comments: false,
      feedback: false,
      plugins: false,
      macros: false,
      uiTheme: 'default-light',
      spellcheck: false,
      logo: null,
      compactHeader: true,
      compactToolbar: true,
      customer: {
        name: '',
        address: '',
        mail: '',
        www: '',
        info: '',
        logo: '',
      },
    },
  },
});

const Index = (props: any) => {
  const {
    style,
    fileType,
    onlyOfficeKey,
    title,
    url,
    organizationId,
    projectId,
    id,
    userInfo,
    isEdit = false,
  } = props;

  useLayoutEffect(() => {
    if (!isEdit) {
      initView();
    } else {
      initEdit();
    }
  }, [isEdit, id]);

  const initEditOnlyOffice = () => {
    const config = normalConfig({
      fileType,
      onlyOfficeKey,
      title,
      url,
      organizationId,
      projectId,
      id,
      userInfo,
      isEdit: true,
    });
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
        }, 1000);
      } else {
        message.error('onlyOffice加载失败，请重试');
      }
    } else {
      const config = normalConfig({
        fileType,
        onlyOfficeKey,
        title,
        url,
        organizationId,
        projectId,
        id,
        userInfo,
        isEdit: false,
      });
      if (!userInfo || !userInfo?.realName) {
        delete config.editorConfig.user;
      }
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
