/* eslint-disable react/jsx-closing-tag-location */
import React, {
  useContext, useEffect, useState, useRef,
} from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Spin,
} from 'choerodon-ui';
import { TextField, Modal } from 'choerodon-ui/pro';
import {
  Page, Header, Content, stores, Permission, Breadcrumb, Choerodon,
} from '@choerodon/boot';
import { HeaderButtons } from '@choerodon/master';
import { withRouter } from 'react-router-dom';
import { injectIntl, useIntl } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import DocDetail from '../../../components/DocDetail';
import DocEditor from './components/doc-editor';
import PageStore from '../stores';
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
import openShare from './components/docModal/ShareModal';
import openImport from './components/docModal/ImportModal';
import openMove from './components/docModal/MoveMoal';
import './DocHome.less';

const { Section, Divider } = ResizeContainer;
const { AppState } = stores;

function DocHome() {
  const {
    pageStore, history, id: proId, organizationId: orgId, type: levelType,
  } = useContext(PageStore);
  const intl = useIntl();
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
  const [isFullScreen, toggleFullScreen] = useFullScreen(() => document.body, onFullScreenChange, 'c7nagile-doc-fullScreen');
  const {
    getSpaceCode: code,
    getSearchVisible: searchVisible,
    getSelectId: selectId,
    getMode: mode,
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
      parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
    }, true);
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function checkPermission(type) {
    if (levelType === 'organization') {
      // const orgData = HeaderStore.getOrgData;
      // const orgObj = orgData.find(v => String(v.id) === String(orgId));
      // if (!orgObj || (orgObj && !orgObj.into)) {
      // setReadOnly(true);
      // } else {
      setReadOnly(false);
      // }
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
        }
      }).catch(() => {
        setReadOnly(true);
        setDocLoading(false);
      });
    } else {
      // 没选文档时，显示主页
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      pageStore.setSelectId(false);
      pageStore.setDoc(false);
      pageStore.setSection('recent');
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
      id = params.spaceId;
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
      let newTree = removeItemFromTree(spaceData, {
        ...item,
        parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
      }, true);
      const newSelectId = item.parentId || item.workSpaceParentId || spaceData.rootId;
      newTree = mutateTree(newTree, newSelectId, { isClick: true });
      pageStore.setWorkSpaceByCode(code, newTree);
      pageStore.setSelectId(newSelectId);
      if (newSelectId !== spaceData.rootId) {
        loadPage(newSelectId);
      } else {
        pageStore.setSection('recent');
        pageStore.queryRecentUpdate();
        pageStore.setDoc(false);
        pageStore.loadWorkSpaceAll();
      }
    }).catch((error) => {
      Choerodon.prompt(error);
    });
  }
  function handleDeleteDoc(id, title, role) {
    Modal.open({
      title: `删除文档"${title}"`,
      children: `文档"${title}"将会被移至回收站，和问题的关联也会移除，您可以在回收站恢复此文档。`,
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
      openShare({ store: pageStore });
    });
  }

  const disabled = getTypeCode() === 'pro' ? ['share', 'org'].includes(spaceCode) : false;
  function handleCreateClick() {
    pageStore.setMode('view');
    CreateDoc({
      onCreate: async ({ title, template: templateId, root }) => {
        const workSpace = pageStore.getWorkSpace;
        const spaceData = workSpace[spaceCode].data;
        const currentCode = pageStore.getSpaceCode;
        let newTree = spaceData;
        const vo = {
          title: title.trim(),
          content: '',
          parentWorkspaceId: root ? spaceData.rootId : selectId || spaceData.rootId,
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
        parentId: (parent && parent.id) || spaceData.rootId,
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
        parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
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
    openImport({ store: pageStore });
  }

  function handleLogClick() {
    const { workSpace } = pageStore.getDoc;
    if (workSpace) {
      const { id: workSpaceId } = workSpace;
      handleShare(workSpaceId);
    }
  }
  async function handleCopyClick() {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode].data;
    let newTree = spaceData;
    const data = await pageStore.copyWorkSpace(selectId);
    newTree = mutateTree(spaceData, selectId, { isClick: false });
    pageStore.setSelectId(data.id);
    newTree = addItemToTree(
      newTree,
      { ...data.workSpace, createdBy: data.createdBy, isClick: true },
      'create',
    );
    pageStore.setWorkSpaceByCode(spaceCode, newTree);
    loadPage(data.workSpace.id, 'create');
  }
  function handleEditClick() {
    pageStore.setCatalogVisible(false);
    pageStore.setMode('edit');
    setLogVisible(false);
  }

  const handleSearchClick = (value) => {
    if (value) {
      pageStore.querySearchList(value).then((res) => {
        pageStore.setSearchVisible(true);
        const searchList = pageStore.getSearchList;
        if (searchList && searchList.length) {
          loadPage(searchList[0].workSpaceId, false, value);
        } else {
          pageStore.setDoc(false);
        }
      });
    } else {
      pageStore.setSearchVisible(false);
    }
  };

  function handleSearchChange(value) {
    setSearchValue(value);
    handleSearchClick(value);
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

  return (
    <Page
      className="c7n-kb-doc"
    >
      {!isFullScreen && (
        <Header className={`c7n-kb-doc-header${disabled || readOnly ? 'Disabled' : ''}`}>
          {section !== 'template'
            ? (
              <HeaderButtons items={[{
                name: '创建文档',
                icon: 'playlist_add',
                handler: handleCreateClick,
                disabled: disabled || readOnly,
                display: true,
              }, {
                name: intl.formatMessage({ id: 'import' }),
                icon: 'archive-o',
                handler: handleImportClick,
                disabled: disabled || readOnly,
                display: true,
              }, {
                name: intl.formatMessage({ id: 'edit' }),
                icon: 'edit-o',
                handler: handleEditClick,
                disabled: disabled || readOnly,
                display: section === 'tree' && selectId,
              }, {
                name: '复制',
                icon: 'file_copy-o',
                handler: handleCopyClick,
                disabled: disabled || readOnly,
                display: section === 'tree' && selectId,
              }, {
                display: section === 'tree' && selectId,
                name: '更多操作',
                disabled: disabled || readOnly,
                groupBtnItems: [{
                  name: '导出',
                  icon: 'unarchive-o',
                  handler: () => {
                    const { pageInfo, workSpace } = pageStore.getDoc;
                    if (!pageInfo || !workSpace) {
                      return;
                    }
                    const { id, title } = pageInfo;
                    Choerodon.prompt('正在导出，请稍候...');
                    pageStore.exportPdf(id, title);
                  },
                  disabled: disabled || readOnly,
                  display: section === 'tree' && selectId,
                }, {
                  name: '移动',
                  disabled: disabled || readOnly,
                  handler: () => {
                    const { pageInfo, workSpace } = pageStore.getDoc;
                    if (!pageInfo || !workSpace) {
                      return;
                    }
                    openMove({ store: pageStore, id: selectId, refresh: loadWorkSpace });
                  },
                }, {
                  name: '操作历史',
                  disabled: disabled || readOnly,
                  handler: () => {
                    const { pageInfo, workSpace } = pageStore.getDoc;
                    if (!pageInfo || !workSpace) {
                      return;
                    }
                    setLogVisible(true);
                  },
                }, {
                  name: '版本对比',
                  disabled: disabled || readOnly,
                  handler: () => {
                    const { pageInfo, workSpace } = pageStore.getDoc;
                    if (!pageInfo || !workSpace) {
                      return;
                    }
                    const { id: workSpaceId } = workSpace;
                    const urlParams = AppState.currentMenuType;
                    history.push(`/knowledge/${urlParams.type}/version/${pageStore.baseId}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
                  },
                }, {
                  name: '删除',
                  disabled: disabled || readOnly,
                  permissions: levelType === 'project'
                    ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
                    : ['choerodon.code.organization.knowledge.ps.doc.delete'],
                  handler: () => {
                    const docData = pageStore.getDoc;
                    const { pageInfo, workSpace } = pageStore.getDoc;
                    if (!pageInfo || !workSpace) {
                      return;
                    }
                    const { title } = pageInfo;
                    const { id: workSpaceId } = workSpace;
                    if (AppState.userInfo.id === docData.createdBy) {
                      handleDeleteDoc(workSpaceId, title);
                    } else {
                      handleDeleteDoc(workSpaceId, title, 'admin');
                    }
                  },
                }],
              }, {
                name: intl.formatMessage({ id: 'share' }),
                icon: 'share',
                handler: handleLogClick,
                disabled: disabled || readOnly,
                display: section === 'tree' && selectId,
                iconOnly: true,
              }, {
                icon: isFullScreen ? 'fullscreen_exit' : 'zoom_out_map',
                iconOnly: true,
                handler: toggleFullScreenEdit,
                display: true,
                tooltipsConfig: {
                  title: isFullScreen ? '退出全屏' : '全屏',
                },
              }, {
                display: true,
                element: (<TextField
                  style={{ marginRight: 8, marginTop: disabled || readOnly ? 4 : 0 }}
                  placeholder="搜索"
                  value={searchValue}
                  valueChangeAction="input"
                  wait={300}
                  onChange={handleSearchChange}
                />),
              }]}
              />
            ) : (
              <HeaderButtons items={[{
                name: '创建模板',
                handler: handleTemplateCreateClick,
                icon: 'playlist_add',
                display: true,
              }]}
              />
            )}
        </Header>
      )}
      {!isFullScreen && <Breadcrumb title={queryString.parse(history.location.search).baseName || ''} />}
      <Content style={{
        padding: 0, height: '100%', margin: 0, overflowY: 'hidden',
      }}
      >
        <div style={{ height: '100%' }}>
          <Spin spinning={loading}>
            <ResizeContainer type="horizontal" style={{ overflowX: 'hidden' }}>
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
              {!searchVisible && !isFullScreen
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
                      {section === 'tree' && (
                      <DocEditor
                        readOnly={disabled || readOnly}
                        loadWorkSpace={loadWorkSpace}
                        searchText={searchValue}
                        editTitleBefore={() => setLogVisible(false)}
                        fullScreen={isFullScreen}
                        exitFullScreen={toggleFullScreenEdit}
                        editDoc={handleEditClick}
                      />
                      )}
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
      {logVisible
        ? (
          <DocDetail onCollapse={() => setLogVisible(false)} store={pageStore} />
        ) : null}
      <Permission
        key="adminDelete"
        type={levelType}
        projectId={proId}
        organizationId={orgId}
        service={levelType === 'project'
          ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
          : ['choerodon.code.organization.knowledge.ps.doc.delete']}
      >
        {null}
      </Permission>
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
