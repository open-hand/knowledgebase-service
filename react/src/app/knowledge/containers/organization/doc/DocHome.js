import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal, Spin, Input,
} from 'choerodon-ui';
import {
  Page, Header, Content, axios, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import DocEmpty from '../../../components/DocEmpty';
import DocEditor from '../../../components/DocEditor';
import DocViewer from '../../../components/DocViewer';
import DocCatalog from '../../../components/DocCatalog';
import DocDetail from '../../../components/DocDetail';
import DocStore from '../../../stores/organization/doc/DocStore';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpace';
import './DocHome.scss';

const { confirm } = Modal;
const { Section, Divider } = ResizeContainer;
const { AppState, MenuStore } = stores;
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
      loading: false,
      docLoading: false,
      currentNav: 'attachment',
      newTitle: false,
    };
    // this.newDocLoop = false;
  }

  componentDidMount() {
    this.initCurrentMenuType();
    MenuStore.setCollapsed(true);
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

  // componentWillUnmount() {
  //   clearInterval(this.newDocLoop);
  // }

  initCurrentMenuType = () => {
    DocStore.initCurrentMenuType(AppState.currentMenuType);
  };

  refresh = () => {
    this.setState({
      loading: true,
      edit: false,
    });
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
        loading: false,
      });
    } else {
      this.setState({
        selectId: false,
        loading: false,
      });
      DocStore.setDoc(false);
    }
  };

  handleSave = (md, type) => {
    const { newTitle } = this.state;
    const docData = DocStore.getDoc;
    const doc = {
      title: (newTitle && newTitle.trim()) || docData.pageInfo.title,
      content: md,
      minorEdit: type === 'edit',
      objectVersionNumber: docData.objectVersionNumber,
    };
    DocStore.editDoc(docData.workSpace.id, doc);
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

  handleTitleChange = (title) => {
    const docData = DocStore.getDoc;
    const doc = {
      title,
      objectVersionNumber: docData.objectVersionNumber,
    };
    DocStore.editDoc(docData.workSpace.id, doc);
  };

  onTitleChange = (e) => {
    this.setState({
      newTitle: e.target.value,
    });
  };

  /**
   * 点击空间
   * @param data
   * @param selectId
   */
  handleSpaceClick = (data, selectId) => {
    this.setState({
      docLoading: true,
      selectId,
      edit: false,
    }, () => {
      DocStore.setWorkSpace(data);
    });
    // 加载详情
    DocStore.loadDoc(selectId).then(() => {
      this.setState({
        docLoading: false,
      });
    });
  };

  handleSpaceExpand = (data, itemId) => {
    const { hasEverExpand } = this.state;
    const itemIds = data.items[itemId].children;
    // 更新展开的数据
    DocStore.setWorkSpace(data);
    // 如果之前没有被展开过，预先加载子级的子级
    if (hasEverExpand.indexOf(itemId) === -1) {
      this.setState({
        hasEverExpand: [...hasEverExpand, itemId],
      });
      DocStore.loadWorkSpaceByParent(itemIds);
    }
  };

  handleSpaceCollapse = (data) => {
    DocStore.setWorkSpace(data);
  };

  handleSpaceDragEnd = (newData, source, destination) => {
    const spaceData = DocStore.getWorkSpace;
    const sourceId = spaceData.items[source.parentId].children[source.index];
    const destId = destination.parentId;
    const destItems = spaceData.items[destination.parentId].children;
    let before = true;
    let targetId = 0;
    if (destination.index) {
      before = false;
      targetId = destItems[destination.index - 1];
    } else if (destination.index === 0 && destItems.length) {
      targetId = destItems[destination.index];
    } else if (destItems.length) {
      before = false;
      targetId = destItems[destItems.length - 1];
    }
    DocStore.moveWorkSpace(destId, {
      id: sourceId,
      before,
      targetId,
    });
    DocStore.setWorkSpace(newData);
  };

  handlePressEnter = (value, item) => {
    const { selectId } = this.state;
    let newTree = DocStore.getWorkSpace;
    const dto = {
      title: value,
      content: '',
      workspaceId: item.parentId,
    };
    DocStore.createWorkSpace(dto).then((data) => {
      if (selectId) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, isClick: true },
        'create',
      );
      this.handleSpaceClick(newTree, data.workSpace.id);
      // DocStore.setWorkSpace(newTree);
    });
  };

  handleCreateBlur = (item) => {
    const spaceData = DocStore.getWorkSpace;
    const newTree = removeItemFromTree(spaceData, item);
    DocStore.setWorkSpace(newTree);
  };

  /**
   * 空间上创建子空间
   * @param data
   */
  handleCreateWorkSpace = (data) => {
    const { hasEverExpand } = this.state;
    const spaceData = DocStore.getWorkSpace;
    // 构建虚拟空间节点
    const item = {
      data: { title: 'create' },
      hasChildren: false,
      isExpanded: false,
      id: 'create',
      parentId: data.id,
    };
    const newTree = addItemToTree(spaceData, item);
    DocStore.setWorkSpace(newTree);
    // 如果有子级且没有被加载过，加载子级的子级
    const itemIds = newTree.items[data.id].children.filter(id => id !== 'create');
    if (itemIds.length && hasEverExpand.indexOf(data.id) === -1) {
      this.setState({
        hasEverExpand: [...hasEverExpand, data.id],
      });
      DocStore.loadWorkSpaceByParent(itemIds);
    }
  };

  handleDeleteWorkSpace = (data) => {
    this.handleDeleteDoc(data.id);
  };

  handleNewDoc = () => {
    // const winObj = window.open('https://www.baidu.com', '', 'width=600,height=251,location=no,menubar=no,resizable=0,top=200px,left=400px');
    const urlParams = AppState.currentMenuType;
    const winObj = window.open(`/#/knowledge/organizations/create?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}`, '');
    this.newDocLoop = setInterval(() => {
      if (winObj.closed) {
        this.refresh();
        clearInterval(this.newDocLoop);
      }
    }, 1);
  };

  handleEditDoc = () => {
    this.setState({
      edit: true,
    });
  };

  handleRefresh = () => {
    const { selectId, sideBarVisible } = this.state;
    if (selectId) {
      DocStore.loadDoc(selectId);
      if (sideBarVisible) {
        const docData = DocStore.getDoc;
        DocStore.loadAttachment(docData.pageInfo.id);
        DocStore.loadComment(docData.pageInfo.id);
        DocStore.loadLog(docData.pageInfo.id);
      }
    } else {
      this.refresh();
    }
  };

  handleBtnClick = (type) => {
    const { selectId, catalogVisible } = this.state;
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
        this.setState({
          currentNav: 'attachment',
          sideBarVisible: true,
        });
        break;
      case 'comment':
        this.setState({
          currentNav: 'comment',
          sideBarVisible: true,
        });
        break;
      case 'log':
        this.setState({
          currentNav: 'log',
          sideBarVisible: true,
        });
        break;
      case 'catalog':
        this.setState({
          catalogVisible: !catalogVisible,
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
      content: `如果文章下面有子级，也会被同时删除，确定要删除文章"${item.data.title}"吗?`,
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
    const {
      edit, selectId, catalogVisible, docLoading,
      sideBarVisible, loading, currentNav,
    } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;

    return (
      <Page
        className="c7n-knowledge"
      >
        <Header title="文档管理">
          <Button
            funcType="flat"
            onClick={() => this.handleCreateWorkSpace({ id: 0 })}
          >
            <Icon type="playlist_add icon" />
            <FormattedMessage id="doc.create" />
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
          {
            loading ? (
              <div
                className="c7n-knowledge-spin"
              >
                <Spin />
              </div>
            ) : null
          }
          <ResizeContainer type="horizontal">
            <Section
              size={{
                width: 200,
                minWidth: 100,
                maxWidth: 600,
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
                  onCreate={this.handleCreateWorkSpace}
                  onDelete={this.handleDeleteWorkSpace}
                />
              </div>
            </Section>
            <Divider />
            <Section
              style={{ flex: 'auto' }}
              size={{
                width: 'auto',
              }}
            >
              {
                docLoading ? (
                  <div
                    className="c7n-knowledge-spin"
                  >
                    <Spin />
                  </div>
                ) : null
              }
              <div className="c7n-knowledge-right">
                {selectId && docData
                  ? (edit
                    ? (
                      <span>
                        <span style={{ marginLeft: 20 }}>
                          {'标题：'}
                        </span>
                        <Input
                          showLengthInfo={false}
                          maxLength={40}
                          style={{ width: 520, margin: 10 }}
                          defaultValue={docData.pageInfo.title}
                          onChange={this.onTitleChange}
                        />
                        <DocEditor
                          data={docData.pageInfo.souceContent}
                          onSave={this.handleSave}
                          onCancel={this.handleCancel}
                        />
                      </span>
                    )
                    : (
                      <DocViewer
                        data={docData}
                        onBtnClick={this.handleBtnClick}
                        loginUserId={loginUserId}
                        permission={isAdmin || loginUserId === docData.createdBy}
                        onTitleEdit={this.handleTitleChange}
                        catalogVisible={catalogVisible}
                      />
                    )
                  )
                  : (
                    <DocEmpty />
                  )
                }
              </div>
            </Section>
            {catalogVisible
              ? (
                <Divider />
              ) : null
            }
            {catalogVisible
              ? (
                <Section
                  size={{
                    width: 200,
                    minWidth: 200,
                    maxWidth: 400,
                  }}
                >
                  <DocCatalog />
                </Section>
              ) : null
            }
            {sideBarVisible
              ? (
                <DocDetail
                  store={DocStore}
                  currentNav={currentNav}
                  onCollapse={() => {
                    this.setState({
                      sideBarVisible: false,
                    });
                  }}
                />
              ) : null
            }
          </ResizeContainer>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
