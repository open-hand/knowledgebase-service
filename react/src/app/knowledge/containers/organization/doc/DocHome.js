import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal, Spin, Input, Collapse,
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
import DocVersion from '../../../components/DocVersion';
import DocStore from '../../../stores/organization/doc/DocStore';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpace';
import './DocHome.scss';

const { confirm } = Modal;
const { Panel } = Collapse;
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
      versionVisible: false,
      sideBarVisible: false,
      catalogVisible: false,
      selectId: false,
      selectProId: false,
      hasEverExpand: [],
      hasEverExpandPro: {},
      loading: true,
      docLoading: true,
      currentNav: 'attachment',
      newTitle: false,
      saving: false,
      hasChange: false, // 文档是否修改
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
    const { type } = AppState.currentMenuType;
    this.setState({
      loading: true,
      edit: false,
    });
    DocStore.loadWorkSpace().then(() => {
      this.setState({
        hasEverExpand: [],
      });
      this.initSelect();
    }).catch(() => {
      this.setState({
        loading: false,
        docLoading: false,
      });
    });
    // 先隐藏项目层数据
    if (type === 'organizationx') {
      DocStore.loadProWorkSpace();
    }
  };

  initSelect =() => {
    const spaceData = DocStore.getWorkSpace;
    // 默认选中第一篇文档
    if (spaceData.items && spaceData.items['0'] && spaceData.items['0'].children.length) {
      const selectId = spaceData.items['0'].children[0];
      // 加载第一篇文档
      DocStore.loadDoc(selectId);
      // 选中第一篇文档菜单
      const newTree = mutateTree(spaceData, selectId, { isClick: true });
      DocStore.setWorkSpace(newTree);
      this.setState({
        selectId,
        loading: false,
        docLoading: false,
        edit: false,
      });
    } else {
      this.setState({
        selectId: false,
        loading: false,
        docLoading: false,
        edit: false,
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
    // 重置title
    this.setState({
      newTitle: false,
    });
  };

  handleCancel = () => {
    this.setState({
      edit: false,
      hasChange: false,
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
   * 导航被点击跳转
   * @param id
   */
  handleBreadcrumbClick = (id) => {
    const { selectId } = this.state;
    const spaceData = DocStore.getWorkSpace;
    let newTree = mutateTree(spaceData, id, { isClick: true });
    if (selectId && newTree.items[selectId]) {
      newTree = mutateTree(newTree, selectId, { isClick: false });
    }
    this.handleSpaceClick(newTree, id);
  };

  /**
   * 点击空间
   * @param data
   * @param selectId
   */
  handleSpaceClick = (data, selectId) => {
    DocStore.setWorkSpace(data);
    const { selectProId, selectId: lastSelectId } = this.state;
    // 点击组织文档，清除项目选中
    if (selectProId) {
      const proWorkSpace = DocStore.getProWorkSpace;
      const newProTree = mutateTree(proWorkSpace[selectProId], lastSelectId, { isClick: false });
      DocStore.setProWorkSpace({
        ...proWorkSpace,
        [selectProId]: newProTree,
      });
    }
    this.setState({
      docLoading: true,
      selectId,
      edit: false,
      selectProId: false,
      saving: false,
      versionVisible: false,
      hasChange: false,
    });
    // 加载详情
    DocStore.loadDoc(selectId).then(() => {
      const { sideBarVisible, catalogVisible } = this.state;
      const docData = DocStore.getDoc;
      if (sideBarVisible) {
        DocStore.loadAttachment(docData.pageInfo.id);
        DocStore.loadComment(docData.pageInfo.id);
        DocStore.loadLog(docData.pageInfo.id);
      }
      if (catalogVisible) {
        DocStore.loadCatalog(docData.pageInfo.id);
      }
      this.setState({
        docLoading: false,
      });
    });
  };

  /**
   * 点击组织下项目的文档
   * @param data
   * @param selectId
   * @param proId
   */
  handleProSpaceClick = (data, selectId, proId) => {
    const { selectProId } = this.state;
    const { selectId: lastSelectId } = this.state;
    // 清空其他项目选中或组织层选中文档
    if (selectProId) {
      const proWorkSpace = DocStore.getProWorkSpace;
      const newProTree = mutateTree(proWorkSpace[selectProId], lastSelectId, { isClick: false });
      DocStore.setProWorkSpace({
        ...proWorkSpace,
        [selectProId]: newProTree,
        [proId]: data,
      });
    } else {
      const workSpace = DocStore.getWorkSpace;
      const proWorkSpace = DocStore.getProWorkSpace;
      const newTree = mutateTree(workSpace, lastSelectId, { isClick: false });
      DocStore.setWorkSpace(newTree);
      DocStore.setProWorkSpace({
        ...proWorkSpace,
        [proId]: data,
      });
    }
    this.setState({
      docLoading: true,
      selectId,
      edit: false,
      selectProId: proId,
      hasChange: false,
    });
    // 加载详情
    DocStore.loadProDoc(selectId, proId).then(() => {
      const { sideBarVisible, catalogVisible } = this.state;
      const docData = DocStore.getDoc;
      if (sideBarVisible) {
        DocStore.loadAttachment(docData.pageInfo.id);
        DocStore.loadComment(docData.pageInfo.id);
        DocStore.loadLog(docData.pageInfo.id);
      }
      if (catalogVisible) {
        DocStore.loadCatalog(docData.pageInfo.id);
      }
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

  handleProSpaceExpand = (data, itemId, proId) => {
    const { hasEverExpandPro } = this.state;
    const itemIds = data.items[itemId].children;
    // 更新展开的数据
    const proWorkSpace = DocStore.getProWorkSpace;
    DocStore.setProWorkSpace({
      ...proWorkSpace,
      [proId]: data,
    });
    // 如果之前没有被展开过，预先加载子级的子级
    if (!hasEverExpandPro[proId] || hasEverExpandPro[proId].indexOf(itemId) === -1) {
      this.setState({
        hasEverExpandPro: {
          ...hasEverExpandPro,
          [proId]: [
            ...(hasEverExpandPro[proId] || []),
            itemId,
          ],
        },
      });
      DocStore.loadProWorkSpaceByParent(itemIds, proId);
    }
  };

  handleSpaceCollapse = (data) => {
    DocStore.setWorkSpace(data);
  };

  handleProSpaceCollapse = (data, proId) => {
    const proWorkSpace = DocStore.getProWorkSpace;
    DocStore.setProWorkSpace({
      ...proWorkSpace,
      [proId]: data,
    });
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

  /**
   * 回车创建空间
   * @param value
   * @param item
   */
  handlePressEnter = (value, item) => {
    const { selectId, selectProId, saving } = this.state;
    if (!value || !value.trim() || saving) {
      return;
    }
    this.setState({
      saving: true,
    });
    let newTree = DocStore.getWorkSpace;
    const dto = {
      title: value,
      content: '',
      workspaceId: item.parentId,
    };
    DocStore.createWorkSpace(dto).then((data) => {
      if (!selectProId && selectId) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, isClick: true },
        'create',
      );
      this.handleSpaceClick(newTree, data.workSpace.id);
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

  handleDocChange = (hasChange) => {
    this.setState({
      hasChange,
    });
  };

  /**
   * 编辑状态下，提示保存
   * @param func
   * @param other
   */
  beforeQuitEdit = (func, ...other) => {
    const { hasChange } = this.state;
    const that = this;
    if (hasChange) {
      confirm({
        title: '文档尚未保存，确定离开吗？',
        okText: '确认',
        cancelText: '取消',
        onOk() {
          that[func](...other);
        },
      });
    } else {
      this[func](...other);
    }
  };

  handleRefresh = () => {
    const { selectId, sideBarVisible, catalogVisible } = this.state;
    this.setState({
      docLoading: true,
      hasChange: false,
    });
    if (selectId) {
      DocStore.loadDoc(selectId).then(() => {
        this.setState({
          docLoading: false,
        });
      }).catch(() => {
        this.setState({
          docLoading: false,
        });
      });
      if (sideBarVisible) {
        const docData = DocStore.getDoc;
        DocStore.loadAttachment(docData.pageInfo.id);
        DocStore.loadComment(docData.pageInfo.id);
        DocStore.loadLog(docData.pageInfo.id);
      }
      if (catalogVisible) {
        const docData = DocStore.getDoc;
        DocStore.loadCatalog(docData.pageInfo.id);
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
          catalogVisible: false,
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
      case 'version':
        this.setState({
          versionVisible: true,
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
      title: `删除文档"${item.data.title}"`,
      content: `如果文档下面有子级，也会被同时删除，确定要删除文档"${item.data.title}"吗?`,
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

  onBackBtnClick = () => {
    this.setState({
      versionVisible: false,
    });
    this.refresh();
  };

  render() {
    const {
      edit, selectId, catalogVisible, docLoading,
      sideBarVisible, loading, currentNav, selectProId,
      versionVisible,
    } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;
    const { type, name } = AppState.currentMenuType;
    const proWorkSpace = DocStore.getProWorkSpace;
    const proList = DocStore.getProList;

    return (
      <Page
        className="c7n-knowledge"
      >
        <Header title={versionVisible ? false : '文档管理'} className={versionVisible ? 'c7n-knowledge-noTitle' : ''}>
          {versionVisible
            ? (
              <span>
                <Button
                  type="primary"
                  onClick={this.onBackBtnClick}
                  className="back-btn small-tooltip"
                  shape="circle"
                  size="large"
                  icon="arrow_back"
                />
                <span className="page-head-title">
                  版本对比
                </span>
              </span>
            ) : (
              <span>
                <Button
                  funcType="flat"
                  onClick={() => this.handleCreateWorkSpace({ id: 0 })}
                >
                  <Icon type="playlist_add icon" />
                  <FormattedMessage id="doc.create" />
                </Button>
                <Button
                  funcType="flat"
                  onClick={() => this.beforeQuitEdit('handleRefresh')}
                >
                  <Icon type="refresh icon" />
                  <FormattedMessage id="refresh" />
                </Button>
              </span>
            )
          }
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
                minWidth: 200,
                maxWidth: 600,
              }}
            >
              <div className="c7n-knowledge-left">
                {/* 先隐藏项目层数据 */}
                {type === 'organizationx'
                  ? (
                    <Collapse bordered={false} defaultActiveKey={['0']}>
                      <Panel
                        header={(
                          <span>
                            <Icon type="domain" style={{ verticalAlign: 'text-bottom', margin: '0 5px' }} />
                            <span style={{ fontWeight: 'bold' }}>{name}</span>
                          </span>
                        )}
                        key="0"
                      >
                        <WorkSpace
                          data={spaceData}
                          selectId={selectId}
                          onClick={(data, id) => this.beforeQuitEdit('handleSpaceClick', data, id)}
                          onExpand={this.handleSpaceExpand}
                          onCollapse={this.handleSpaceCollapse}
                          onDragEnd={this.handleSpaceDragEnd}
                          onPressEnter={this.handlePressEnter}
                          onCreateBlur={this.handleCreateBlur}
                          onCreate={this.handleCreateWorkSpace}
                          onDelete={this.handleDeleteWorkSpace}
                        />
                      </Panel>
                      {
                        proList.length && proList.map(pro => (
                          <Panel
                            header={(
                              <span>
                                <Icon type="assignment" style={{ verticalAlign: 'text-bottom', margin: '0 5px' }} />
                                <span style={{ fontWeight: 'bold' }}>{pro.projectName}</span>
                              </span>
                            )}
                            key={pro.projectId}
                          >
                            <WorkSpace
                              mode="pro"
                              data={proWorkSpace[pro.projectId]}
                              selectId={selectId}
                              onClick={(newTree, itemId) => this.beforeQuitEdit('handleProSpaceClick', newTree, itemId, pro.projectId)}
                              onExpand={(newTree, itemId) => this.handleProSpaceExpand(newTree, itemId, pro.projectId)}
                              onCollapse={newTree => this.handleProSpaceCollapse(newTree, pro.projectId)}
                            />
                          </Panel>
                        ))
                      }
                    </Collapse>
                  ) : (
                    <WorkSpace
                      data={spaceData}
                      selectId={selectId}
                      onClick={(data, id) => this.beforeQuitEdit('handleSpaceClick', data, id)}
                      onExpand={this.handleSpaceExpand}
                      onCollapse={this.handleSpaceCollapse}
                      onDragEnd={this.handleSpaceDragEnd}
                      onPressEnter={this.handlePressEnter}
                      onCreateBlur={this.handleCreateBlur}
                      onCreate={this.handleCreateWorkSpace}
                      onDelete={this.handleDeleteWorkSpace}
                    />
                  )
                }
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
                ) : (
                  <div className="c7n-knowledge-right">
                    {selectId && docData
                      ? (edit
                        ? (
                          <span>
                            <Input
                              size="large"
                              showLengthInfo={false}
                              maxLength={40}
                              style={{ width: 650, margin: 10 }}
                              defaultValue={docData.pageInfo.title}
                              onChange={this.onTitleChange}
                            />
                            <DocEditor
                              data={docData.pageInfo.souceContent}
                              onSave={this.handleSave}
                              onCancel={this.handleCancel}
                              onChange={this.handleDocChange}
                            />
                          </span>
                        )
                        : (versionVisible
                          ? (
                            <DocVersion store={DocStore} onRollback={this.onBackBtnClick} />
                          ) : (
                            <DocViewer
                              mode={selectProId}
                              data={docData}
                              spaceData={spaceData}
                              onBreadcrumbClick={id => this.beforeQuitEdit('handleBreadcrumbClick', id)}
                              onBtnClick={this.handleBtnClick}
                              loginUserId={loginUserId}
                              permission={isAdmin || loginUserId === docData.createdBy}
                              onTitleEdit={this.handleTitleChange}
                              catalogVisible={catalogVisible}
                            />
                          )
                        )
                      )
                      : (
                        <DocEmpty />
                      )
                    }
                  </div>
                )
              }
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
                  <DocCatalog store={DocStore} />
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
