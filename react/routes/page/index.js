import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';
import { PermissionRoute } from '@choerodon/master';
import { StoreProvider } from './stores';

const KnowledgeBases = asyncRouter(() => import('../knowledge-bases'));
const Doc = asyncRouter(() => import('./doc'));
const Version = asyncRouter(() => import('./version'));

export default function Index(props) {
  const { match } = props;
  return (
    <StoreProvider {...props}>
      <Switch>
        <PermissionRoute
          service={(type) => (type === 'project' ? [
            'choerodon.code.project.cooperation.knowledge.ps.default',
            'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.createbase',
            'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.updatebase',
            'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.deletebase',
          ] : [
            'choerodon.code.organization.knowledge.ps.default',
            'choerodon.code.organization.knowledge.ps.recycle',
          ])}
          exact
          path={`${match.url}`}
          component={KnowledgeBases}
        />
        <PermissionRoute
          service={(type) => (
            type === 'project' ? [
            // 项目层
              'choerodon.code.project.cooperation.knowledge.ps.choerodon.code.project.cooperation.knowledge.page',
            ]
              : [
                'choerodon.code.organization.knowledge.ps.doc',
                'choerodon.code.organization.knowledge.ps.doc.delete',
                'choerodon.code.organization.knowledge.ps.template.delete',
                'choerodon.code.organization.knowledge.ps.page_comment.delete',
              ]
          )}
          exact
          path={`${match.url}/doc/:baseId`}
          component={Doc}
        />
        <Route path={`${match.url}/version/:baseId`} component={Version} />
        <Route path="*" component={nomatch} />
      </Switch>
    </StoreProvider>
  );
}
