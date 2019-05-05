import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncLocaleProvider, asyncRouter, nomatch } from '@choerodon/boot';

const OrganizationDoc = asyncRouter(() => import('./organization/doc'));
const ProjectDoc = asyncRouter(() => import('./project/doc'));

@inject('AppState')
class KNOWLEDGEIndex extends React.Component {
    render() {
        const { match, AppState } = this.props;
        const langauge = AppState.currentLanguage;
        const IntlProviderAsync = asyncLocaleProvider(langauge, () => import(`../locale/${langauge}`));
        return (
            <IntlProviderAsync>
            <Switch>
                <Route path={`${match.url}/organization`} component={OrganizationDoc} />
                <Route path={`${match.url}/project`} component={ProjectDoc} />
                <Route path={'*'} component={nomatch} />
            </Switch>
            </IntlProviderAsync>
        );
    }
}

export default KNOWLEDGEIndex;

