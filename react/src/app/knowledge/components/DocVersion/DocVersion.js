import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Icon, Table } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import './DocVersion.scss';

@inject('AppState')
@observer class DocAttachment extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.loadVersions();
  }

  loadVersions = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadVersion(docData.pageInfo.id);
  };

  getColume = () => [
    {
      title: '版本',
      dataIndex: 'name',
    },
    {
      title: '创建时间',
      dataIndex: 'creationDate',
    },
  ];

  render() {
    const { store } = this.props;
    const versions = store.getVersion;

    return (
      <div className="c7n-docVersion" id="version">
        <div className="c7n-head-wrapper">
          <div className="c7n-head-left">
            <Icon type="attach_file c7n-icon-title" />
            <span>版本</span>
          </div>
          <div style={{
            flex: 1, height: 1, borderTop: '1px solid rgba(0, 0, 0, 0.08)', marginLeft: '14px',
          }}
          />
        </div>
        <div className="c7n-body-wrapper" style={{ marginTop: '-47px', justifyContent: 'flex-end' }}>
          <Table
            rowClassName="table-row"
            columns={this.getColume()}
            dataSource={versions}
            pagination={false}
            rowKey={record => record.id}
            filterBar={false}
          />
        </div>
      </div>
    );
  }
}

export default withRouter(injectIntl(DocAttachment));
