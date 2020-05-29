import React, { useContext, useState, useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page, Header, Content, Breadcrumb, stores,
} from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { Button } from 'choerodon-ui/pro';
import BaseItem from './components/baseItem';
import { openCreateBaseModal } from './components/baseModal';
import BinTable from './components/binTable';

import Store from './stores';
import './KnowledgeHome.less';

const { AppState } = stores;

const KnowledgeBases = observer(() => {
  const { prefixCls, knowledgeHomeStore, type, binTableDataSet } = useContext(Store);
  const { projectBaseList, orgBaseList } = knowledgeHomeStore;
  const [projectExpand, setProjectExpand] = useState(true);
  const [organizationExpand, setOrganizationExpand] = useState(AppState.menuType.type !== 'project');
  const [binExpand, setBinExpand] = useState(false);

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
    <Fragment>
      <Header>
        <Button className={`${prefixCls}-createBaseBtn`} onClick={handleCreateBase}>
          <Icon type="playlist_add icon" />
          创建知识库
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-container`}>
        {type === 'project' && (
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>本项目知识库</h1>
            <Icon type={`${projectExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('project'); }} />
          </div>
          {projectExpand && projectBaseList && projectBaseList.length > 0 && (
            <div className={`${prefixCls}-container-base-content`}>
              {
                projectBaseList.map(item => <BaseItem key={item.id} item={item} baseType="project" />)
              }
            </div>
          )}
        </div>
        )}
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>本组织知识库</h1>
            <Icon type={`${organizationExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('organization'); }} />
          </div>
          {organizationExpand && orgBaseList && orgBaseList.length > 0 && (
            <div className={`${prefixCls}-container-base-content`}>
              {
                orgBaseList.map(item => <BaseItem key={item.id} item={item} baseType="organization" className={type === 'project' ? 'c7n-kb-orgBaseItem' : ''} />)
              }
            </div>
          )}
        </div>
        <div className={`${prefixCls}-container-base`}>
          <div className={`${prefixCls}-container-base-title`}>
            <h1>回收站</h1>
            <Icon type={`${binExpand ? 'expand_less' : 'expand_more'}`} role="none" onClick={() => { handleChangeExpand('bin'); }} />
          </div>
          <div className={`${prefixCls}-container-base-binContent`}>
            {
              binExpand && (<BinTable type={type} />)
            }

          </div>
        </div>
      </Content>
    </Fragment>
  );
});

export default (props) => {
  const { prefixCls, type } = useContext(Store);
  return (
    <Page
      className={prefixCls}
      service={type === 'project'
        ? [
          'choerodon.code.project.cooperation.knowledge.ps.default',
          'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.createbase',
          'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.updatebase',
          'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.deletebase',
        ]
        : [
          'choerodon.code.organization.knowledge.ps.default',
          'choerodon.code.organization.knowledge.ps.recycle',
        ]}
    >
      <KnowledgeBases {...props} />
    </Page>
  );
};
