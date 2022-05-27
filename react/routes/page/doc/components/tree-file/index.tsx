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
import {
  observer,
} from 'mobx-react-lite';
import { message, Breadcrumb } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import OnlyOffice from '@/components/OnlyOffice';
import DocComment from '@/components/doc-comment';

import './index.less';

// eslint-disable-next-line
// @ts-ignore
const HAS_BASE_PRO = C7NHasModule('@choerodon/base-pro');

const Index = inject('AppState')(observer((props: any) => {
  const {
    store,
    data,
    cRef,
    setFileIsEdit,
    AppState: {
      userInfo,
      currentMenuType: {
        projectId,
        organizationId,
        type,
      },
    },
    AppState,
  } = props;

  const [isEdit, setIsEdit] = useState(false);
  const [breadList, setBreadList] = useState([]);
  const [isOnlyOffice, setIsOnlyOffice] = useState(!HAS_BASE_PRO);
  const [key, setKey] = useState<any>(null);
  const prefix = 'c7ncd-knowledge-file-container';
  const {
    id,
  } = data;

  useEffect(() => {
    setIsEdit(false);
    init();
    getNewKey();
  }, [data]);

  const getNewKey = async () => {
    const res = type === 'project' ? await workSpaceApi.getFileData(id) : await workSpaceApi.getOrgFiledData(id);
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
  const renderList = () => (
    <>
      <div className={`${prefix}-buy-container-list`}>
        <div className={`${prefix}-buy-container-list-icon`} />
        多人实时在线编辑Office文件
      </div>
      <div className={`${prefix}-buy-container-list`}>
        <div className={`${prefix}-buy-container-list-icon`} />
        查看文件的协作记录
      </div>
      <div className={`${prefix}-buy-container-list`}>
        <div className={`${prefix}-buy-container-list-icon`} />
        查看、保存与恢复历史版本
      </div>
    </>
  );
  const renderBuy = () => (
    <div className={`${prefix}-buy-container`}>
      <div className={`${prefix}-buy-container-title1`}>{store.getOrgOrigin === 'sass' ? '您所在租户暂未购买文件的【在线编辑】服务，请联系我们进行增购。' : '您所在组织暂未开通文件的【在线编辑】服务，请联系平台管理员开通。'}</div>
      {store.getOrgOrigin === 'sass' && (
        <div className={`${prefix}-buy-container-content1`}>
          电话：400 800 2077
          <br />
          {' '}
          邮箱：marketing@zknow.com
        </div>
      )}
      {
        store.getOrgOrigin === 'sass' ? (
          <>
            <div className={`${prefix}-buy-container-title2`}>增购后，您的租户将能使用以下功能：</div>
            <div className={`${prefix}-buy-container-content2`}>
              {renderList()}
            </div>
          </>
        ) : (
          <div className={`${prefix}-buy-container-plaformContent`}>
            <div className={`${prefix}-buy-container-platformTitle`}>开通后，您的组织将能使用以下功能：</div>
            {renderList()}
          </div>
        )
      }

    </div>
  );
  const renderBeyond = () => (
    <div className={`${prefix}-beyond-container`}>
      <div className={`${prefix}-beyond-container-title`}>
        当前文件【在线编辑】功能的连接数已达到您组织套餐中的限制：XX
        <br />
        您暂时无法进行文件的编辑操作。若想继续编辑该文件，可执行如下操作：
      </div>
      <div className={`${prefix}-beyond-container-content`}>
        <div className={`${prefix}-beyond-container-list`}>
          <div className={`${prefix}-beyond-container-list-icon`} />
          {store.getOrgOrigin === 'sass' ? '联系我们升级【在线编辑】套餐；' : '联系平台管理员升级【在线编辑】功能的连接数；'}
        </div>
        {store.getOrgOrigin === 'sass' && (
        <div className={`${prefix}-beyond-container-content1`}>
          电话：400 800 2077
          <br />
          邮箱：marketing@zknow.com
        </div>
        )}
        <div className={`${prefix}-beyond-container-list`}>
          <div className={`${prefix}-beyond-container-list-icon`} />
          退出目前处于编辑状态的其他文件页面。
        </div>
      </div>
    </div>
  );
  const renderBuyTitle = () => {
    if (store.getOrgOrigin === 'sass') {
      return '购买在线编辑服务';
    }
    return '开通在线编辑服务';
  };
  const resultList = { 1: { content: renderBuy() }, 2: { content: renderBeyond() } };
  const handleOk = () => {
    store.loadWorkSpaceAll();
    goView();
  };
  const handleDelete = (value:any) => {
    console.log(store.getOrgOrigin);
    Modal.open({
      title: value.result === 1 ? renderBuyTitle() : '超出连接数限制',
      children: resultList[value.result].content,
      okText: '我知道了',
      autoCenter: true,
      cancelButton: false,
      onOk: handleOk,
    });
  };

  useEffect(() => {
    setFileIsEdit(isEdit);
  }, [isEdit]);

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
          isEdit={isEdit}
          axios={axios}
          fileKey={key?.fileKey}
          tenantId={organizationId}
          sourceId={key?.id}
          handlerEditResult={(value:any) => { handleDelete(value); }}
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
}));

export default Index;
