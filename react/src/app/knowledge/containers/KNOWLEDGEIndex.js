import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncLocaleProvider, asyncRouter, nomatch } from '@choerodon/boot';

const Doc = asyncRouter(() => import('./organization/doc'));
const DocNew = asyncRouter(() => import('./organization/docNew'));

@inject('AppState')
class KNOWLEDGEIndex extends React.Component {
  render() {
    const { match, AppState } = this.props;
    const langauge = AppState.currentLanguage;
    const IntlProviderAsync = asyncLocaleProvider(langauge, () => import(`../locale/${langauge}`));
    return (
      <IntlProviderAsync>
        <Switch>
          <Route path={`${match.url}/organization`} component={Doc} />
          <Route path={`${match.url}/organizations/create`} component={DocNew} />
          <Route path={`${match.url}/project`} component={Doc} />
          <Route path={`${match.url}/project/create`} component={DocNew} />
          <Route path="*" component={nomatch} />
        </Switch>
      </IntlProviderAsync>
    );
  }
}

export default KNOWLEDGEIndex;
