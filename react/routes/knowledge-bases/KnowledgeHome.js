import React, {
  useContext, useState, useEffect, Fragment,
} from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page, Header, Content, Breadcrumb, stores,
} from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { HeaderButtons } from '@choerodon/master';
import useFormatMessage from '@/hooks/useFormatMessage';
import BaseItem from './components/baseItem';
import { openCreateBaseModal } from './components/baseModal';
import BinTable from './components/binTable';

import Store from './stores';
import './KnowledgeHome.less';

const { AppState } = stores;

const KnowledgeBases = observer(() => {
  const {
    prefixCls, knowledgeHomeStore, type, binTableDataSet,
  } = useContext(Store);
  const { projectBaseList, orgBaseList } = knowledgeHomeStore;
  const [projectExpand, setProjectExpand] = useState(true);
  const [organizationExpand, setOrganizationExpand] = useState(AppState.menuType.type !== 'project');
  const [binExpand, setBinExpand] = useState(false);
  const formatMessage = useFormatMessage('knowledge.common');

  const handleCreateBase = () => {
    openCreateBaseModal({ onCallBack: type === 'project' ? knowledgeHomeStore.axiosProjectBaseList : knowledgeHomeStore.axiosOrgBaseList, type });
  };

  const handleChangeExpand = (baseType) => {
    if (baseType === 'project') {
      setProjectExpand(!projectExpand);
    } else if (baseType === 'organization') {
      setOrganizationExpand(!organizationExpand);
    } else if (baseType === 'bin') {
      setBinExpand(!binExpand);
    }
  };

  useEffect(() => {
    if (type === 'project') {
      knowledgeHomeStore.axiosProjectBaseList();
    } else {
      knowledgeHomeStore.axiosOrgBaseList();
    }
    binTableDataSet.query();
  }, []);

  return (
    <>
      <Header>
        <HeaderButtons items={[{
          name: formatMessage({ id: 'create' }),
          display: true,
          handler: handleCreateBase,
          icon: 'playlist_add',
        }]}
        />
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-container`}>
        {type === 'project' && (
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>{formatMessage({ id: 'project' })}</h1>
            <Icon type={`${projectExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('project'); }} />
          </div>
          {projectExpand && projectBaseList && projectBaseList.length > 0 && (
            <div className={`${prefixCls}-container-base-content`}>
              {
                projectBaseList.map((item) => <BaseItem key={item.id} item={item} baseType="project" />)
              }
            </div>
          )}
        </div>
        )}
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>{formatMessage({ id: 'organization' })}</h1>
            <Icon type={`${organizationExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('organization'); }} />
          </div>
          {organizationExpand && orgBaseList && orgBaseList.length > 0 && (
            <div className={`${prefixCls}-container-base-content`}>
              {
                orgBaseList.map((item) => <BaseItem key={item.id} item={item} baseType="organization" className={type === 'project' ? 'c7n-kb-orgBaseItem' : ''} />)
              }
            </div>
          )}
        </div>
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>{formatMessage({ id: 'recycle_bin' })}</h1>
            <Icon type={`${binExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('bin'); }} />
          </div>
          <div className={`${prefixCls}-container-base-binContent`}>
            {
              binExpand && (<BinTable type={type} />)
            }

          </div>
        </div>
      </Content>
    </>
  );
});

export default (props) => {
  const { prefixCls, type } = useContext(Store);
  return (
    <Page
      className={prefixCls}
    >
      <KnowledgeBases {...props} />
    </Page>
  );
};
