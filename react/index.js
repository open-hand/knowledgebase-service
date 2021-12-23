import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { useRouteMatch } from 'react-router';
import { asyncLocaleProvider, nomatch, useCurrentLanguage } from '@choerodon/master';
import { ModalContainer } from 'choerodon-ui/pro';
import './index.less';

const Page = React.lazy(() => import('./routes/page'));
const Share = React.lazy(() => import('./routes/share'));

const KNOWLEDGEIndex = () => {
  const match = useRouteMatch();
  const language = useCurrentLanguage();
  const IntlProviderAsync = asyncLocaleProvider(language, () => import(`./locale/${language}/index.js`));
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
};

export default KNOWLEDGEIndex;
