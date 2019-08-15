import React, { Component } from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/master';
import { StoreProvider } from './stores';

const Doc = asyncRouter(() => import('./doc'));
const FullScreen = asyncRouter(() => import('./fullScreen'));
const Version = asyncRouter(() => import('./version'));

export default function Index(props) {
  const { match } = props;
  return (
    <StoreProvider {...props}>
      <Switch>
        <Route exact path={match.url} component={Doc} />
        <Route path={`${match.url}/fullScreen`} component={FullScreen} />
        <Route path={`${match.url}/version`} component={Version} />
        <Route path="*" component={nomatch} />
      </Switch>
    </StoreProvider>
  );
}
