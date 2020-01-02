import React, { useContext, useState } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page, Header, Content, Breadcrumb, stores,
} from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { Button, Modal } from 'choerodon-ui/pro';
import BaseItem from './components/baseItem';
import { openCreateBaseModal } from './components/baseModal';
import BinTable from './components/binTable';

import Store from './stores';
import './KnowledgeHome.less';

const createBaseModal = Modal.key();
const { AppState } = stores;

const KnowledgeBases = observer(() => {
  const { prefixCls } = useContext(Store);
  const [projectExpand, setProjectExpand] = useState(true);
  const [organizationExpand, setOrganizationExpand] = useState(AppState.menuType.type !== 'project');
  const [binExpand, setBinExpand] = useState(false);

  const handleCreateBase = () => {
    openCreateBaseModal();
  };

  const handleChangeExpand = (type) => {
    if (type === 'project') {
      setProjectExpand(!projectExpand);
    } else if (type === 'organization') {
      setOrganizationExpand(!organizationExpand);
    } else if (type === 'bin') {
      setBinExpand(!binExpand);
    }
  };

  return (
    <Page 
      className={prefixCls}
      service={[
        'base-service.organization-project.listProjectsByOrgId',
      ]}
    >
      <Header>
        <Button className={`${prefixCls}-createBaseBtn`} onClick={handleCreateBase}>
          <Icon type="playlist_add icon" />
          创建知识库
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-container`}>
        {AppState.menuType.type === 'project' && (
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>本项目知识库</h1>
            <Icon type={`${projectExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('project'); }} />

          </div>
          <div className={`${prefixCls}-container-base-content ${projectExpand ? 'isExpand' : 'notExpand'}`}>
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
            <BaseItem />
          </div>
        </div>
        )}
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>本组织知识库</h1>
            <Icon type={`${organizationExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('organization'); }} />
          </div>
          <div className={`${prefixCls}-container-base-content ${organizationExpand ? 'isExpand' : 'notExpand'}`}>
            <BaseItem />
          </div>
        </div>
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>回收站</h1>
            <Icon type={`${binExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('bin'); }} />
          </div>
          <div className={`${prefixCls}-container-base-binContent ${binExpand ? 'isExpand' : 'notExpand'}`}>
            <BinTable />
          </div>
        </div>
      </Content>
    </Page>
  );
});

export default KnowledgeBases;
