import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Choerodon } from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import {
  BackTop, Input, Icon, Tooltip,
} from 'choerodon-ui';
import { TextField, Button, Form } from 'choerodon-ui/pro';
import 'codemirror/lib/codemirror.css';
import '@toast-ui/editor/dist/toastui-editor.css';
import table from '@toast-ui/editor-plugin-table-merged-cell';
import { Viewer } from '@toast-ui/react-editor';
import Lightbox from 'react-image-lightbox';
import DocHeader from '../DocHeader';
import DocAttachment from '../doc-attachment';
import DocComment from '../doc-comment';
import './DocViewer.less';

@observer
class DocViewer extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      hasImageViewer: false,
      imgSrc: false,
      editTitle: false,
      loading: false,
    };
    this.ref = React.createRef();
  }

  componentDidMount() {
    this.ref.current.addEventListener('click', this.onImageClick);
  }

  componentWillUnmount() {
    this.ref.current.removeEventListener('click', this.onImageClick);
  }

  onImageClick = (e) => {
    const { hasImageViewer } = this.state;
    if (!hasImageViewer) {
      if (e && e.target && e.target.nodeName === 'IMG' && e.target.className === '') {
        this.setState({
          hasImageViewer: true,
          imgSrc: e.target.src,
        });
        e.stopPropagation();
      }
    }
  };

  onViewerClose = () => {
    this.setState({
      hasImageViewer: false,
      imgSrc: false,
    });
  };

  handleClickTitle = () => {
    const { editTitleBefore } = this.props;
    editTitleBefore();
    this.setState({
      editTitle: true,
    });
  };

  handleCancel = () => {
    this.setState({
      editTitle: false,
      newTitle: false,
      loading: false,
    });
  };

  handleTitleChange = (value) => {
    this.setState({
      newTitle: value,
    });
  };

  handleSubmit = () => {
    const { newTitle } = this.state;
    const { store, searchText } = this.props;
    if (newTitle && newTitle.trim()) {
      this.setState({
        loading: true,
      });
      const { getDoc: { pageInfo: { objectVersionNumber }, workSpace: { id } } } = store;
      const doc = {
        title: newTitle,
        objectVersionNumber,
      };
      store.editDoc(id, doc, searchText);
    }
    this.handleCancel();
  };

  render() {
    const {
      hasImageViewer, imgSrc, editTitle, loading,
    } = this.state;
    const {
      data,
      store,
      readOnly,
      fullScreen,
      editDoc,
      exitFullScreen,
    } = this.props;
    const searchVisible = store.getSearchVisible;

    return (
      <div className="c7n-docViewer" ref={this.ref}>
        <DocHeader {...this.props} />
        <div className="c7n-docViewer-wrapper" id="docViewer-scroll">
          <DocAttachment store={store} readOnly={readOnly} />
          <div className="c7n-docViewer-content">
            {editTitle
              ? (
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <Form style={{
                    maxWidth: 684, width: 'calc(100% - 150px)', marginBottom: -20, marginLeft: -5,
                  }}
                  >
                    <TextField
                      size="large"
                      showLengthInfo={false}
                      maxLength={40}
                      defaultValue={data.pageInfo.title || ''}
                      onChange={this.handleTitleChange}
                      label="文档标题"
                      labelLayout="float"
                    />
                  </Form>
                  <Button
                    color="primary"
                    style={{ marginLeft: 10 }}
                    onClick={this.handleSubmit}
                    loading={loading}
                  >
                    <FormattedMessage id="save" />
                  </Button>
                  <Button
                    style={{ marginLeft: 10 }}
                    onClick={this.handleCancel}
                  >
                    <FormattedMessage id="cancel" />
                  </Button>
                </div>
              ) : (
                <div className="c7n-docViewer-title">
                  {data.pageInfo.title}
                  {readOnly
                    ? null
                    : (
                      <Icon
                        type="edit-o"
                        className="c7n-docHeader-title-edit"
                        onClick={this.handleClickTitle}
                      />
                    )}
                  {fullScreen
                    ? (
                      <span style={{ float: 'right', margin: '-2px 5px 0 0' }}>
                        {readOnly
                          ? null
                          : (
                            <Button type="primary" funcType="flat" onClick={editDoc}>
                              <Icon type="edit-o icon" />
                              <FormattedMessage id="edit" />
                            </Button>
                          )}
                        <Button type="primary" funcType="flat" onClick={exitFullScreen}>
                          <Icon type="fullscreen_exit" />
                          <FormattedMessage id="exitFullScreen" />
                        </Button>
                      </span>
                    ) : null}
                </div>
              )}
            <Viewer
              initialValue={searchVisible ? data.pageInfo.highlightContent : data.pageInfo.content}
              usageStatistics={false}
              plugins={[table]}
            />
          </div>
          <div className="c7n-docViewer-footer">
            <div>
              <span className="c7n-docViewer-mRight">创建者</span>
              {data.createUser
                ? (
                  <Tooltip placement="top" title={data.pageInfo.createUser.ldap ? `${data.pageInfo.createUser.realName}（${data.pageInfo.createUser.loginName}）` : `${data.pageInfo.createUser.realName}（${data.pageInfo.createUser.email}）`}>
                    <span className="c7n-docViewer-mRight">{data.pageInfo.createUser.realName || data.pageInfo.createUser.loginName}</span>
                  </Tooltip>
                ) : '无'}
              {'（'}
              {data.pageInfo.creationDate || ''}
              ）
            </div>
            <div>
              <span className="c7n-docViewer-mRight">最近编辑</span>
              {data.lastUpdatedUser
                ? (
                  <Tooltip placement="top" title={data.pageInfo.lastUpdatedUser.ldap ? `${data.pageInfo.lastUpdatedUser.realName}（${data.pageInfo.lastUpdatedUser.loginName}）` : `${data.pageInfo.lastUpdatedUser.realName}（${data.pageInfo.lastUpdatedUser.email}）`}>
                    <span className="c7n-docViewer-mRight">{data.pageInfo.lastUpdatedUser.realName || data.pageInfo.lastUpdatedUser.loginName}</span>
                  </Tooltip>
                ) : '无'}
              {'（'}
              {data.pageInfo.lastUpdateDate || ''}
              ）
            </div>
          </div>
          {!readOnly
            ? <DocComment data={data} store={store} />
            : null}
          <BackTop target={() => document.getElementById('docViewer-scroll')}>
            {/* <Icon type="vertical_align_top" className="c7n-backTop-icon" /> */}
          </BackTop>
        </div>
        {hasImageViewer
          ? (
            <Lightbox
              mainSrc={imgSrc}
              onCloseRequest={this.onViewerClose}
              imageTitle="images"
            />
          ) : ''}
      </div>
    );
  }
}
export default withRouter(DocViewer);
