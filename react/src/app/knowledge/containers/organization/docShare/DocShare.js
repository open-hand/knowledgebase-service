import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Spin, Icon, Modal, Input,
} from 'choerodon-ui';
import {
  Page, Header, Content, axios, stores,
} from '@choerodon/boot';
import { mutateTree } from '@atlaskit/tree';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import DocStore from '../../../stores/organization/doc/DocStore';
import DocViewer from '../../../components/DocViewer';
import DocCatalog from '../../../components/DocCatalog';
import DocDetail from '../../../components/DocDetail';
import DocEmpty from '../../../components/DocEmpty/DocEmpty';
import WorkSpaceShare, { addItemToTree, removeItemFromTree } from '../../../components/WorkSpaceShare';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import NoMatch from '../../../components/ErrorPages/404';
import './DocShare.scss';

const { Section, Divider } = ResizeContainer;

@observer
class DocShare extends Component {
  constructor(props) {
    super(props);
    this.state = {
      sideBarVisible: false,
      catalogVisible: false,
      selectId: false,
      loading: true,
      docLoading: true,
      currentNav: 'attachment',
    };
  }

  componentDidMount() {
    const { match } = this.props;
    DocStore.getSpaceByToken(match.params.token).then(() => {
      this.initSelect();
      this.setState({
        loading: false,
        docLoading: false,
      });
    });
  }

  initSelect = () => {
    const { match } = this.props;
    const spaceData = DocStore.getShareWorkSpace;
    // 默认选中第一篇文档
    if (spaceData.items && spaceData.items['0'] && spaceData.items['0'].children.length) {
      const selectId = spaceData.items['0'].children[0];
      // 加载第一篇文档
      DocStore.getDocByToken(selectId, match.params.token);
      // 选中第一篇文档菜单
      const newTree = mutateTree(spaceData, selectId, { isClick: true });
      DocStore.setShareWorkSpace(newTree);
      this.setState({
        selectId,
        loading: false,
        docLoading: false,
      });
    } else {
      this.setState({
        selectId: false,
        loading: false,
        docLoading: false,
      });
      DocStore.setShareDoc(false);
    }
  };

  handleSpaceExpand = (data, itemId) => {
    DocStore.setShareWorkSpace(data);
  };

  handleSpaceCollapse = (data) => {
    DocStore.setShareWorkSpace(data);
  };

  /**
   * 点击空间
   * @param data
   * @param selectId
   */
  handleSpaceClick = (data, selectId) => {
    const { match } = this.props;
    const { token } = match.params;
    DocStore.setShareWorkSpace(data);
    this.setState({
      docLoading: true,
      catalogVisible: false,
      selectId,
    });
    // 加载详情
    DocStore.getDocByToken(selectId, token).then(() => {
      const { sideBarVisible } = this.state;
      const docData = DocStore.getShareDoc;
      if (sideBarVisible) {
        DocStore.getAttachmentByToken(docData.pageInfo.id, token);
      }
      this.setState({
        docLoading: false,
      });
    });
  };

  /**
   * 导航被点击跳转
   * @param id
   */
  handleBreadcrumbClick = (id) => {
    const { selectId } = this.state;
    const spaceData = DocStore.getShareWorkSpace;
    let newTree = mutateTree(spaceData, id, { isClick: true });
    if (selectId && selectId !== id && newTree.items[selectId]) {
      newTree = mutateTree(newTree, selectId, { isClick: false });
    }
    this.handleSpaceClick(newTree, id);
  };

  handleBtnClick = (type) => {
    const { match } = this.props;
    const { catalogVisible } = this.state;
    const docData = DocStore.getShareDoc;
    const { id, title } = docData.pageInfo;
    const { token } = match.params;
    switch (type) {
      case 'attach':
        this.setState({
          currentNav: 'attachment',
          sideBarVisible: true,
        });
        break;
      case 'catalog':
        this.setState({
          catalogVisible: !catalogVisible,
        });
        break;
      case 'export':
        DocStore.exportPdfByToken(id, title, token);
        break;
      default:
        break;
    }
  };

  render() {
    const { match } = this.props;
    const {
      selectId, catalogVisible, docLoading,
      sideBarVisible, loading, currentNav,
    } = this.state;
    const spaceData = DocStore.getShareWorkSpace;
    const docData = DocStore.getShareDoc;
    const { token } = match.params;

    return (
      <Page
        className="c7n-knowledge"
      >
        {spaceData.noAccess
          ? (
            <NoMatch />
          ) : (
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
                    width: 40,
                    minWidth: 40,
                    maxWidth: 600,
                  }}
                  style={{
                    minWidth: 40,
                    maxWidth: 600,
                  }}
                >
                  <div className="c7n-knowledge-left">
                    <WorkSpaceShare
                      data={spaceData}
                      selectId={selectId}
                      onClick={this.handleSpaceClick}
                      onExpand={this.handleSpaceExpand}
                      onCollapse={this.handleSpaceCollapse}
                    />
                  </div>
                </Section>
                <Divider />
                <Section
                  style={{ flex: 'auto', backgroundColor: 'white', zIndex: 5 }}
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
                          ? (
                            <DocViewer
                              mode="share"
                              data={docData}
                              spaceData={spaceData}
                              onBreadcrumbClick={this.handleBreadcrumbClick}
                              onBtnClick={this.handleBtnClick}
                              loginUserId={null}
                              permission={false}
                              onTitleEdit={this.handleTitleChange}
                              catalogVisible={catalogVisible}
                            />
                          ) : (
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
                      <DocCatalog />
                    </Section>
                  ) : null
                }
                {sideBarVisible
                  ? (
                    <DocDetail
                      mode="share"
                      token={token}
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
          )
        }
      </Page>
    );
  }
}

export default withRouter(injectIntl(DocShare));
