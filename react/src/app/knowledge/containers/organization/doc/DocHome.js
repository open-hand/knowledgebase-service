import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal, Spin, Input, Checkbox,
} from 'choerodon-ui';
import {
  Page, Header, Content, stores,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import DocEmpty, { DocSearchEmpty } from '../../../components/DocEmpty/DocEmpty';
import DocCatalog from '../../../components/DocCatalog';
import DocDetail from '../../../components/DocDetail';
import SearchList from '../../../components/SearchList';
import EditMode from './components/editMode';
import ViewMode from './components/viewMode';
import DocModal from './components/docModal';
import DocStore from '../../../stores/organization/doc/DocStore';
import AttachmentRender from '../../../components/Extensions/attachment/AttachmentRender';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpace';
import WorkSpaceWrapper from '../../../components/WorkSpaceWrapper';
import { getCurrentFullScreen, toFullScreen, exitFullScreen, addFullScreenEventListener, removeFullScreenEventListener } from './components/fullScreen';
import './DocHome.scss';

const { confirm } = Modal;
const { Section, Divider } = ResizeContainer;
const { AppState, MenuStore } = stores;

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
      loading: true,
      docLoading: true,
      currentNav: 'attachment',
      newTitle: false,
      saving: false,
      hasChange: false, // 文档是否修改
      creating: false,
      path: false,
      searchVisible: false,
      searchValue: '',
      isFullScreen: false,
    };
    this.newDocLoop = false;
  }

  componentDidMount() {
    // 由于页面公用，设置项目/组织信息
    this.initCurrentMenuType();
    // 收起菜单
    MenuStore.setCollapsed(true);
    // 注册全屏事件
    addFullScreenEventListener(this.handleChangeFullScreen);
    // 加载数据
    this.refresh();
  }

  componentWillUnmount() {
    removeFullScreenEventListener(this.handleChangeFullScreen);
    clearInterval(this.newDocLoop);
  }

  handleChangeFullScreen = () => {    
    const currentFullScreen = getCurrentFullScreen();
    this.setState({
      isFullScreen: !!currentFullScreen,
    });
  };

  toggleFullScreen = () => {
    const currentFullScreen = getCurrentFullScreen();
    if (currentFullScreen) {
      exitFullScreen();
    } else {
      toFullScreen(document.documentElement);      
    }
  };

  searchRef = React.createRef();

  paramConverter = (url) => {
    const reg = /[^?&]([^=&#]+)=([^&#]*)/g;
    const retObj = {};
    if (url.match(reg)) {
      url.match(reg).forEach((item) => {
        const [tempKey, paramValue] = item.split('=');
        const paramKey = tempKey[0] !== '&' ? tempKey : tempKey.substring(1);
        Object.assign(retObj, {
          [paramKey]: paramValue,
        });
      });
    }
    return retObj;
  };

  initCurrentMenuType = () => {
    DocStore.initCurrentMenuType(AppState.currentMenuType);
  };

  refresh = () => {
    const { hash } = window.location;
    const search = hash && hash.split('?').length && hash.split('?')[1];
    const params = this.paramConverter(search);
    const id = params.docId;
    const { selectId } = this.state;
    this.setState({
      loading: true,
      edit: false,
      selectId: id || selectId,
    });
    DocStore.loadWorkSpaceAll(id || selectId).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        DocStore.loadWorkSpaceAll().then(() => {
          this.setState({
            selectId: false,
          }, () => {
            this.initSelect();
          });
        });
      } else {
        this.initSelect();
      }
    }).catch((e) => {
      this.setState({
        loading: false,
        docLoading: false,
      });
    });
  };

  /**
   *
   */
  initSelect =() => {
    const { selectId } = this.state;
    if (!selectId || selectId === '0') {
      const workSpace = DocStore.getWorkSpace;
      const { data: spaceData, code: spaceCode } = (workSpace.length && workSpace[0]) || {};
      // 默认选中第一篇文档
      if (spaceData && spaceData.items && spaceData.items['0'] && spaceData.items['0'].children.length) {
        const currentSelectId = spaceData.items['0'].children[0];
        // 加载第一篇文档
        DocStore.loadDoc(currentSelectId);
        // 选中第一篇文档菜单
        const newTree = mutateTree(spaceData, currentSelectId, { isClick: true });
        DocStore.setWorkSpaceMap(spaceCode, newTree);
        this.setState({
          selectId: currentSelectId,
          loading: false,
          docLoading: false,
          edit: false,
        });
        this.changeUrl(currentSelectId);
      } else {
        this.setState({
          selectId: false,
          loading: false,
          docLoading: false,
          edit: false,
        });
        DocStore.setDoc(false);
      }
    } else {
      this.changeUrl(selectId);
      DocStore.loadDoc(selectId).then((res) => {
        if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
          this.setState({
            selectId: false,
          }, () => {
            this.initSelect();
          });
        } else {
          this.setState({
            loading: false,
            docLoading: false,
            edit: false,
            versionVisible: false,
            sideBarVisible: false,
            catalogVisible: false,
          });
          DocStore.setImportVisible(false);
          DocStore.setShareVisible(false);
        }
      }).catch(() => {
        this.setState({
          loading: false,
          docLoading: false,
          edit: false,
          versionVisible: false,
          sideBarVisible: false,
          catalogVisible: false,
        });
        DocStore.setImportVisible(false);
        DocStore.setShareVisible(false);
      });
    }
  };

  editDoc = (type, id, doc) => {
    const { searchVisible } = this.state;
    DocStore.editDoc(id, doc).then(() => {
      // 点击保存，退出编辑模式
      if (type === 'save') {
        if (searchVisible) {
          this.onClickSearch(id);
        }
        this.setState({
          edit: false,
        });
      }
    });
  };

  handleSave = (md, type, editMode) => {
    const { newTitle } = this.state;
    const docData = DocStore.getDoc;
    if (type === 'autoSave') {
      const doc = {
        content: md,
      };
      DocStore.autoSaveDoc(docData.pageInfo.id, doc);
    } else {
      const doc = {
        title: (newTitle && newTitle.trim()) || docData.pageInfo.title,
        content: md,
        minorEdit: type === 'edit',
        objectVersionNumber: docData.pageInfo.objectVersionNumber,
      };
      // 修改默认编辑模式
      if (editMode) {
        const mode = {
          editMode,
          type: 'edit_mode',
        };
        if (docData.userSettingVO) {
          mode.id = docData.userSettingVO.id;
          mode.objectVersionNumber = docData.userSettingVO.objectVersionNumber;
        }
        DocStore.editDefaultMode(mode).then(() => {
          this.editDoc(type, docData.workSpace.id, doc);
        }).catch(() => {
          this.editDoc(type, docData.workSpace.id, doc);
        });
      } else {
        this.editDoc(type, docData.workSpace.id, doc);
      }
      // 重置title
      this.setState({
        newTitle: false,
      });
    }
  };

  handleCancel = () => {
    this.setState({
      edit: false,
      hasChange: false,
      path: false,
    });
    DocStore.setImportVisible(false);
    DocStore.setMoveVisible(false);
    DocStore.setShareVisible(false);
  };

  /**
   * 移动文档
   * @param docId 被移动文档id
   */
  closeDocMove = (docId) => {
    this.handleCancel();
    if (docId || docId === 0) { // 移动到顶级，id为0
      this.refresh();
    }
  };

  handleTitleChange = (title) => {
    const docData = DocStore.getDoc;
    const doc = {
      title,
      objectVersionNumber: docData.pageInfo.objectVersionNumber,
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
    const spaceData = DocStore.getCurrentSpace;
    let newTree = mutateTree(spaceData, id, { isClick: true });
    if (selectId && String(selectId) !== String(id) && newTree.items[selectId]) {
      newTree = mutateTree(newTree, selectId, { isClick: false });
    }
    DocStore.setCurrentSpace(newTree);
    this.handleSpaceClick(id);
  };

  changeUrl = (id) => {
    const { origin } = window.location;
    const { location } = this.props;
    const { pathname, search } = location;
    const params = this.paramConverter(search);
    let newParam = `?docId=${id}`;
    Object.keys(params).forEach((key, index) => {
      if (key !== 'docId') {
        newParam += `&${key}=${params[key]}`;
      }
    });
    const newUrl = `${origin}#${pathname}${newParam}`;
    window.history.pushState({}, 0, newUrl);
  };

  /**
   * 点击空间
   * @param selectId
   * @param mode 当创建时调用为create,自动进入编辑模式
   */
  handleSpaceClick = (selectId, mode) => {
    this.setState({
      searchValue: '',
      searchVisible: false,
    });
    this.handleCancel();
    const { selectId: lastSelectId } = this.state;
    if (String(lastSelectId) !== String(selectId)) {
      this.changeUrl(selectId);
      this.setState({
        docLoading: true,
        selectId,
        edit: mode === 'create', // 创建后，默认编辑模式
        saving: false,
        versionVisible: false,
        hasChange: false,
        catalogVisible: false,
      });
      // 创建后进入编辑，关闭侧边栏
      if (mode === 'create') {
        this.setState({
          sideBarVisible: false,
        });
      }
      // 加载详情
      DocStore.loadDoc(selectId).then(() => {
        const { sideBarVisible } = this.state;
        const docData = DocStore.getDoc;
        if (sideBarVisible) {
          DocStore.loadAttachment(docData.pageInfo.id);
          DocStore.loadComment(docData.pageInfo.id);
          DocStore.loadLog(docData.pageInfo.id);
        }
        this.setState({
          docLoading: false,
        });
      });
    } else {
      this.setState({
        versionVisible: false,
        catalogVisible: false,
      });
    }
  };

  /**
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  handleSpaceSave = (value, item) => {
    const workSpace = DocStore.getWorkSpace;
    // 可以操作的空间code
    const spaceCode = workSpace.length && workSpace[0].code;
    const workSpaceMap = DocStore.getWorkSpaceMap;
    const spaceData = spaceCode && workSpaceMap[spaceCode];
    const currentCode = DocStore.getSpaceCode;

    const { saving, selectId } = this.state;
    if (!spaceCode || !value || !value.trim() || saving) {
      return;
    }
    this.setState({
      saving: true,
      creating: false,
    });
    let newTree = spaceData;
    const vo = {
      title: value.trim(),
      content: '',
      parentWorkspaceId: item.parentId,
    };
    DocStore.createWorkSpace(vo).then((data) => {
      if (currentCode !== spaceCode) {
        const newSpace = mutateTree(workSpaceMap[currentCode], selectId, { isClick: false });
        this.updateWorkSpaceMap(newSpace, currentCode);
      }

      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, createdBy: data.createdBy, isClick: true },
        'create',
      );
      DocStore.setWorkSpaceMap(spaceCode, newTree);
      this.handleSpaceClick(data.workSpace.id, 'create');
    });
  };

  handleSpaceCancel = (item) => {
    this.setState({
      creating: false,
    });
    const spaceData = DocStore.getWorkSpace;
    const newTree = removeItemFromTree(spaceData, item);
    DocStore.setWorkSpace(newTree);
  };

  /**
   * 空间上创建子空间
   * @param data
   */
  handleCreateWorkSpace = (data) => {
    const { creating } = this.state;
    const workSpace = DocStore.getWorkSpace;
    // 可以操作的空间code
    const spaceCode = workSpace.length && workSpace[0].code;
    const workSpaceMap = DocStore.getWorkSpaceMap;
    const spaceData = spaceCode && workSpaceMap[spaceCode];
    if (!creating && spaceData) {
      this.setState({
        creating: true,
      });
      // 构建虚拟空间节点
      const item = {
        data: { title: 'create' },
        hasChildren: false,
        isExpanded: false,
        id: 'create',
        parentId: data.id,
      };
      const newTree = addItemToTree(spaceData, item);
      DocStore.setWorkSpaceMap(spaceCode, newTree);
    }
  };

  handleDeleteWorkSpace = (data, mode) => {
    this.handleDeleteDoc(data.id, mode);
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
        title: '编辑提示',
        content: '你这在编辑的内容尚未保存，确定离开吗？',
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
    const { selectId, sideBarVisible, searchValue, searchVisible } = this.state;
    this.setState({
      docLoading: true,
      hasChange: false,
      catalogVisible: false,
    });
    if (selectId) {
      DocStore.loadWorkSpaceAll(selectId);
      DocStore.loadDoc(selectId, searchValue).then(() => {
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
    } else {
      this.refresh();
    }
  };

  handleBtnClick = (type) => {
    const docData = DocStore.getDoc;
    const { id, title } = docData.pageInfo;
    const { id: workSpaceId } = docData.workSpace;
    const { catalogVisible } = this.state;
    switch (type) {
      case 'delete':
        this.handleDeleteDoc(workSpaceId);
        break;
      case 'adminDelete':
        this.handleDeleteDoc(workSpaceId, 'admin');
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
      case 'export':
        Choerodon.prompt('正在导出，请稍候...');
        DocStore.exportPdf(id, title);
        break;
      case 'share':
        this.shareDoc(workSpaceId);
        break;
      case 'move':
        DocStore.setMoveVisible(true);
        break;
      default:
        break;
    }
  };

  shareDoc = (id) => {
    DocStore.queryShareMsg(id).then(() => {
      DocStore.setShareVisible(true);
    });
  };

  handleDeleteDraft = () => {
    const docData = DocStore.getDoc;
    const hasDraft = DocStore.getDraftVisible;
    const { id } = docData.pageInfo;
    if (hasDraft) {
      DocStore.deleteDraftDoc(id).then(() => {
        this.handleRefresh();
        this.handleCancel();
      });
    } else {
      this.handleCancel();
    }
  };

  handleLoadDraft = () => {
    const docData = DocStore.getDoc;
    const { id } = docData.pageInfo;
    DocStore.loadDraftDoc(id).then(() => {
      this.setState({
        edit: true,
        catalogVisible: false,
      });
    });
  };

  handleDeleteDoc = (selectId, mode) => {
    const { searchVisible } = this.state;
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
        // 文档创建者和管理员调用不同删除接口
        if (mode === 'admin') {
          DocStore.adminDeleteDoc(selectId).then(() => {
            const newSelectId = item.parentId || item.workSpaceParentId || 0;
            const newTree = removeItemFromTree(spaceData, {
              ...item,
              parentId: newSelectId,
            });
            DocStore.setWorkSpace(newTree);
            if (searchVisible) {
              const newSearchList = DocStore.getSearchList.filter(search => search.pageId !== item.id);
              DocStore.setSearchList(newSearchList);
              if (newSearchList && newSearchList.length) {
                that.onClickSearch(newSearchList[0].pageId);
              } else {
                DocStore.setDoc(false);
              }
            } else {
              that.changeUrl(newSelectId);
              that.setState({
                selectId: newSelectId,
              }, () => that.handleRefresh());
            }
          }).catch((error) => {
            Choerodon.prompt('网络错误，请重试。');
          });
        } else {
          DocStore.deleteDoc(selectId).then(() => {
            const newSelectId = item.parentId || item.workSpaceParentId || 0;
            const newTree = removeItemFromTree(spaceData, {
              ...item,
              parentId: newSelectId,
            });
            DocStore.setWorkSpace(newTree);
            if (searchVisible) {
              const newSearchList = DocStore.getSearchList.filter(search => search.pageId !== item.id);
              DocStore.setSearchList(newSearchList);
              if (newSearchList && newSearchList.length) {
                that.onClickSearch(newSearchList[0].pageId);
              } else {
                DocStore.setDoc(false);
              }
            } else {
              that.changeUrl(newSelectId);
              that.setState({
                selectId: newSelectId,
              }, () => that.handleRefresh());
            }
          }).catch((error) => {
            Choerodon.prompt('网络错误，请重试。');
          });
        }
      },
      onCancel() {
      },
    });
  };

  onBackBtnClick = () => {
    this.setState({
      versionVisible: false,
    });
    this.handleRefresh();
  };

  handleImport = () => {
    DocStore.setImportVisible(true);
  };

  handlePathChange = (e) => {
    if (e && e.target && e.target.value) {
      this.setState({
        path: e.target.value.trim(),
      });
    }
  };

  migration = () => {
    const { path } = this.state;
    DocStore.migration(path).then((res) => {
      if (res && res.failed) {
        Choerodon.prompt('未找到文档，请检查路径填写是否正确！');
      } else {
        Choerodon.prompt('正在迁移，请耐心等待，稍后刷新查看！');
      }
    }).catch(() => {
      Choerodon.prompt('同步失败，请检查wiki服务是否运行正常！');
    });
    this.handleCancel();
  };

  handleSearch = (e) => {
    const { searchValue } = this.state;
    if (searchValue) {
      DocStore.querySearchList(searchValue).then((res) => {
        const searchList = DocStore.getSearchList;
        if (searchList && searchList.length) {
          this.onClickSearch(searchList[0].pageId, searchValue);
        }
        this.setState({
          searchVisible: true,
        });
      });
    } else {
      this.setState({
        searchVisible: false,
      });
    }
  };

  handleSearchChange = (e) => {
    const str = e.target.value && e.target.value.trim();
    this.setState({
      searchValue: e.target.value.trim(),
    });
    if (!str) {
      this.setState({
        searchVisible: false,
      });
    }
  };

  onClearSearch = () => {
    this.setState({
      searchValue: '',
      searchVisible: false,
    });
    this.refresh();
    this.handleCancel();
  };

  onClickSearch = (id) => {
    this.setState({
      selectId: id,
      versionVisible: false,
      catalogVisible: false,
      sideBarVisible: false,
    });
    const { searchValue } = this.state;
    DocStore.loadDoc(id, searchValue);
  };
 

  render() {
    const {
      edit, selectId, catalogVisible, docLoading, uploading,
      sideBarVisible, loading, currentNav, selectProId, moveVisible,
      versionVisible, shareVisible, importVisible, searchVisible,
      searchValue, isFullScreen,
    } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;
    const initialEditType = docData.userSettingVO ? docData.userSettingVO.editMode : 'markdown';

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
              <span style={{ display: 'flex', justifyContent: 'space-between', width: 'calc(100% - 160px)' }}>
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
                  <Button
                    funcType="flat"
                    onClick={() => this.beforeQuitEdit('handleImport')}
                  >
                    <Icon type="archive icon" />
                    <FormattedMessage id="import" />
                  </Button>
                  <Button onClick={this.toggleFullScreen}>
                    <Icon type={isFullScreen ? 'fullscreen_exit' : 'zoom_out_map'} />
                    {isFullScreen ? <FormattedMessage id="exitFullScreen" /> : <FormattedMessage id="fullScreen" />}
                  </Button>
                </span>
                <span className="c7n-knowledge-search">
                  <Input
                    value={searchValue}
                    onPressEnter={this.handleSearch}
                    onChange={this.handleSearchChange}
                    suffix={(
                      <Icon
                        type="search"
                        className="c7n-knowledge-search-icon"
                        onClick={this.handleSearch}
                      />
                    )}
                  />
                </span>
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
              style={{
                minWidth: 200,
                maxWidth: 600,
              }}
            >
              <div className="c7n-knowledge-left">
                <WorkSpaceWrapper
                  data={spaceData}
                  selectId={selectId}
                  onClick={(data, id) => this.beforeQuitEdit('handleSpaceClick', data, id)}
                  onSave={this.handleSpaceSave}
                  onCancel={this.handleSpaceCancel}
                  onCreate={this.handleCreateWorkSpace}
                  onDelete={this.handleDeleteWorkSpace}
                  onShare={this.shareDoc}
                  store={DocStore}
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
                ) : (
                  <div className={`c7n-knowledge-${searchVisible ? 'searchDoc' : 'doc'}`}>
                    {searchVisible
                      ? (
                        <SearchList
                          store={DocStore}
                          onClearSearch={this.onClearSearch}
                          onClickSearch={this.onClickSearch}
                          searchId={selectId}
                        />
                      ) : null
                    }
                    <div className="c7n-knowledge-content">
                      {selectId && docData
                        ? (
                          edit
                            ? (
                              <EditMode
                                docData={docData}
                                initialEditType={initialEditType}
                                onTitleChange={this.onTitleChange}
                                handleSave={this.handleSave}
                                handleDeleteDraft={this.handleDeleteDraft}
                                handleDocChange={this.handleDocChange}
                              />
                            )
                            : (
                              <ViewMode
                                versionVisible={versionVisible}
                                docData={docData}
                                spaceData={spaceData}
                                catalogVisible={catalogVisible}
                                onBackBtnClick={this.onBackBtnClick}
                                beforeQuitEdit={this.beforeQuitEdit}
                                handleBtnClick={this.handleBtnClick}
                                handleTitleChange={this.handleTitleChange}
                                searchVisible={searchVisible}
                              />
                            )
                        )
                        : (searchVisible
                          ? <DocSearchEmpty />
                          : <DocEmpty />
                        )
                      }
                    </div>
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
                  style={{
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
                  mode={docData.isOperate ? '' : 'share'}
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
          <DocModal store={DocStore} selectId={selectId} edit={edit} />
        </Content>
        <AttachmentRender />
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
