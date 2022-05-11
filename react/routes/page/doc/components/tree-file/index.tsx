import React, {
  useLayoutEffect, useImperativeHandle, useState, useMemo, useEffect,
} from 'react';
import { inject } from 'mobx-react';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import {
  axios,
  Choerodon,
} from '@choerodon/master';
import TimeAgo from 'timeago-react';
import {
  Wps,
} from '@choerodon/components';
import { message, Breadcrumb } from 'choerodon-ui';
import DocComment from '@/components/doc-comment';

import './index.less';

const onlyofficeApi = 'http://onlyoffice.c7n.devops.hand-china.com';

let tryTime = 0;

const Index = inject('AppState')((props: any) => {
  const {
    store,
    data,
    cRef,
    AppState: {
      currentMenuType: {
        projectId,
        organizationId,
      },
    },
  } = props;

  const [isEdit, setIsEdit] = useState(false);
  const [breadList, setBreadList] = useState([]);
  const [isOnlyOffice, setIsOnlyOffice] = useState(true);

  const {
    fileType,
    key,
    title,
    url,
    fileKey,
    id,
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
        // eslint-disable-next-line no-underscore-dangle
        callbackUrl: `${window._env_.API_HOST}/knowledge/v1/choerodon/only_office/save/file?${organizationId ? `organization_id=${organizationId}&` : ''}${projectId ? `project_id=${projectId}&` : ''}${title ? `title=${title}&` : ''}${id ? `business_id=${id}` : ''}`,
      },
    };
    const docEditor = new window.DocsAPI.DocEditor('c7ncd-onlyoffice', config);
  };

  const goView = () => {
    setIsEdit(false);
    if (isOnlyOffice) {
      refreshNode();
      initOnlyOfficeApi();
    }
  };

  const goEdit = () => {
    setIsEdit(true);
    refreshNode();
    initEditOnlyOffice();
  };

  useImperativeHandle((cRef), () => ({
    goEdit,
    goView,
    getIsEdit: () => isEdit,
    initEdit,
    initView,
    getIsOnlyOffice: () => isOnlyOffice,
    changeMode: () => {
      setIsOnlyOffice(!isOnlyOffice);
    },
  }));

  useEffect(() => {
    if (isOnlyOffice) {
      initView();
    }
  }, [isOnlyOffice]);

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

  const spaceData = useMemo(() => {
    const code = store.getSpaceCode;
    return store.getWorkSpace?.[code].data;
  }, [store.getSpaceCode, store.getWorkSpace]);

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

  const getBreads = () => {
    const result = data?.route?.split('.').map((i: any) => spaceData?.items?.[i]);
    setBreadList(result);
    console.log(result);
  };

  const initView = () => {
    goView();
    store.loadDoc(data?.id);
    getBreads();
  };

  const initEdit = () => {
    goEdit();
    store.loadDoc(data?.id);
    getBreads();
  };

  const renderOffice = () => {
    if (isOnlyOffice) {
      return <div className="c7ncd-knowledge-file" />;
    }
    return (
      <Wps
        style={{
          width: '100%',
          height: '100%',
        }}
        axios={axios}
        fileKey={fileKey}
        tenantId={organizationId}
        sourceId={data?.id}
      />
    );
  };

  const handleClickBread = (d: any) => {
    const newTree = mutateTree(spaceData, d?.id, {
      isClick: true,
    });
    const newTree2 = mutateTree(newTree, data?.id, {
      isClick: false,
    });
    store.setWorkSpaceByCode(store.getSpaceCode, newTree2);
    store.setSelectItem(d);
  };

  return (
    <div className="c7ncd-knowledge-file-container">
      <Breadcrumb>
        {
          breadList?.map((bread: any, index: any) => (
            <Breadcrumb.Item
              {
                ...(index !== breadList.length - 1 ? {
                  onClick: () => handleClickBread(bread),
                } : {})
              }
            >
              { bread?.data?.title }

            </Breadcrumb.Item>
          ))
        }
      </Breadcrumb>
      {
        renderOffice()
      }
      <div className="c7ncd-knowledge-file-container-creator">
        <p>
          <span>创建者</span>
          <span style={{ margin: '0 3px' }}>{ data?.createdUser?.realName }</span>
          (
          <TimeAgo
            datetime={data?.creationDate}
            locale={Choerodon.getMessage('zh_CN', 'en')}
          />
          )
        </p>
        <p>
          <span>最近编辑</span>
          <span style={{ margin: '0 3px' }}>{ data?.lastUpdatedUser?.realName || '无' }</span>
          (
          <TimeAgo
            datetime={data?.lastUpdateDate}
            locale={Choerodon.getMessage('zh_CN', 'en')}
          />
          )
        </p>
      </div>
      <DocComment
        data={store.getDoc}
        store={store}
      />
    </div>
  );
});

export default Index;
