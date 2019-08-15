import React, { Component, useContext, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import { Spin, Modal } from 'choerodon-ui';
import { Page, Content, stores } from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import DocEditor from './components/doc-editor';
import PageStore from '../stores';
import AttachmentRender from '../../../components/Extensions/attachment/AttachmentRender';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import Catalog from '../../../components/Catalog';
import { getCurrentFullScreen, toFullScreen, exitFullScreen, addFullScreenEventListener, removeFullScreenEventListener } from './components/fullScreen';
import './style/index.less';

const { Section, Divider } = ResizeContainer;
const { AppState, MenuStore } = stores;
const { confirm } = Modal;
const { Fragment } = React;

function DocHome() {
  const { pageStore, history, type: levelType } = useContext(PageStore);
  const [loading, setLoading] = useState(false);
  const [docLoading, setDocLoading] = useState(false);
  const {
    getSpaceCode: code,
    getSelectId: selectId,
    getMode: mode,
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
    const { origin } = window.location;
    const { pathname, search } = history.location;
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
          pageStore.setSelectId(id);
          loadPage();
        } else {
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
      const params = queryString.parse(history.location.search);
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

  function handleExitFullScreen() {
    const { workSpace: { id: workSpaceId } } = pageStore.getDoc;
    const urlParams = AppState.currentMenuType;
    history.push(`/knowledge/${urlParams.type}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
  }

  useEffect(() => {
    addFullScreenEventListener(handleExitFullScreen);
    toFullScreen(document.getElementsByClassName('c7n-kb-doc')[0]);
    loadWorkSpace();
    return () => {
      removeFullScreenEventListener(handleExitFullScreen);
    };
  }, []);

  function handleEditClick() {
    pageStore.setCatalogVisible(false);
    pageStore.setMode('edit');
  }

  return (
    <Page
      className="c7n-kb-doc"
    >
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
                      exitFullScreen={handleExitFullScreen}
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
      <AttachmentRender />
    </Page>
  );
}

export default withRouter(injectIntl(observer(DocHome)));
