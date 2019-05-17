import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal,
} from 'choerodon-ui';
import {
  Page, Header, Content, axios, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import WorkSpace, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpace';
import DocEditor from '../../../components/DocEditor';
import DocViewer from '../../../components/DocViewer';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import DocEmpty from '../../../components/DocEmpty';
import DocStore from '../../../stores/organization/doc/DocStore';
import './DocHome.scss';

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const head = document.getElementsByClassName('page-header');
    const menu = document.getElementById('menu');
    head.style.display = 'none';
    menu.style.display = 'none';
  }

  render() {
    return (
      <Page
        className="c7n-knowledge"
      >
        <Header>
          <Button
            type="primary"
            funcType="raised"
            onClick={this.handleNewDoc}
          >
            <FormattedMessage id="doc.create" />
          </Button>
          <Button
            funcType="flat"
            onClick={this.handleEditDoc}
          >
            <Icon type="playlist_add icon" />
            <FormattedMessage id="edit" />
          </Button>
          <Button
            funcType="flat"
            onClick={this.handleRefresh}
          >
            <Icon type="refresh icon" />
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content style={{ padding: 0 }}>
          test
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
