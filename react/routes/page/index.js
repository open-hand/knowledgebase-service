import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';
import { StoreProvider } from './stores';

const KnowledgeBases = asyncRouter(() => import('../knowledge-bases'));
const Doc = asyncRouter(() => import('./doc'));
const Version = asyncRouter(() => import('./version'));

export default function Index(props) {
  const { match } = props;
  return (
    <StoreProvider {...props}>
      <Switch>
        <Route exact path={`${match.url}`} component={KnowledgeBases} />
        <Route exact path={`${match.url}/doc/:baseId`} component={Doc} />
        <Route path={`${match.url}/version/:baseId`} component={Version} />
        <Route path="*" component={nomatch} />
      </Switch>
    </StoreProvider>
  );
}
