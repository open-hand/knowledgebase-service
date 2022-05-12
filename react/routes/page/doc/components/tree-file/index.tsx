import React, {
  useLayoutEffect, useImperativeHandle, useState, useMemo, useEffect, useCallback,
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
import OnlyOffice from '@/components/OnlyOffice';
import DocComment from '@/components/doc-comment';

import './index.less';

const Index = inject('AppState')((props: any) => {
  const {
    store,
    data,
    cRef,
    AppState: {
      userInfo,
      currentMenuType: {
        projectId,
        organizationId,
      },
    },
    AppState,
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

  useEffect(() => {
    init();
  }, [data]);

  const goView = () => {
    setIsEdit(false);
  };

  const goEdit = () => {
    setIsEdit(true);
  };

  useImperativeHandle((cRef), () => ({
    goEdit,
    goView,
    getIsEdit: () => isEdit,
    initEdit: () => init(),
    initView: () => init(),
    getIsOnlyOffice: () => isOnlyOffice,
    changeMode: () => {
      setIsOnlyOffice(!isOnlyOffice);
      setTimeout(() => {
        store.setSelectItem(JSON.parse(JSON.stringify(store.getSelectItem)));
      }, 500);
    },
  }));

  const spaceData = useMemo(() => {
    const code = store.getSpaceCode;
    return store.getWorkSpace?.[code].data;
  }, [store.getSpaceCode, store.getWorkSpace]);

  const getBreads = () => {
    const result = data?.route?.split('.').map((i: any) => spaceData?.items?.[i]);
    setBreadList(result);
  };

  const init = () => {
    store.loadDoc(data?.id);
    getBreads();
  };

  const renderOffice = useCallback(() => {
    console.log(userInfo);
    if (isOnlyOffice) {
      return (
        <OnlyOffice
          style={{
            marginTop: 10,
          }}
          fileType={fileType}
          onlyOfficeKey={key}
          title={title}
          url={url}
          isEdit={isEdit}
          organizationId={organizationId}
          projectId={organizationId}
          id={id}
          userInfo={userInfo}
        />
      );
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
  }, [
    key,
    data,
    fileKey,
    fileType,
    id,
    isEdit,
    isOnlyOffice,
    organizationId,
    title,
    url,
    userInfo,
  ]);

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
