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

const { confirm } = Modal;
const { Section, Divider } = ResizeContainer;
const { AppState } = stores;
let loginUserId = false;
let isAdmin = false;

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      edit: false,
      sideBarVisible: false,
      catalogVisible: false,
      selectId: false,
      hasEverExpand: [],
    };
  }

  componentDidMount() {
    this.refresh();
    axios.all([
      axios.get('/iam/v1/users/self'),
      axios.post('/iam/v1/permissions/checkPermission', [{
        code: 'agile-service.project-info.updateProjectInfo',
        organizationId: AppState.currentMenuType.organizationId,
        projectId: AppState.currentMenuType.id,
        resourceType: 'project',
      }]),
    ])
      .then(axios.spread((users, permission) => {
        loginUserId = users.id;
        isAdmin = permission[0].approve;
      }));
  }

  refresh = () => {
    DocStore.loadWorkSpace().then(() => {
      this.setState({
        hasEverExpand: [],
      });
      this.initSelect();
    });
  };

  initSelect =() => {
    const spaceData = DocStore.getWorkSpace;
    // 默认选中第一篇文章
    if (spaceData.items && spaceData.items['0'] && spaceData.items['0'].children.length) {
      const selectId = spaceData.items['0'].children[0];
      // 加载第一篇文章
      DocStore.loadDoc(selectId);
      // 选中第一篇文章菜单
      const newTree = mutateTree(spaceData, selectId, { isClick: true });
      DocStore.setWorkSpace(newTree);
      this.setState({
        selectId,
      });
    } else {
      this.setState({
        selectId: false,
      });
      DocStore.setDoc({
        title: '',
        content: '',
      });
    }
  };

  handleSave = (workSpaceId, md, type) => {
    const doc = {
      content: md,
      minorEdit: type === 'edit',
    };
    DocStore.editDoc(workSpaceId, doc);
    if (type === 'save') {
      this.setState({
        edit: false,
      });
    }
  };

  handleCancel = () => {
    this.setState({
      edit: false,
    });
  };

  handleSpaceClick = (data, selectId) => {
    DocStore.setWorkSpace(data);
    DocStore.loadDoc(selectId);
    this.setState({
      selectId,
      edit: false,
    });
  };

  handleSpaceExpand = (data, itemId) => {
    const { hasEverExpand } = this.state;
    const items = data.items[itemId].children;
    DocStore.setWorkSpace(data);
    if (hasEverExpand.indexOf(itemId) === -1) {
      this.setState({
        hasEverExpand: [...hasEverExpand, itemId],
      });
      DocStore.loadWorkSpaceByParent(items);
    }
  };

  handleSpaceCollapse = (data) => {
    DocStore.setWorkSpace(data);
  };

  handleSpaceDragEnd = (data) => {
    DocStore.setWorkSpace(data);
  };

  handlePressEnter = (value, item) => {
    const spaceData = DocStore.getWorkSpace;
    const dto = {
      title: value,
      content: '',
      workspaceId: item.parentId,
    };
    DocStore.createWorkSpace(dto).then((data) => {
      const newTree = addItemToTree(
        spaceData,
        {
          ...data,
          id: data.workSpaceId,
          data: {
            title: data.title,
          },
          children: [],
          parentId: item.parentId,
        },
        'create',
      );
      DocStore.setWorkSpace(newTree);
    });
  };

  handleCreateBlur = (item) => {
    const spaceData = DocStore.getWorkSpace;
    const newTree = removeItemFromTree(spaceData, item);
    DocStore.setWorkSpace(newTree);
  };

  handleCreateDoc = () => {
    const { selectId } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const item = {
      // children: [],
      data: { title: 'create' },
      hasChildren: false,
      isExpanded: false,
      id: 'create',
      parentId: selectId || 0,
    };
    const newTree = addItemToTree(spaceData, item);
    DocStore.setWorkSpace(newTree);
  };

  handleEditDoc = () => {
    this.setState({
      edit: true,
    });
  };

  handleRefresh = () => {
    this.refresh();
  };

  handleBtnClick = (type) => {
    const { selectId } = this.state;
    switch (type) {
      case 'delete':
        this.handleDeleteDoc(selectId);
        break;
      case 'edit':
        this.setState({
          edit: true,
        });
        break;
      case 'attach':
      case 'comment':
      case 'log':
        this.setState({
          sideBarVisible: true,
        });
        break;
      case 'catalog':
        this.setState({
          catalogVisible: true,
        });
        break;
      default:
        break;
    }
  };

  handleDeleteDoc = (selectId) => {
    const spaceData = DocStore.getWorkSpace;
    const item = spaceData.items[selectId];
    const that = this;
    confirm({
      title: `删除文章"${item.data.title}"`,
      content: `确定要删除文章"${item.data.title}"吗?`,
      okText: '删除',
      cancelText: '取消',
      width: 520,
      onOk() {
        DocStore.deleteDoc(selectId).then(() => {
          const newTree = removeItemFromTree(spaceData, {
            ...item,
            parentId: item.parentId || item.workSpaceParentId || 0,
          });
          DocStore.setWorkSpace(newTree);
          that.initSelect();
        }).catch((error) => {
        });
      },
      onCancel() {
      },
    });
  };

  render() {
    const { edit, selectId, catalogVisible, sideBarVisible } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;

    return (
      <Page
        className="c7n-knowledge"
      >
        <Header>
          <Button
            type="primary"
            funcType="raised"
            onClick={this.handleCreateDoc}
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
          <ResizeContainer type="horizontal">
            <Section size={{
              width: 248,
              minWidth: 100,
              maxWidth: 400,
            }}
            >
              <div className="c7n-knowledge-left">
                <WorkSpace
                  data={spaceData}
                  selectId={selectId}
                  onClick={this.handleSpaceClick}
                  onExpand={this.handleSpaceExpand}
                  onCollapse={this.handleSpaceCollapse}
                  onDragEnd={this.handleSpaceDragEnd}
                  onPressEnter={this.handlePressEnter}
                  onCreateBlur={this.handleCreateBlur}
                />
              </div>
            </Section>
            <Divider />
            <Section size={{
              width: '100%',
            }}
            >
              <div className="c7n-knowledge-right">
                {selectId
                  ? (edit
                    ? (
                      <DocEditor
                        data={docData}
                        onSave={this.handleSave}
                        onCancel={this.handleCancel}
                      />
                    )
                    : (
                      <DocViewer
                        data={docData}
                        onBtnClick={this.handleBtnClick}
                        loginUserId={loginUserId}
                        permission={isAdmin || loginUserId === docData.createdBy}
                      />
                    )
                  )
                  : (
                    <DocEmpty />
                  )
                }
              </div>
            </Section>
          </ResizeContainer>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
