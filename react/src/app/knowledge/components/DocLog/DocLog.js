import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import Log from './components/Log';
import './DocLog.scss';

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
        <div className="c7n-head-wrapper">
          <div className="c7n-head-left">
            <Icon type="insert_invitation c7n-icon-title" />
            <FormattedMessage id="doc.log" />
          </div>
          <div
            style={{
              flex: 1,
              height: 1,
              borderTop: '1px solid rgba(0, 0, 0, 0.08)',
              marginLeft: '14px',
            }}
          />
        </div>
        {this.renderLogs()}
      </div>
    );
  }
}

export default withRouter(injectIntl(DocLog));
