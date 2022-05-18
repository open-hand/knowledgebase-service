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
  workSpaceApi,
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
  const [key, setKey] = useState<any>(null);

  const {
    id,
  } = data;

  useEffect(() => {
    setIsEdit(false);
    init();
    getNewKey();
  }, [data]);

  const getNewKey = async () => {
    const res = await workSpaceApi.getFileData(id);
    setKey(res);
  };

  const goView = async () => {
    await getNewKey();
    setIsEdit(false);
  };

  const goEdit = async () => {
    await getNewKey();
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
    if (key) {
      if (isOnlyOffice) {
        return (
          <OnlyOffice
            style={{
              marginTop: 16,
            }}
            fileType={key?.fileType}
            onlyOfficeKey={key?.key}
            title={key?.title}
            url={key?.url}
            isEdit={isEdit}
            organizationId={organizationId}
            projectId={organizationId}
            id={key?.id}
            userInfo={userInfo}
          />
        );
      }
      return (
        <Wps
          style={{
            width: '100%',
            height: '100%',
            marginTop: 16,
          }}
          axios={axios}
          fileKey={key?.fileKey}
          tenantId={organizationId}
          sourceId={key?.id}
        />
      );
    }
    return '';
  }, [
    key,
    isEdit,
    isOnlyOffice,
    organizationId,
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
      {/* @ts-ignore */}
      <Breadcrumb
        separator={'>' as any}
      >
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
