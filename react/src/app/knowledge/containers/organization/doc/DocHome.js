import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon, Modal, Spin, Input, Collapse, Checkbox,
} from 'choerodon-ui';
import {
  Page, Header, Content, axios, stores, Permission,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import copy from 'copy-to-clipboard';
import { mutateTree } from '@atlaskit/tree';
import DocEmpty from '../../../components/DocEmpty';
import DocEditor from '../../../components/DocEditor';
import DocViewer from '../../../components/DocViewer';
import DocCatalog from '../../../components/DocCatalog';
import DocDetail from '../../../components/DocDetail';
import DocVersion from '../../../components/DocVersion';
import DocMove from '../../../components/DocMove';
import DocStore from '../../../stores/organization/doc/DocStore';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpace';
import './DocHome.scss';

const { confirm } = Modal;
const { Panel } = Collapse;
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
      moveVisible: false,
      selectId: false,
      selectProId: false,
      loading: true,
      docLoading: true,
      currentNav: 'attachment',
      newTitle: false,
      saving: false,
      hasChange: false, // 文档是否修改
      creating: false,
      path: false,
      migrationVisible: false,
      shareVisible: false,
      importVisible: false,
      uploading: false,
    };
    this.newDocLoop = false;
  }

  componentDidMount() {
    // 由于页面公用，设置项目/组织信息
    this.initCurrentMenuType();
    // 收起菜单
    MenuStore.setCollapsed(true);
    // 加载数据
    this.refresh();
  }

  componentWillUnmount() {
    clearInterval(this.newDocLoop);
  }

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
      migrationVisible: false,
      selectId: id || selectId,
    });
    DocStore.loadWorkSpaceAll(id || selectId).then((res) => {
      if (res && res.failed && res.code === 'error.workspace.illegal') {
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

  initSelect =() => {
    const { selectId } = this.state;
    const spaceData = DocStore.getWorkSpace;
    if (!selectId) {
      // 默认选中第一篇文档
      if (spaceData.items && spaceData.items['0'] && spaceData.items['0'].children.length) {
        const currentSelectId = spaceData.items['0'].children[0];
        // 加载第一篇文档
        DocStore.loadDoc(currentSelectId);
        // 选中第一篇文档菜单
        const newTree = mutateTree(spaceData, currentSelectId, { isClick: true });
        DocStore.setWorkSpace(newTree);
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
        if (res && res.failed && res.code === 'error.workspace.illegal') {
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
            migrationVisible: false,
            shareVisible: false,
            importVisible: false,
          });
        }
      }).catch(() => {
        this.setState({
          loading: false,
          docLoading: false,
          edit: false,
          versionVisible: false,
          sideBarVisible: false,
          catalogVisible: false,
          migrationVisible: false,
          shareVisible: false,
          importVisible: false,
        });
      });
    }
  };

  handleSave = (md, type, editMode) => {
    const { newTitle } = this.state;
    const docData = DocStore.getDoc;
    if (type === 'autoSave') {
      const doc = {
        content: md,
      };
      DocStore.autoSaveDoc(docData.workSpace.id, doc);
    } else {
      const doc = {
        title: (newTitle && newTitle.trim()) || docData.pageInfo.title,
        content: md,
        minorEdit: type === 'edit',
        objectVersionNumber: docData.objectVersionNumber,
      };
      // 修改默认编辑模式
      if (editMode) {
        const mode = {
          editMode,
          type: 'edit_mode',
        };
        if (docData.userSettingDTO) {
          mode.id = docData.userSettingDTO.id;
          mode.objectVersionNumber = docData.userSettingDTO.objectVersionNumber;
        }
        DocStore.editDefaultMode(mode).then(() => {
          DocStore.editDoc(docData.workSpace.id, doc);
        });
      } else {
        DocStore.editDoc(docData.workSpace.id, doc);
      }
      // 点击保存，退出编辑模式
      if (type === 'save') {
        this.setState({
          edit: false,
        });
      }
      // 重置title
      this.setState({
        newTitle: false,
      });
    }
  };

  handleCancel = () => {
    this.handleRefresh();
    this.setState({
      edit: false,
      hasChange: false,
      migrationVisible: false,
      path: false,
      shareVisible: false,
      importVisible: false,
      moveVisible: false,
    });
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
    if (selectId && String(selectId) !== String(id) && newTree.items[selectId]) {
      newTree = mutateTree(newTree, selectId, { isClick: false });
    }
    this.handleSpaceClick(newTree, id);
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
   * @param data
   * @param selectId
   * @param mode 当创建时调用为create,自动进入编辑模式
   */
  handleSpaceClick = (data, selectId, mode) => {
    const { selectProId, selectId: lastSelectId } = this.state;
    if (String(lastSelectId) !== String(selectId)) {
      this.changeUrl(selectId);
      DocStore.setWorkSpace(data);
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
        edit: mode === 'create', // 创建后，默认编辑模式
        selectProId: false,
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
      catalogVisible: false,
    });
    // 加载详情
    DocStore.loadProDoc(selectId, proId).then(() => {
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
  };

  handleSpaceExpand = (data, itemId) => {
    DocStore.setWorkSpace(data);
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
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  handleSpaceSave = (value, item) => {
    const { selectId, selectProId, saving } = this.state;
    if (!value || !value.trim() || saving) {
      return;
    }
    this.setState({
      saving: true,
      creating: false,
    });
    let newTree = DocStore.getWorkSpace;
    const dto = {
      title: value.trim(),
      content: '',
      parentWorkspaceId: item.parentId,
    };
    DocStore.createWorkSpace(dto).then((data) => {
      if (!selectProId && selectId) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, createdBy: data.createdBy, isClick: true },
        'create',
      );
      this.handleSpaceClick(newTree, data.workSpace.id, 'create');
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
    const { hasEverExpand, creating } = this.state;
    if (!creating) {
      this.setState({
        creating: true,
      });
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
    }
  };

  handleDeleteWorkSpace = (data, mode) => {
    this.handleDeleteDoc(data.id, mode);
  };

  handleNewDoc = (mode) => {
    // const winObj = window.open('https://www.baidu.com', '', 'width=600,height=251,location=no,menubar=no,resizable=0,top=200px,left=400px');
    const urlParams = AppState.currentMenuType;
    if (mode !== 'import') {
      localStorage.removeItem('importDoc');
      localStorage.removeItem('importDocTitle');
    }
    const winObj = window.open(`/#/knowledge/organizations/create?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}`, '');
    this.newDocLoop = setInterval(() => {
      if (winObj && winObj.closed) {
        this.setState({
          selectId: winObj.newDocId,
        }, () => {
          this.refresh();
        });
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
    const { selectId, sideBarVisible } = this.state;
    this.setState({
      docLoading: true,
      hasChange: false,
      catalogVisible: false,
    });
    if (selectId) {
      DocStore.loadWorkSpaceAll(selectId);
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
    } else {
      this.refresh();
    }
  };

  handleBtnClick = (type) => {
    const docData = DocStore.getDoc;
    const { id, title } = docData.pageInfo;
    const { id: workSpaceId } = docData.workSpace;
    const { selectId, catalogVisible } = this.state;
    switch (type) {
      case 'delete':
        this.handleDeleteDoc(selectId);
        break;
      case 'adminDelete':
        this.handleDeleteDoc(selectId, 'admin');
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
        this.setState({
          moveVisible: true,
        });
        break;
      default:
        break;
    }
  };

  shareDoc = (id) => {
    DocStore.queryShareMsg(id).then(() => {
      this.setState({
        shareVisible: true,
      });
    });
  };

  handleDeleteDraft = () => {
    const docData = DocStore.getDoc;
    const { selectId } = this.state;
    if (docData && docData.hasDraft) {
      DocStore.deleteDraftDoc(selectId).then(() => {
        this.handleCancel();
      });
    } else {
      this.handleCancel();
    }
  };

  handleLoadDraft = () => {
    const { selectId } = this.state;
    DocStore.loadDraftDoc(selectId).then(() => {
      this.setState({
        edit: true,
        catalogVisible: false,
      });
    });
  };

  handleDeleteDoc = (selectId, mode) => {
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
            const newTree = removeItemFromTree(spaceData, {
              ...item,
              parentId: item.parentId || item.workSpaceParentId || 0,
            });
            DocStore.setWorkSpace(newTree);
            that.setState({
              selectId: item.parentId || item.workSpaceParentId || 0,
            }, () => that.refresh());
          }).catch((error) => {
            Choerodon.prompt('网络错误，请重试。');
          });
        } else {
          DocStore.deleteDoc(selectId).then(() => {
            const newTree = removeItemFromTree(spaceData, {
              ...item,
              parentId: item.parentId || item.workSpaceParentId || 0,
            });
            DocStore.setWorkSpace(newTree);
            that.setState({
              selectId: item.parentId || item.workSpaceParentId || 0,
            }, () => that.refresh());
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

  handleMigration = () => {
    this.setState({
      migrationVisible: true,
    });
  };

  handleImport = () => {
    this.setState({
      importVisible: true,
    });
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

  handleCopy = () => {
    const shareInput = document.getElementById('shareUrl');
    if (shareInput && shareInput.value) {
      copy(shareInput.value);
      Choerodon.prompt('复制成功！');
    }
  };

  /**
   * 空间上创建子空间
   * @param mode
   */
  handleCheckChange = (mode) => {
    const share = DocStore.getShare;
    const { type: shareType, workspaceId, objectVersionNumber, id } = share || {};
    let newType = 'disabled';
    if (mode === 'share') {
      newType = shareType === 'disabled' ? 'current_page' : 'disabled';
    } else {
      newType = shareType === 'current_page' ? 'include_page' : 'current_page';
    }
    DocStore.setShare({
      ...share,
      type: newType,
    });
    DocStore.updateShare(id, workspaceId, {
      objectVersionNumber,
      type: newType,
    });
  };

  importWord = () => {
    this.uploadInput.click();
  };

  beforeUpload = (e) => {
    if (e.target.files[0]) {
      this.upload(e.target.files[0]);
    }
  };

  upload = (file) => {
    if (!file) {
      Choerodon.prompt('请选择文件');
      return;
    }
    if (file.size > 1024 * 1024 * 10) {
      Choerodon.prompt('文件不能超过10M');
      return false;
    }
    const formData = new FormData();
    formData.append('file', file);
    this.setState({
      uploading: true,
      // fileName: file.name,
    });
    DocStore.importWord(formData).then((res) => {
      localStorage.setItem('importDoc', res);
      if (file.name) {
        const nameList = file.name.split('.');
        nameList.pop();
        localStorage.setItem('importDocTitle', nameList.join());
      }
      this.setState({
        uploading: false,
        importVisible: false,
      });
      this.handleNewDoc('import');
    }).catch((e) => {
      this.setState({
        uploading: false,
      });
      Choerodon.prompt('网络错误');
    });
  };

  render() {
    const {
      edit, selectId, catalogVisible, docLoading, uploading,
      sideBarVisible, loading, currentNav, selectProId, moveVisible,
      versionVisible, migrationVisible, shareVisible, importVisible,
    } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;
    const draftVisible = docData.hasDraft;
    const { type, name, id: projectId, organizationId: orgId } = AppState.currentMenuType;
    const proWorkSpace = DocStore.getProWorkSpace;
    const proList = DocStore.getProList;
    const share = DocStore.getShare;
    const { type: shareType, token } = share || {};
    const initialEditType = docData.userSettingDTO ? docData.userSettingDTO.editMode : 'markdown';

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
                <Button
                  funcType="flat"
                  onClick={() => this.beforeQuitEdit('handleImport')}
                >
                  <Icon type="archive icon" />
                  <FormattedMessage id="import" />
                </Button>
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={orgId}
                  service={[`knowledgebase-service.wiki-migration.${type}LevelMigration`]}
                >
                  <Button
                    funcType="flat"
                    onClick={this.handleMigration}
                  >
                    <Icon type="auto_deploy icon" />
                    {'WIKI迁移'}
                  </Button>
                </Permission>
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
                          onSave={this.handleSpaceSave}
                          onCancel={this.handleSpaceCancel}
                          onCreate={this.handleCreateWorkSpace}
                          onDelete={this.handleDeleteWorkSpace}
                          onShare={this.shareDoc}
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
                      onSave={this.handleSpaceSave}
                      onCancel={this.handleSpaceCancel}
                      onCreate={this.handleCreateWorkSpace}
                      onDelete={this.handleDeleteWorkSpace}
                      onShare={this.shareDoc}
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
                              initialEditType={initialEditType}
                              onSave={this.handleSave}
                              onCancel={this.handleDeleteDraft}
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
                              loginUserId={AppState.userInfo.id}
                              onTitleEdit={this.handleTitleChange}
                              store={DocStore}
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
          {migrationVisible
            ? (
              <Modal
                title="wiki文档迁移"
                visible={migrationVisible}
                closable={false}
                onOk={this.migration}
                onCancel={this.handleCancel}
                okText="迁移"
                maskClosable={false}
              >
                <div style={{ padding: '20px 0' }}>
                  {'你可以将wiki中的文档迁移到知识管理中，如果你之前修改过项目名称，请在下方填写wiki中的文档路径。如路径为“/O-Choerodon/P-Choerodon敏捷管理/”，请填写“O-Choerodon.P-Choerodon敏捷管理”。'}
                  <Input
                    label="文档路径"
                    onChange={this.handlePathChange}
                    placeholder="O-Choerodon"
                  />
                </div>
              </Modal>
            ) : null
          }
          {shareVisible
            ? (
              <Modal
                title="分享链接"
                visible={shareVisible}
                closable={false}
                onOk={this.migration}
                onCancel={this.handleCancel}
                footer={<Button onClick={this.handleCancel} funcType="flat">取消</Button>}
                maskClosable={false}
              >
                <div style={{ padding: '20px 0' }}>
                  <FormattedMessage id="doc.share.tip" />
                  <Checkbox disabled={shareType === 'disabled'} checked={shareType === 'include_page'} onChange={() => this.handleCheckChange('type')} className="c7n-knowledge-checkBox">
                    <FormattedMessage id="doc.share.include" />
                  </Checkbox>
                  <div className="c7n-knowledge-input">
                    <Input
                      id="shareUrl"
                      label="分享链接"
                      disabled
                      value={`${window.location.origin}/#/knowledge/share/${token}`}
                    />
                    <Button onClick={this.handleCopy} type="primary" funcType="raised">
                      <FormattedMessage id="doc.share.copy" />
                    </Button>
                  </div>
                </div>
              </Modal>
            ) : null
          }
          {importVisible
            ? (
              <Modal
                title="Word文档导入"
                visible={importVisible}
                closable={false}
                onOk={this.handleCancel}
                onCancel={this.handleCancel}
                footer={<Button onClick={this.handleCancel} funcType="flat">取消</Button>}
                maskClosable={false}
              >
                <div style={{ padding: '20px 0' }}>
                  <FormattedMessage id="doc.import.tip" />
                  <div style={{ marginTop: 10 }}>
                    <Button
                      loading={uploading}
                      type="primary"
                      funcType="flat"
                      onClick={() => this.importWord()}
                      style={{ marginBottom: 2 }}
                    >
                      <Icon type="archive icon" />
                      <span>导入文档</span>
                    </Button>
                    <input
                      ref={
                        (uploadInput) => { this.uploadInput = uploadInput; }
                      }
                      type="file"
                      onChange={this.beforeUpload}
                      style={{ display: 'none' }}
                      accept=".docx"
                    />
                  </div>
                </div>
              </Modal>
            ) : null
          }
          {moveVisible
            ? (
              <DocMove
                store={DocStore}
                moveVisible={moveVisible}
                id={selectId}
                closeDocMove={this.closeDocMove}
              />
            ) : null
          }
          {draftVisible
            ? (
              <Modal
                title="提示"
                visible={draftVisible}
                closable={false}
                onOk={this.handleLoadDraft}
                onCancel={this.handleDeleteDraft}
                maskClosable={false}
                cancelText="删除草稿"
              >
                当前文档存在上次编辑未被保存的草稿，点击确认进行查看，点击删除草稿将会删除未保存的修改。
              </Modal>
            ) : null
          }
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
