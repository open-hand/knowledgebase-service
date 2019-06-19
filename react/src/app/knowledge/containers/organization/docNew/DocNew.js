import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Input, Icon } from 'choerodon-ui';
import {
  Page, Content, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import DocEditor from '../../../components/DocEditor';
import DocStore from '../../../stores/organization/doc/DocStore';
import './DocNew.scss';

const { AppState } = stores;

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.initCurrentMenuType();
  }

  paramConverter = (url) => {
    const reg = /[^?&]([^=&#]+)=([^&#]*)/g;
    const retObj = {};
    url.match(reg).forEach((item) => {
      const [tempKey, paramValue] = item.split('=');
      const paramKey = tempKey[0] !== '&' ? tempKey : tempKey.substring(1);
      Object.assign(retObj, {
        [paramKey]: paramValue,
      });
    });
    return retObj;
  };

  initCurrentMenuType = () => {
    const { history } = this.props;
    const { search } = history.location;
    const menuType = this.paramConverter(search);
    DocStore.initCurrentMenuType(menuType);
  };

  handleCreateDoc = (md) => {
    const dto = {
      title: '123',
      content: md,
      parentWorkspaceId: 0,
    };
    DocStore.createDoc(dto).then((data) => {
      localStorage.removeItem('importDoc');
      // window.close();
    });
  };

  render() {
    const docData = localStorage.getItem('importDoc');
    return (
      <Page
        className="c7n-docNew"
      >
        <Content>
          <div>
            <Input label="标题" showLengthInfo={false} style={{ width: 520 }} />
          </div>
          <div style={{ marginBottom: 20 }}>
            <div>位置</div>
            <div style={{ borderBottom: '1px solid #000', width: 520, padding: 5 }}>
              /
              <Icon type="device_hub" style={{ float: 'right' }} />
            </div>
          </div>
          <DocEditor
            height="calc(100% - 40px)"
            data={docData || ''}
            onSave={this.handleCreateDoc}
            onCancel={this.handleCancel}
          />
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
