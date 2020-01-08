import React, { useContext, useEffect, useState, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Button, Icon, Dropdown, Spin, Input, Menu, Modal,
} from 'choerodon-ui';
import {
  Page, Header, Content, stores, Permission, Breadcrumb, Choerodon,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import DocDetail from '../../../components/DocDetail';
import DocEditor from './components/doc-editor';
import PageStore from '../stores';
import AttachmentRender from '../../../components/Extensions/attachment/AttachmentRender';
import { removeItemFromTree, addItemToTree } from '../../../components/WorkSpaceTree';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace from '../components/work-space';
import SearchList from '../../../components/SearchList';
import Catalog from '../../../components/Catalog';
import DocModal from './components/docModal';
import HomePage from './components/home-page';
import CreateDoc from './components/create-doc';
import CreateTemplate from './components/create-template';
import Template from './components/template';
import useFullScreen from './components/fullScreen/useFullScreen';
import './style/index.less';

const { Section, Divider } = ResizeContainer;
const { AppState, HeaderStore } = stores;
const { confirm } = Modal;
const { Fragment } = React;

function DocHome() {
  const { pageStore, history, id: proId, organizationId: orgId, type: levelType } = useContext(PageStore);
  const [loading, setLoading] = useState(false);
  const [docLoading, setDocLoading] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [logVisible, setLogVisible] = useState(false);
  const [creating, setCreating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [catalogTag, setCatalogTag] = useState(false);
  const [readOnly, setReadOnly] = useState(true);
  const { section } = pageStore;
  const workSpaceRef = useRef(null);
  const spaceCode = pageStore.getSpaceCode;
  const onFullScreenChange = (fullScreen) => {
    pageStore.setFullScreen(!!fullScreen);
    if (catalogTag) {
      pageStore.setCatalogVisible(true);
      setCatalogTag(false);
    } else {
      pageStore.setCatalogVisible(false);
    }
  };
  const [isFullScreen, toggleFullScreen] = useFullScreen(document.getElementsByClassName('c7n-kb-doc')[0], onFullScreenChange);
  const {
    getSpaceCode: code,
    getSearchVisible: searchVisible,
    getSelectId: selectId,
    getMode: mode,
    getFullScreen: fullScreen,
  } = pageStore;

  function getTypeCode() {
    return levelType === 'project' ? 'pro' : 'org';
  }

  /**
   * 将文档id加入url
   * @param spaceId
   */
  function changeUrl(spaceId) {
    const { origin, hash } = window.location;
    const { pathname } = history.location;
    const search = hash.split('?').length > 1 ? hash.split('?')[1] : '';
    const params = queryString.parse(search);
    params.spaceId = spaceId;
    const newParams = queryString.stringify(params);
    const newUrl = `${origin}#${pathname}?${newParams}`;
    window.history.pushState({}, 0, newUrl);
  }

  function handleCancel(spaceId) {
    setCreating(false);
    setSaving(false);
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    const newTree = removeItemFromTree(spaceData, {
      ...item,
      parentId: item.parentId || item.workSpaceParentId || 0,
    }, true);
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function checkPermission(type) {
    if (levelType === 'organization') {
      const orgData = HeaderStore.getOrgData;
      const orgObj = orgData.find(v => String(v.id) === String(orgId));
      if (!orgObj || (orgObj && !orgObj.into)) {
        setReadOnly(true);
      } else {
        setReadOnly(false);
      }
    } else {
      setReadOnly(getTypeCode() !== type);
    }
  }

  /**
   * 加载文档详情
   * @param spaceId 空间id
   * @param isCreate
   * @param searchText
   */
  function loadPage(spaceId = false, isCreate = false, searchText) {
    if (!isCreate && creating) {
      handleCancel('create');
      return;
    }
    setDocLoading(true);
    pageStore.setCatalogVisible(false);
    const id = spaceId; // getDefaultSpaceId();
    if (id) {
      // 更新url中文档ID
      changeUrl(id);
      pageStore.loadDoc(id, searchText).then((res) => {
        if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
          // 访问无权限文档或已被删除的文档
          if (searchVisible || searchText) {
            pageStore.setSelectId(id);
            setDocLoading(false);
            pageStore.setDoc(false);
            setReadOnly(true);
          } else {
            pageStore.setSelectId(id);
            loadPage();
          }
        } else {
          if (logVisible) {
            if (levelType === 'project' && !res.pageInfo.projectId) {
              // 项目查看组织文章，则关闭日志
              setLogVisible(false);
            } else {
              // 否则更新日志
              pageStore.loadLog(res.pageInfo.id);
            }
          }
          checkPermission(res.pageInfo.projectId ? 'pro' : 'org');
          pageStore.setSelectId(id);
          setDocLoading(false);
          pageStore.setMode(isCreate ? 'edit' : 'view');
          pageStore.setImportVisible(false);
          pageStore.setShareVisible(false);
        }
      }).catch(() => {
        setReadOnly(true);
        setDocLoading(false);
        pageStore.setImportVisible(false);
        pageStore.setShareVisible(false);
      });
    } else {
      // 没选文档时，显示主页
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      pageStore.setSelectId(false);
      checkPermission(getTypeCode());
      pageStore.queryRecentUpdate();
      setDocLoading(false);
    }
  }

  /**
   * 加载空间
   */
  function loadWorkSpace(spaceId) {
    let id = spaceId;
    if (!id) {
      const { hash } = window.location;
      const search = hash.split('?').length > 1 ? hash.split('?')[1] : '';
      const params = queryString.parse(search);
      id = params.spaceId && Number(params.spaceId);
    }
    if (id) {
      pageStore.setSelectId(id);
    }

    pageStore.loadWorkSpaceAll(id || selectId).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        // 如果id错误或不存在
        pageStore.loadWorkSpaceAll().then(() => {
          pageStore.setSelectId(false);
          setLoading(false);
          // loadPage();
        });
      } else {
        setLoading(false);
        loadPage(id || selectId);
      }
    }).catch((e) => {
      setLoading(false);
    });
  }
  useEffect(() => {
    // 加载数据
    // MenuStore.setCollapsed(true);
    loadWorkSpace();
  }, []);

  /**
   * 移动文档至回收站
   * @param {*} spaceId 
   * @param {*} role 
   */
  function deleteDoc(spaceId, role) {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    const request = role === 'admin' ? pageStore.adminDeleteDoc : pageStore.deleteDoc;
    request(spaceId).then(() => {
      // 更改
      const newTree = removeItemFromTree(spaceData, {
        ...item,
        parentId: item.parentId || item.workSpaceParentId || 0,
      }, true);
      pageStore.setWorkSpaceByCode(code, newTree);
      if (pageStore.getSelectId === item.id) {
        const newSelectId = item.parentId || item.workSpaceParentId || 0;
        pageStore.setSelectId(newSelectId);
        loadPage(newSelectId);
      }
      // setLoading(true);
      // pageStore.loadRecycleWorkSpaceAll().then((res) => {
      //   setLoading(false);
      // });
    }).catch((error) => {
      Choerodon.prompt(error);
    });
  }
  function handleDeleteDoc(id, title, role) {
    confirm({
      title: `删除文档"${title}"`,
      content: `文档"${title}"将会被移至回收站，您可以在回收站恢复此文档`,
      okText: '删除',
      cancelText: '取消',
      width: 520,
      onOk() {
        deleteDoc(id, role);
      },
      onCancel() { },
    });
  }


  function handleShare(id) {
    pageStore.queryShareMsg(id).then(() => {
      pageStore.setShareVisible(true);
    });
  }
  /**
   * 处理更多菜单点击事件
   * @param e
   */
  function handleMenuClick(e) {
    const { pageInfo, workSpace } = pageStore.getDoc;
    if (!pageInfo || !workSpace) {
      return;
    }
    const { id, title } = pageInfo;
    const { id: workSpaceId } = workSpace;
    const urlParams = AppState.currentMenuType;
    switch (e.key) {
      case 'delete':
        handleDeleteDoc(workSpaceId, title);
        break;
      case 'adminDelete':
        handleDeleteDoc(workSpaceId, title, 'admin');
        break;
      case 'version':
        history.push(`/knowledge/${urlParams.type}/version/${pageStore.baseId}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
        break;
      case 'export':
        Choerodon.prompt('正在导出，请稍候...');
        pageStore.exportPdf(id, title);
        break;
      case 'move':
        pageStore.setMoveVisible(true);
        break;
      case 'log':
        setLogVisible(true);
        break;
      default:
        break;
    }
  }

  /**
   * 获取更多操作菜单
   * @returns {XML}
   */
  function getMenus() {
    const docData = pageStore.getDoc;
    if (readOnly) {
      return (
        <Menu onClick={handleMenuClick}>
          <Menu.Item key="export">
            导出
          </Menu.Item>
        </Menu>
      );
    }
    return (
      <Menu onClick={handleMenuClick}>
        <Menu.Item key="export">
          导出
        </Menu.Item>
        <Menu.Item key="move">
          移动
        </Menu.Item>
        <Menu.Item key="log">
          操作历史
        </Menu.Item>
        <Menu.Item key="version">
          版本对比
        </Menu.Item>
        {AppState.userInfo.id === docData.createdBy
          ? (
            <Menu.Item key="delete">
              删除
            </Menu.Item>
          ) : (
            <Permission
              key="adminDelete"
              type={levelType}
              projectId={proId}
              organizationId={orgId}
              service={[`knowledgebase-service.work-space-${levelType}.delete`]}
            >
              <Menu.Item key="adminDelete">
                删除
              </Menu.Item>
            </Permission>
          )}

      </Menu>
    );
  }
  function handleCreateClick() {
    pageStore.setMode('view');
    CreateDoc({
      onCreate: async ({ title, template: templateId }) => {
        const workSpace = pageStore.getWorkSpace;
        const spaceData = workSpace[spaceCode].data;
        const currentCode = pageStore.getSpaceCode;
        let newTree = spaceData;
        const vo = {
          title: title.trim(),
          content: '',
          parentWorkspaceId: selectId || 0,
        };
        const data = templateId ? await pageStore.createWorkSpaceWithTemplate(vo, templateId) : await pageStore.createWorkSpace(vo);
        if (selectId) {
          if (currentCode !== spaceCode) {
            const newSpace = mutateTree(workSpace[currentCode].data, selectId, { isClick: false });
            pageStore.setWorkSpaceByCode(currentCode, newSpace);
          } else {
            newTree = mutateTree(spaceData, selectId, { isClick: false });
          }
        }
        newTree = addItemToTree(
          newTree,
          { ...data.workSpace, createdBy: data.createdBy, isClick: true },
          'create',
        );
        pageStore.setWorkSpaceByCode(spaceCode, newTree);
        loadPage(data.workSpace.id, 'create');
        setSaving(false);
        setCreating(false);
        setLoading(false);
      },
      pageStore,
    });
  }
  function handleCreateClickInTree(parent) {
    if (levelType === 'project') {
      pageStore.setSpaceCode('pro');
    }
    pageStore.setMode('view');
    // 新建时，创建项所在分组展开
    if (workSpaceRef && workSpaceRef.current) {
      const openKeys = workSpaceRef.current.openKeys || [];
      const openKey = getTypeCode();
      if (openKeys.indexOf(openKey) === -1) {
        openKeys.push(openKey);
      }
      workSpaceRef.current.handlePanelChange(openKeys);
    }
    if (saving) {
      return;
    }

    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode].data;
    if (!creating && spaceData) {
      setCreating(true);
      // 构建虚拟空间节点
      const item = {
        data: { title: 'create' },
        hasChildren: false,
        isExpanded: false,
        id: 'create',
        parentId: (parent && parent.id) || 0,
      };
      const newTree = addItemToTree(spaceData, item);
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
    }
  }
  /**
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  function handleSpaceSave(value, item) {
    setSaving(true);
    setLoading(true);
    const currentCode = pageStore.getSpaceCode;
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode].data;
    let newTree = spaceData;
    if (creating) {
      setCreating(false);
      newTree = removeItemFromTree(spaceData, {
        ...item,
        parentId: item.parentId || item.workSpaceParentId || 0,
      });
    }
    pageStore.setWorkSpaceByCode(spaceCode, newTree);
    if (!value || !value.trim() || saving) {
      return;
    }
    const vo = {
      title: value.trim(),
      content: '',
      parentWorkspaceId: item.parentId,
    };
    pageStore.createWorkSpace(vo).then((data) => {
      if (selectId) {
        if (currentCode !== spaceCode) {
          const newSpace = mutateTree(workSpace[currentCode].data, selectId, { isClick: false });
          pageStore.setWorkSpaceByCode(currentCode, newSpace);
        } else {
          newTree = mutateTree(spaceData, selectId, { isClick: false });
        }
      }
      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, createdBy: data.createdBy, isClick: true },
        'create',
      );
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
      loadPage(data.workSpace.id, 'create');
      setSaving(false);
      setCreating(false);
      setLoading(false);
    });
  }
  function handleTemplateCreateClick() {
    CreateTemplate({
      pageStore,
    });
  }
  function handleImportClick() {
    pageStore.setImportVisible(true);
  }

  function handleLogClick() {
    const { workSpace } = pageStore.getDoc;
    if (workSpace) {
      const { id: workSpaceId } = workSpace;
      handleShare(workSpaceId);
    }
  }

  function handleEditClick() {
    pageStore.setCatalogVisible(false);
    pageStore.setMode('edit');
  }


  function handleSearch() {
    if (searchValue) {
      pageStore.querySearchList(searchValue).then((res) => {
        pageStore.setSearchVisible(true);
        const searchList = pageStore.getSearchList;
        if (searchList && searchList.length) {
          loadPage(searchList[0].workSpaceId, false, searchValue);
        } else {
          pageStore.setDoc(false);
        }
      });
    } else {
      pageStore.setSearchVisible(false);
    }
  }

  function handleSearchChange(e) {
    setSearchValue(e.target.value);
  }

  function handleClearSearch() {
    pageStore.setSearchVisible(false);
    setSearchValue('');
    loadWorkSpace();
  }

  function handleLoadDraft() {
    const docData = pageStore.getDoc;
    const { pageInfo: { id } } = docData;
    pageStore.loadDraftDoc(id).then(() => {
      pageStore.setCatalogVisible(false);
      pageStore.setMode('edit');
    });
  }

  function handleDeleteDraft() {
    const docData = pageStore.getDoc;
    const hasDraft = pageStore.getDraftVisible;
    const { pageInfo: { id } } = docData;
    if (hasDraft) {
      pageStore.deleteDraftDoc(id).then(() => {
        loadPage(selectId);
      });
    }
  }

  function toggleFullScreenEdit() {
    const { catalogVisible } = pageStore;
    if (catalogVisible) {
      pageStore.setCatalogVisible(false);
      setCatalogTag(true);
    }
    toggleFullScreen();
    pageStore.setFullScreen(!isFullScreen);
  }
  const disabled = getTypeCode() === 'pro' ? ['share', 'org'].includes(spaceCode) : false;
  return (
    <Page
      className="c7n-kb-doc"
      service={[
        // 项目层
        'knowledgebase-service.work-space-project.queryAllSpaceByOptions',
        'knowledgebase-service.work-space-project.createWorkSpaceAndPage',
        'knowledgebase-service.work-space-project.queryAllTreeList',
        'knowledgebase-service.work-space-project.deleteWorkSpaceAndPage',
        'knowledgebase-service.work-space-project.querySpaceByIds',
        'knowledgebase-service.work-space-project.recentUpdateList',
        'knowledgebase-service.work-space-project.recycleWorkspaceTree',
        'knowledgebase-service.work-space-project.removeWorkSpaceAndPage',
        'knowledgebase-service.work-space-project.removeWorkSpaceAndPageByMyWorkSpace',
        'knowledgebase-service.work-space-project.restoreWorkSpaceAndPage',
        'knowledgebase-service.work-space-project.moveWorkSpace',
        'knowledgebase-service.work-space-project.queryWorkSpaceInfo',
        'knowledgebase-service.work-space-project.updateWorkSpaceAndPage',
        'knowledgebase-service.page-comment-project.create',
        'knowledgebase-service.page-comment-project.deleteMyComment',
        'knowledgebase-service.page-comment-project.queryByPageId',
        'knowledgebase-service.page-comment-project.update',
        'knowledgebase-service.page-comment-project.deleteComment',
        'knowledgebase-service.page-project.createPageByImport',
        'knowledgebase-service.page-project.autoSavePage',
        'knowledgebase-service.page-project.deleteDraftContent',
        'knowledgebase-service.page-project.queryDraftPage',
        'knowledgebase-service.page-project.exportMd2Pdf',
        'knowledgebase-service.page-project.fullTextSearch',
        'knowledgebase-service.page-project.importDocx2Md',
        'knowledgebase-service.page-attachment-project.create',
        'knowledgebase-service.page-attachment-project.batchDelete',
        'knowledgebase-service.page-attachment-project.queryByList',
        'knowledgebase-service.page-attachment-project.queryByFileName',
        'knowledgebase-service.page-attachment-project.uploadForAddress',
        'knowledgebase-service.page-attachment-project.delete',
        'knowledgebase-service.page-version-project.compareVersion',
        'knowledgebase-service.page-version-project.listQuery',
        'knowledgebase-service.page-version-project.rollbackVersion',
        'knowledgebase-service.page-version-project.queryById',
        'knowledgebase-service.user-setting-project.createOrUpdate',
        'knowledgebase-service.work-space-share-project.queryShare',
        'knowledgebase-service.work-space-share-project.updateShare',
        'knowledgebase-service.page-log-project.listByPageId',
        // 组织层
        'knowledgebase-service.work-space-organization.queryAllSpaceByOptions',
        'knowledgebase-service.work-space-organization.createWorkSpaceAndPage',
        'knowledgebase-service.work-space-organization.queryAllTreeList',
        'knowledgebase-service.work-space-organization.deleteWorkSpaceAndPage',
        'knowledgebase-service.work-space-organization.querySpaceByIds',
        'knowledgebase-service.work-space-organization.recentUpdateList',
        'knowledgebase-service.work-space-organization.recycleWorkspaceTree',
        'knowledgebase-service.work-space-organization.removeWorkSpaceAndPage',
        'knowledgebase-service.work-space-organization.removeWorkSpaceAndPageByMyWorkSpace',
        'knowledgebase-service.work-space-organization.restoreWorkSpaceAndPage',
        'knowledgebase-service.work-space-organization.moveWorkSpace',
        'knowledgebase-service.work-space-organization.queryWorkSpaceInfo',
        'knowledgebase-service.work-space-organization.updateWorkSpaceAndPage',
        'knowledgebase-service.page-comment-organization.create',
        'knowledgebase-service.page-comment-organization.deleteMyComment',
        'knowledgebase-service.page-comment-organization.queryByPageId',
        'knowledgebase-service.page-comment-organization.update',
        'knowledgebase-service.page-comment-organization.deleteComment',
        'knowledgebase-service.page-organization.createPageByImport',
        'knowledgebase-service.page-organization.autoSavePage',
        'knowledgebase-service.page-organization.deleteDraftContent',
        'knowledgebase-service.page-organization.queryDraftPage',
        'knowledgebase-service.page-organization.exportMd2Pdf',
        'knowledgebase-service.page-organization.fullTextSearch',
        'knowledgebase-service.page-organization.importDocx2Md',
        'knowledgebase-service.page-attachment-organization.create',
        'knowledgebase-service.page-attachment-organization.batchDelete',
        'knowledgebase-service.page-attachment-organization.queryByList',
        'knowledgebase-service.page-attachment-organization.queryByFileName',
        'knowledgebase-service.page-attachment-organization.uploadForAddress',
        'knowledgebase-service.page-attachment-organization.delete',
        'knowledgebase-service.page-version-organization.compareVersion',
        'knowledgebase-service.page-version-organization.listQuery',
        'knowledgebase-service.page-version-organization.rollbackVersion',
        'knowledgebase-service.page-version-organization.queryById',
        'knowledgebase-service.user-setting-organization.createOrUpdate',
        'knowledgebase-service.work-space-share-organization.queryShare',
        'knowledgebase-service.work-space-share-organization.updateShare',
        'knowledgebase-service.page-log-organization.listByPageId',
      ]}
    >
      {!fullScreen && (
        <Header>
          {section !== 'template'
            ? (
              <span style={{ display: 'flex', justifyContent: 'space-between', width: 'calc(100% - 20px)' }}>
                <span>
                  <Button
                    funcType="flat"
                    onClick={handleCreateClick}
                    disabled={disabled || readOnly}
                  >
                    <Icon type="playlist_add icon" />
                    <FormattedMessage id="create" />
                  </Button>
                  <Button
                    funcType="flat"
                    onClick={handleImportClick}
                    disabled={disabled || readOnly}
                  >
                    <Icon type="archive icon" />
                    <FormattedMessage id="import" />
                  </Button>
                  <div
                    style={{
                      height: '60%',
                      width: 1,
                      margin: '0 20px',
                      border: '.001rem solid rgb(0, 0, 0, 0.12)',
                      display: 'inline-block',
                      verticalAlign: 'middle',
                    }}
                  />
                  {section === 'tree' && selectId && (
                    <Fragment>
                      <Button
                        funcType="flat"
                        onClick={handleEditClick}
                        disabled={readOnly}
                      >
                        <Icon type="mode_edit icon" />
                        <FormattedMessage id="edit" />
                      </Button>
                      <Button
                        funcType="flat"
                        onClick={handleLogClick}
                        disabled={readOnly}
                      >
                        <Icon type="share icon" />
                        <FormattedMessage id="share" />
                      </Button>
                      <Dropdown overlay={getMenus()} trigger={['click']}>
                        <i className="icon icon-more_vert" style={{ margin: '0 20px', color: '#3f51b5', cursor: 'pointer', verticalAlign: 'text-bottom' }} />
                      </Dropdown>
                    </Fragment>
                  )}
                  <Button onClick={toggleFullScreenEdit}>
                    <Icon type="fullscreen" />
                    <FormattedMessage id="fullScreen" />
                  </Button>
                </span>
                <span className="c7n-kb-doc-search">
                  <Input
                    className="hidden-label"
                    placeholder="搜索"
                    value={searchValue}
                    onPressEnter={handleSearch}
                    onChange={handleSearchChange}
                    prefix={(
                      <Icon
                        type="search"
                        className="c7n-kb-doc-search-icon"
                        onClick={handleSearch}
                      />
                    )}
                  />
                </span>
              </span>
            ) : (
              <Button
                funcType="flat"
                icon="playlist_add"
                onClick={handleTemplateCreateClick}
              >
                创建模板
              </Button>
            )}
        </Header>
      )}
      {!fullScreen
        ? (
          <Content style={{ padding: 0, height: '100%' }}>
            <Breadcrumb />
            <div style={{ height: 'calc( 100% - 65px )' }}>
              <Spin spinning={loading}>
                <ResizeContainer type="horizontal">
                  {searchVisible
                    ? (
                      <SearchList
                        searchText={searchValue}
                        store={pageStore}
                        onClearSearch={handleClearSearch}
                        onClickSearch={loadPage}
                        searchId={selectId}
                      />
                    ) : null}
                  {!searchVisible
                    ? (
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
                        <div className="c7n-kb-doc-left">
                          <WorkSpace
                            readOnly={disabled}
                            forwardedRef={workSpaceRef}
                            onClick={loadPage}
                            onSave={handleSpaceSave}
                            onDelete={handleDeleteDoc}
                            onCreate={handleCreateClickInTree}
                            onCancel={handleCancel}
                          />
                        </div>
                      </Section>
                    ) : null}
                  {!searchVisible
                    ? (
                      <Divider />
                    ) : null}
                  <Section
                    style={{ flex: 1 }}
                    size={{
                      width: 'auto',
                    }}
                  >
                    <Spin spinning={docLoading}>
                      <div className="c7n-kb-doc-doc">
                        <div className="c7n-kb-doc-content">
                          {section === 'recent' && <HomePage pageStore={pageStore} onClick={loadWorkSpace} />}
                          {section === 'tree' && <DocEditor readOnly={readOnly} loadWorkSpace={loadWorkSpace} searchText={searchValue} />}
                          {section === 'template' && <Template />}
                        </div>
                      </div>
                    </Spin>
                  </Section>
                  {pageStore.catalogVisible
                    ? (
                      <Divider />
                    ) : null}
                  {pageStore.catalogVisible
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
                        <Catalog store={pageStore} />
                      </Section>
                    ) : null}
                </ResizeContainer>
              </Spin>
            </div>
          </Content>
        ) : (
          <Content style={{ padding: 0, height: '100%' }}>
            <Spin spinning={loading}>
              <ResizeContainer type="horizontal" style={{ borderTop: '1px solid #d3d3d3' }}>
                <Section
                  style={{ flex: 1 }}
                  size={{
                    width: 'auto',
                  }}
                >
                  <Spin spinning={docLoading}>
                    <div className="c7n-kb-doc-doc">
                      <div className="c7n-kb-doc-content">
                        {selectId
                          ? (
                            <DocEditor
                              readOnly={readOnly}
                              fullScreen
                              loadWorkSpace={loadWorkSpace}
                              exitFullScreen={toggleFullScreenEdit}
                              editDoc={handleEditClick}
                              searchText={searchValue}
                            />
                          ) : <HomePage pageStore={pageStore} onClick={loadWorkSpace} />}
                      </div>
                    </div>
                  </Spin>
                </Section>
                {pageStore.catalogVisible
                  ? (
                    <Divider />
                  ) : null}
                {pageStore.catalogVisible
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
                      <Catalog store={pageStore} />
                    </Section>
                  ) : null}
              </ResizeContainer>
            </Spin>
          </Content>
        )}
      {logVisible
        ? (
          <DocDetail onCollapse={() => setLogVisible(false)} store={pageStore} />
        ) : null}
      <Permission
        key="adminDelete"
        type={levelType}
        projectId={proId}
        organizationId={orgId}
        service={[`knowledgebase-service.work-space-${levelType}.delete`]}
      />
      <AttachmentRender />
      <DocModal
        store={pageStore}
        selectId={selectId}
        mode={mode}
        refresh={loadWorkSpace}
        handleDeleteDraft={handleDeleteDraft}
        handleLoadDraft={handleLoadDraft}
      />
    </Page>
  );
}

export default withRouter(injectIntl(observer(DocHome)));
