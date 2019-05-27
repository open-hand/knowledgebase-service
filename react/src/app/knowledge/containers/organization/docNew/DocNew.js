import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal, Input,
} from 'choerodon-ui';
import {
  Page, Header, Content, axios, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import DocEditor from '../../../components/DocEditor';
import DocStore from '../../../stores/organization/doc/DocStore';
import './DocNew.scss';

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const head = document.getElementsByClassName('page-header');
    const menu = document.getElementById('menu');
    if (head.length && head[0] && head[0].style) {
      head[0].style.display = 'none';
    }
    if (menu && menu.style) {
      menu.style.display = 'none';
    }
  }

  handleCreateWorkSpace = (workSpaceId, md) => {
    const dto = {
      title: '123',
      content: md,
      workspaceId: 0,
    };
    DocStore.createWorkSpace(dto).then((data) => {
      window.close();
    });
  };

  render() {
    return (
      <Page
        className="c7n-docNew"
      >
        <Content style={{ padding: 50 }}>
          <div>
            <div>标题</div>
            <Input showLengthInfo={false} style={{ width: 520 }} />
          </div>
          <div style={{ marginBottom: 20 }}>
            <div>位置</div>
            <div style={{ borderBottom: '1px solid #000', width: 520, padding: 5 }}>
              /
            </div>
          </div>
          <DocEditor
            onSave={this.handleCreateWorkSpace}
            onCancel={this.handleCancel}
          />
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
