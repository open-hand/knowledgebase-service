import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import Log from './components/Log';
import './DocLog.less';

@inject('AppState')
@observer class DocLog extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.loadLogs();
  }

  loadLogs = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadLog(docData.pageInfo.id);
  };

  renderLogs() {
    const { store } = this.props;
    const logs = store.getLog;
    return (
      <Log
        data={logs}
      />
    );
  }

  render() {
    return (
      <div id="log" className="c7n-docLog">
        {this.renderLogs()}
      </div>
    );
  }
}

export default withRouter(injectIntl(DocLog));
