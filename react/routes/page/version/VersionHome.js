import React, {
  useContext, useEffect, useState, useCallback,
} from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Page, Content, Breadcrumb, Header, stores,
} from '@choerodon/boot';
import { HeaderButtons } from '@choerodon/master';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import Loading, { LoadingProvider } from '@choerodon/agile/lib/components/Loading';
import DocVersion from '../../../components/DocVersion';
import PageStore from '../stores';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace from '../components/work-space';
import './style/index.less';

const { Section, Divider } = ResizeContainer;

const { AppState } = stores;

function VersionHome() {
  const {
    pageStore, history, id: proId, organizationId: orgId, type: levelType,
  } = useContext(PageStore);
  const [loading, setLoading] = useState(false);
  const [docLoading, setDocLoading] = useState(false);
  const [mode, setMode] = useState('edit');
  const { getSpaceCode: code, getSelectId: selectId, getDoc: docData } = pageStore;

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
        const list = workSpace[key] && workSpace[key].data.items[workSpace.rootId].children;
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
    }
    return false;
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
          setMode(isCreate ? 'edit' : 'view');
          pageStore.setMode(isCreate ? 'edit' : 'view');
        }
      }).catch(() => {
        setDocLoading(false);
        setMode('view');
      });
    } else {
      setDocLoading(false);
    }
  }

  /**
   * 加载空间
   */
  const loadWorkSpace = useCallback((spaceId) => {
    let id = spaceId;
    if (!id) {
      const params = queryString.parse(history.location.search);
      id = params.spaceId;
    }
    // 初始化
    // setLoading(true);
    setMode('view');
    if (id) {
      pageStore.setSelectId(id);
    }
    const type = levelType === 'project' ? 'pro' : 'org';
    pageStore.loadWorkSpaceAll(id || selectId, type).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        // 如果id错误或不存在
        pageStore.loadWorkSpaceAll(false, type).then(() => {
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
  }, [history.location.search, levelType, loadPage, pageStore, selectId]);

  useEffect(() => {
    // 加载数据
    loadWorkSpace();
  }, []);

  const backToDoc = useCallback(() => {
    const urlParams = AppState.currentMenuType;
    const { getDoc: { workSpace: { id: workSpaceId } } } = pageStore;
    history.push(`/knowledge/${urlParams.type}/doc/${pageStore.baseId}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
  }, [history, pageStore]);

  return (
    <Page
      className="c7n-kb-version"
    >
      <Breadcrumb />
      <Header>
        <HeaderButtons items={[{
          name: '返回知识库',
          display: true,
          icon: 'arrow_back',
          handler: backToDoc,
        }]}
        />
      </Header>
      <Content style={{ padding: 0, overflow: 'hidden', margin: 0 }}>
        <div style={{ height: 'calc( 100% - 0px )' }}>
          <LoadingProvider loading={loading}>
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
                <div className="c7n-kb-version-left">
                  <WorkSpace onClick={loadPage} readOnly />
                </div>
              </Section>
              <Divider />
              <Section
                style={{ flex: 1 }}
                size={{
                  width: 'auto',
                }}
              >
                <Loading loading={docLoading} allowSelfLoading>
                  <div className="c7n-kb-version-doc">
                    <div className="c7n-kb-version-content">
                      {docData
                        ? (
                          <DocVersion store={pageStore} onRollback />
                        ) : null}
                    </div>
                  </div>
                </Loading>
              </Section>
            </ResizeContainer>
          </LoadingProvider>
        </div>
      </Content>
    </Page>
  );
}

export default withRouter(injectIntl(observer(VersionHome)));
