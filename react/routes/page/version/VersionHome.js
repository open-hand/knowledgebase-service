import React, { Component, useContext, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Button, Icon, Dropdown, Spin, Input, Divider as C7NDivider, Menu, Modal,
} from 'choerodon-ui';
import {
  Page, Header, Content, stores, Permission, Breadcrumb,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import DocVersion from '../../../components/DocVersion';
import PageStore from '../stores';
import AttachmentRender from '../../../components/Extensions/attachment/AttachmentRender';
import { removeItemFromTree, addItemToTree } from '../../../components/WorkSpaceTree';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace from '../components/work-space';
import DocCatalog from '../../../components/Catalog';
import './style/index.less';

const { Section, Divider } = ResizeContainer;
const { AppState, MenuStore } = stores;
const { confirm } = Modal;

function DocHome() {
  const { pageStore, history, id: proId, organizationId: orgId, type: levelType } = useContext(PageStore);
  const [loading, setLoading] = useState(false);
  const [docLoading, setDocLoading] = useState(false);
  const [mode, setMode] = useState('edit');
  const [selectId, setSelectId] = useState(false);
  const [catalogVisible, setCatalogVisible] = useState(false);
  const [logVisible, setLogVisible] = useState(false);
  const [creating, setCreating] = useState(false);
  const [saving, setSaving] = useState(false);

  /**
   * 将文档id加入url
   * @param spaceId
   */
  function changeUrl(spaceId) {
    const { origin } = window.location;
    const { pathname, search } = history.location;
    const params = queryString.parse(search);
    params.spaceId = spaceId;
    const newParams = queryString.stringify(params);
    const newUrl = `${origin}#${pathname}?${newParams}`;
    window.history.pushState({}, 0, newUrl);
  }

  function handleSpaceClick(spaceId) {

  }

  /**
   * 默认选中空间并返回id
   * @returns id
   * 注意: 此函数会更改空间数据
   */
  function getDefaultSpaceId() {
    const workSpace = pageStore.getWorkSpace;
    let spaceId = false;
    let code = false;
    Object.keys(workSpace).forEach((key) => {
      if (!spaceId) {
        const list = workSpace[key] && workSpace[key].data.items[0].children;
        if (list && list.length) {
          [spaceId] = list;
          code = key;
        }
      }
    });
    if (spaceId) {
      const newTree = mutateTree(workSpace[code].data, spaceId, { isClick: true });
      pageStore.setWorkSpaceByCode(code, newTree);
      pageStore.setSpaceCode(code);
      pageStore.setSelectId(spaceId);
      return spaceId;
    } else {
      return false;
    }
  }

  /**
   * 加载文档详情
   * @param spaceId 空间id
   * @param isCreate
   */
  function loadPage(spaceId = false, isCreate = false) {
    setDocLoading(true);
    const id = spaceId || getDefaultSpaceId();
    if (id) {
      changeUrl(id);
      pageStore.loadDoc(id).then((res) => {
        if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
          setSelectId(id);
          loadPage();
        } else {
          setDocLoading(false);
          setMode(isCreate ? 'edit' : 'view');
          pageStore.setMode(isCreate ? 'edit' : 'view');
          pageStore.setImportVisible(false);
          pageStore.setShareVisible(false);
        }
      }).catch(() => {
        setDocLoading(false);
        setMode('view');
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
      const params = queryString.parse(history.location.search);
      id = params.spaceId;
    }
    // 初始化
    setLoading(true);
    setMode('view');
    if (id) {
      setSelectId(id);
    }
    pageStore.loadWorkSpaceAll(selectId).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        // 如果id错误或不存在
        pageStore.loadWorkSpaceAll().then(() => {
          setSelectId(false);
          setLoading(false);
          loadPage();
        });
      } else {
        setLoading(false);
        loadPage(selectId);
      }
    }).catch((e) => {
      setLoading(false);
    });
  }

  useEffect(() => {
    // 加载数据
    loadWorkSpace();
  }, []);

  function deleteDoc(spaceId, role) {
    const workSpace = pageStore.getWorkSpace;
    const code = pageStore.getSpaceCode;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    if (role === 'admin') {
      pageStore.adminDeleteDoc(spaceId).then(() => {
        const newTree = removeItemFromTree(spaceData, {
          ...item,
          parentId: item.parentId || item.workSpaceParentId || 0,
        });
        pageStore.setWorkSpace(code, newTree);
        const newSelectId = item.parentId || item.workSpaceParentId || 0;
        setSelectId(newSelectId);
        loadWorkSpace(newSelectId);
      }).catch((error) => {
      });
    } else {
      pageStore.deleteDoc(selectId).then(() => {
        const newTree = removeItemFromTree(spaceData, {
          ...item,
          parentId: item.parentId || item.workSpaceParentId || 0,
        });
        pageStore.setWorkSpace(code, newTree);
        const newSelectId = item.parentId || item.workSpaceParentId || 0;
        setSelectId(newSelectId);
        loadWorkSpace(newSelectId);
      }).catch((error) => {
      });
    }
  }

  function handleDeleteDoc(id, role) {
    const spaceData = pageStore.getWorkSpace;
    const spaceCode = pageStore.getSpaceCode;
    const item = spaceData[spaceCode].data.items[selectId];
    confirm({
      title: `删除文档"${item.data.title}"`,
      content: `如果文档下面有子级，也会被同时删除，确定要删除文档"${item.data.title}"吗?`,
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
    const { pageInfo: { pageId, title }, workSpace: { id: workSpaceId } } = pageStore.getDoc;
    switch (e.key) {
      case 'delete':
        handleDeleteDoc(workSpaceId);
        break;
      case 'adminDelete':
        handleDeleteDoc(workSpaceId, 'admin');
        break;
      case 'edit':
        setMode('edit');
        setCatalogVisible(false);
        break;
      case 'log':
        setLogVisible(true);
        break;
      case 'catalog':
        setCatalogVisible(!catalogVisible);
        break;
      case 'version':
        // 跳转
        break;
      case 'export':
        Choerodon.prompt('正在导出，请稍候...');
        pageStore.exportPdf(pageId, title);
        break;
      case 'share':
        handleShare(workSpaceId);
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
          )
        }
      </Menu>
    );
  }

  function handleCreateClick() {
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
        parentId: 0,
      };
      const newTree = addItemToTree(spaceData, item);
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
    }
  }

  function handleEditClick() {
    pageStore.setMode('edit');
  }

  /**
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  function handleSpaceSave(value, item) {
    const spaceCode = levelType === 'project' ? 'pro' : 'org';
    const currentCode = pageStore.getSpaceCode;
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode];
    if (!value || !value.trim() || saving) {
      return;
    }
    setSaving(true);
    setCreating(false);
    let newTree = spaceData;
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
          newTree = mutateTree(workSpace[currentCode].data, selectId, { isClick: false });
        }
      }

      newTree = addItemToTree(
        newTree,
        { ...data.workSpace, createdBy: data.createdBy, isClick: true },
        'create',
      );
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
      loadPage(data.workSpace.id, 'create');
    });
  }

  return (
    <Page
      className="c7n-kb-doc"
    >
      <Content style={{ padding: 0 }}>
        <Breadcrumb title={'协作 > 知识库'} />
        <div style={{ height: 'calc( 100% - 68px )' }}>
          <Spin spinning={loading}>
            <ResizeContainer type="horizontal" style={{ borderTop: '1px solid #d3d3d3' }}>
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
                  <WorkSpace onClick={loadPage} onSave={handleSpaceSave} />
                </div>
              </Section>
              <Divider />
              <Section
                style={{ flex: 'auto' }}
                size={{
                  width: 'auto',
                }}
              >
                <Spin spinning={docLoading}>
                  <div className="c7n-kb-doc-doc">
                    <div className="c7n-kb-doc-content">
                      <DocVersion store={pageStore} />
                    </div>
                  </div>
                </Spin>
              </Section>
              <DocCatalog visible={catalogVisible} />
            </ResizeContainer>
          </Spin>
        </div>
      </Content>
      <AttachmentRender />
    </Page>
  );
}

export default withRouter(injectIntl(observer(DocHome)));
