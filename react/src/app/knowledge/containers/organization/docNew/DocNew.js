import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Input, Icon, Modal } from 'choerodon-ui';
import {
  Page, Content, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import DocEditor from '../../../components/DocEditor';
import WorkSpaceSelect from '../../../components/WorkSpaceSelect';
import DocStore from '../../../stores/organization/doc/DocStore';
import './DocNew.scss';

const { AppState } = stores;

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      spaceSelectVisible: false,
      selectId: 0,
      currentSelectId: false,
    };
  }

  componentDidMount() {
    this.initCurrentMenuType();
    this.getAllWorkSpace();
  }

  getAllWorkSpace = () => {
    DocStore.loadWorkSpaceAll();
  };

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
    let title = '';
    const titleEle = document.getElementById('importDocTitle');
    if (titleEle) {
      title = titleEle.value;
    }
    const { selectId } = this.state;
    if (title && title.trim()) {
      const dto = {
        title: title.trim(),
        content: md,
        parentWorkspaceId: selectId,
      };
      DocStore.createDoc(dto).then((data) => {
        localStorage.removeItem('importDoc');
        localStorage.removeItem('importDocTitle');
        window.newDocId = data.workSpace.id;
        window.close();
      });
    } else {
      Choerodon.prompt('文档标题不能为空！');
    }
  };

  handleCancel = () => {
    localStorage.removeItem('importDoc');
    localStorage.removeItem('importDocTitle');
    window.close();
  };

  handlePathClick = () => {
    const spaceData = DocStore.getWorkSpace;
    const { selectId } = this.state;
    this.setState({
      spaceSelectVisible: true,
      originData: spaceData,
      originSelectId: selectId,
    });
  };

  handlePathCancel = () => {
    const { originData, originSelectId } = this.state;
    DocStore.setWorkSpace(originData);
    this.setState({
      spaceSelectVisible: false,
      selectId: originSelectId,
      currentSelectId: false,
    });
  };

  /**
   * 点击空间
   * @param data
   * @param selectId
   */
  handleSpaceClick = (data, selectId) => {
    DocStore.setWorkSpace(data);
    this.setState({
      currentSelectId: selectId,
    });
  };

  handleSpaceChange = (data) => {
    DocStore.setWorkSpace(data);
  };

  handlePathChange = () => {
    const { currentSelectId } = this.state;
    this.setState({
      selectId: currentSelectId,
      currentSelectId: false,
      spaceSelectVisible: false,
    });
  };

  getPath = () => {
    const { selectId } = this.state;
    const spaceData = DocStore.getWorkSpace;
    if (selectId) {
      const data = spaceData.items[selectId];
      const parentIds = data.route && data.route.split('.');
      let path = '';
      parentIds.forEach((item) => {
        path += `/${spaceData.items[Number(item)] && spaceData.items[Number(item)].data.title}`;
      });
      return path;
    } else {
      return '/';
    }
  };

  render() {
    const { spaceSelectVisible, selectId, currentSelectId } = this.state;
    const docData = localStorage.getItem('importDoc');
    const title = localStorage.getItem('importDocTitle') || '';
    const spaceData = DocStore.getWorkSpace;
    return (
      <Page
        className="c7n-docNew"
      >
        <Content>
          <div>
            <Input id="importDocTitle" defaultValue={title} label="标题" showLengthInfo={false} style={{ width: 520 }} />
          </div>
          <div style={{ marginBottom: 20 }}>
            <div style={{ fontSize: 12 }}>位置</div>
            <div
              onClick={this.handlePathClick}
              style={{
                borderBottom: '1px solid #000',
                width: 520,
                padding: 5,
                color: '#3F51B5',
                cursor: 'pointer',
              }}
            >
              {this.getPath()}
              <Icon type="device_hub" style={{ float: 'right' }} />
            </div>
          </div>
          <DocEditor
            mode="create"
            height="calc(100% - 40px)"
            data={docData || ''}
            onSave={this.handleCreateDoc}
            onCancel={this.handleCancel}
          />
          {spaceSelectVisible
            ? (
              <Modal
                title="文档创建位置"
                visible={spaceSelectVisible}
                closable={false}
                onOk={this.handlePathChange}
                onCancel={this.handlePathCancel}
              >
                <div style={{ padding: 10, maxHeight: '300px', overflowY: 'scroll' }}>
                  <WorkSpaceSelect
                    data={spaceData}
                    selectId={currentSelectId || selectId}
                    onClick={this.handleSpaceClick}
                    onExpand={this.handleSpaceChange}
                    onCollapse={this.handleSpaceChange}
                  />
                </div>
              </Modal>
            ) : null
          }
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
