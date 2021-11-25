import React, { Fragment } from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncLocaleProvider, asyncRouter, nomatch } from '@choerodon/boot';
import { ModalContainer } from 'choerodon-ui/pro';
import './index.less';

const Page = asyncRouter(() => import('./routes/page'));
const Share = asyncRouter(() => import('./routes/share'));

@inject('AppState')
class KNOWLEDGEIndex extends React.Component {
  render() {
    const { match, AppState } = this.props;
    const langauge = AppState.currentLanguage;
    const IntlProviderAsync = asyncLocaleProvider(langauge, () => import(`./locale/${langauge}/index.js`));
    return (
      <>
        <IntlProviderAsync>
          <Switch>
            <Route path={`${match.url}/organization`} component={Page} />
            <Route path={`${match.url}/project`} component={Page} />
            <Route path={`${match.url}/share/:token`} component={Share} />
            <Route path="*" component={nomatch} />
          </Switch>
          <ModalContainer />
        </IntlProviderAsync>
      </>
    );
  }
}

export default KNOWLEDGEIndex;
