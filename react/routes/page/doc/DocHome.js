import React, { Component, useContext, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Button, Icon, Dropdown, Spin, Input, Divider as C7NDivider, Menu, Modal,
} from 'choerodon-ui';
import {
  Page, Header, Content, stores, Permission, Breadcrumb,
} from '@choerodon/master';
import { withRouter } from 'react-router-dom';
import CooperateSide from '@choerodon/buzz/lib/routes/cooperate-side';
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
import useFullScreen from './components/fullScreen/useFullScreen';
import './style/index.less';

const { Section, Divider } = ResizeContainer;
const { AppState, MenuStore } = stores;
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
  const [buzzVisible, setBuzzVisible] = useState(false);
  const [defaultOpenId, setDefaultOpenId] = useState(false);
  const onFullScreenChange = (fullScreen) => {
    pageStore.setFullScreen(!!fullScreen);
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

  const readOnly = getTypeCode() !== code;

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

  /**
   * 默认选中空间并返回id
   * @returns id
   * 注意: 此函数会更改空间数据
   */
  function getDefaultSpaceId() {
    const workSpace = pageStore.getWorkSpace;
    let spaceId = false;
    let spaceCode = false;
    Object.keys(workSpace).forEach((key) => {
      if (!spaceId) {
        const list = workSpace[key] && workSpace[key].data.items[0].children;
        if (list && list.length) {
          [spaceId] = list;
          spaceCode = key;
        }
      }
    });
    if (spaceId) {
      const newTree = mutateTree(workSpace[spaceCode].data, spaceId, { isClick: true });
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
      pageStore.setSpaceCode(spaceCode);
      pageStore.setSelectId(spaceId);
      return spaceId;
    } else {
      return false;
    }
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
    });
    pageStore.setWorkSpaceByCode(code, newTree);
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
    const id = spaceId || getDefaultSpaceId();
    if (id) {
      changeUrl(id);
      pageStore.loadDoc(id, searchText).then((res) => {
        if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
          if (searchVisible || searchText) {
            pageStore.setSelectId(id);
            setDocLoading(false);
            pageStore.setDoc(false);
          } else {
            pageStore.setSelectId(id);
            loadPage();
          }
        } else {
          pageStore.setSelectId(id);
          pageStore.loadLog(id);
          setDocLoading(false);
          pageStore.setMode(isCreate ? 'edit' : 'view');
          pageStore.setImportVisible(false);
          pageStore.setShareVisible(false);
        }
      }).catch(() => {
        setDocLoading(false);
        pageStore.setImportVisible(false);
        pageStore.setShareVisible(false);
      });
    } else {
      setDocLoading(false);
    }
  }

  /**
   * 加载空间
   */
  function loadWorkSpace(spaceId) {
    let id = spaceId;
    if (!id) {
      const params = queryString.parse(window.location.hash);
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
          loadPage();
        });
      } else {
        setLoading(false);
        loadPage(id || selectId);
      }
    }).catch((e) => {
      setLoading(false);
    });
  }

  function openCooperate() {
    const { hash } = window.location;
    const search = hash.split('?').length > 1 ? hash.split('?')[1] : '';
    const params = queryString.parse(search);
    if (params.openCooperate) {
      setBuzzVisible(true);
      if (params.defaultOpenId) {
        setDefaultOpenId(params.defaultOpenId);
        delete params.defaultOpenId;
      }
      delete params.openCooperate;
      const { origin } = window.location;
      const { pathname } = history.location;
      const newParams = queryString.stringify(params);
      const newUrl = `${origin}#${pathname}?${newParams}`;
      window.history.pushState({}, 0, newUrl);
    }
  }

  useEffect(() => {
    // 加载数据
    // MenuStore.setCollapsed(true);
    openCooperate();
    loadWorkSpace();
  }, []);

  function deleteDoc(spaceId, role) {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    if (role === 'admin') {
      pageStore.adminDeleteDoc(spaceId).then(() => {
        const newTree = removeItemFromTree(spaceData, {
          ...item,
          parentId: item.parentId || item.workSpaceParentId || 0,
        });
        pageStore.setWorkSpaceByCode(code, newTree);
        const newSelectId = item.parentId || item.workSpaceParentId || 0;
        pageStore.setSelectId(newSelectId);
        loadWorkSpace(newSelectId);
      }).catch((error) => {
      });
    } else {
      pageStore.deleteDoc(spaceId).then(() => {
        const newTree = removeItemFromTree(spaceData, {
          ...item,
          parentId: item.parentId || item.workSpaceParentId || 0,
        });
        pageStore.setWorkSpaceByCode(code, newTree);
        const newSelectId = item.parentId || item.workSpaceParentId || 0;
        pageStore.setSelectId(newSelectId);
        loadWorkSpace(newSelectId);
      }).catch((error) => {
      });
    }
  }

  function handleDeleteDoc(id, title, role) {
    confirm({
      title: `删除文档"${title}"`,
      content: `如果文档下面有子级，也会被同时删除，确定要删除文档"${title}"吗?`,
      okText: '删除',
      cancelText: '取消',
      width: 520,
      onOk() {
        deleteDoc(id, role);
      },
      onCancel() {},
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
    const { pageInfo: { id, title }, workSpace: { id: workSpaceId } } = pageStore.getDoc;
    const urlParams = AppState.currentMenuType;
    switch (e.key) {
      case 'delete':
        handleDeleteDoc(workSpaceId, title);
        break;
      case 'adminDelete':
        handleDeleteDoc(workSpaceId, title, 'admin');
        break;
      case 'log':
        setLogVisible(true);
        break;
      case 'version':
        history.push(`/knowledge/${urlParams.type}/version?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
        break;
      case 'export':
        Choerodon.prompt('正在导出，请稍候...');
        pageStore.exportPdf(id, title);
        break;
      case 'share':
        handleShare(workSpaceId);
        break;
      case 'move':
        pageStore.setMoveVisible(true);
        break;
      case 'import':
        pageStore.setImportVisible(true);
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
        <Menu.Item key="share">
          分享
        </Menu.Item>
        <Menu.Item key="export">
          导出
        </Menu.Item>
        <Menu.Item key="import">
          导入
        </Menu.Item>
        <Menu.Item key="move">
          移动
        </Menu.Item>
        <Menu.Item key="version">
          版本对比
        </Menu.Item>
        <Menu.Item key="log">
          活动日志
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

  function handleCreateClick(parent) {
    if (saving) {
      return;
    }
    const spaceCode = levelType === 'project' ? 'pro' : 'org';
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

  function handleEditClick() {
    pageStore.setCatalogVisible(false);
    pageStore.setMode('edit');
  }

  /**
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  function handleSpaceSave(value, item) {
    setSaving(true);
    const spaceCode = levelType === 'project' ? 'pro' : 'org';
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
    });
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

  function fullScreenEdit() {
    toggleFullScreen();
    pageStore.setFullScreen(true);
  }

  function handleBuzzClick() {
    setLogVisible(false);
    setBuzzVisible(!buzzVisible);
    setDefaultOpenId(false);
  }

  return (
    <Page
      className="c7n-kb-doc"
    >
      {!fullScreen
        ? (
          <Header>
            <span style={{ display: 'flex', justifyContent: 'space-between', width: 'calc(100% - 20px)' }}>
              <span>
                <Button
                  funcType="flat"
                  onClick={handleCreateClick}
                >
                  <Icon type="playlist_add icon" />
                  <FormattedMessage id="create" />
                </Button>
                <Button
                  funcType="flat"
                  onClick={handleEditClick}
                  disabled={readOnly}
                >
                  <Icon type="mode_edit icon" />
                  <FormattedMessage id="edit" />
                </Button>
                <C7NDivider type="vertical" />
                <Button onClick={fullScreenEdit}>
                  <Icon type="fullscreen" />
                  <FormattedMessage id="fullScreen" />
                </Button>
                <Button
                  funcType="flat"
                  onClick={handleBuzzClick}
                >
                  <Icon type="question_answer" />
                  <FormattedMessage id="page.doc.buzz" />
                </Button>
                <Dropdown overlay={getMenus()} trigger={['click']}>
                  <i className="icon icon-more_vert" style={{ marginLeft: 20, color: '#3f51b5', cursor: 'pointer', verticalAlign: 'text-bottom' }} />
                </Dropdown>
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
          </Header>
        ) : null
      }
      {!fullScreen
        ? (
          <Content style={{ padding: 0 }}>
            <Breadcrumb />
            <div style={{ height: 'calc( 100% - 65px )' }}>
              <Spin spinning={loading}>
                <ResizeContainer type="horizontal" style={{ borderTop: '1px solid #d3d3d3' }}>
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
                          <WorkSpace onClick={loadPage} onSave={handleSpaceSave} onDelete={handleDeleteDoc} onCreate={handleCreateClick} onCancel={handleCancel} />
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
                          <DocEditor readOnly={readOnly} loadWorkSpace={loadWorkSpace} />
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
          <Content style={{ padding: 0 }}>
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
                        <DocEditor
                          readOnly={readOnly}
                          fullScreen
                          loadWorkSpace={loadWorkSpace}
                          exitFullScreen={toggleFullScreen}
                          editDoc={handleEditClick}
                        />
                      </div>
                    </div>
                  </Spin>
                </Section>
                {pageStore.catalogVisible
                  ? (
                    <Divider />
                  ) : null
                }
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
                  ) : null
                }
              </ResizeContainer>
            </Spin>
          </Content>
        )
      }
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
      >
        {''}
      </Permission>
      <AttachmentRender />
      <DocModal 
        store={pageStore}
        selectId={selectId}
        mode={mode}
        refresh={loadWorkSpace}
        handleDeleteDraft={handleDeleteDraft}
        handleLoadDraft={handleLoadDraft}
      />
      {buzzVisible
        ? (
          <CooperateSide
            defaultOpenId={defaultOpenId}
            linkParam={{
              linkId: selectId,
              linkType: 'knowledge_page',
            }}
            onClose={handleBuzzClick}
          />
        ) : null}
    </Page>
  );
}

export default withRouter(injectIntl(observer(DocHome)));
