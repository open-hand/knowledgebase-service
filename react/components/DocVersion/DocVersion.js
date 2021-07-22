import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import {
  Checkbox, Tooltip, Button, Icon,
} from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { stores, Choerodon } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import Lightbox from 'react-image-lightbox';
import { Viewer } from '@toast-ui/react-editor';
import { escape } from '../../utils';
import UserHead from '../UserHead';
import './DocVersion.less';

const { AppState } = stores;

@inject('AppState')
@observer class DocAttachment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      firstVersionId: false,
      secondVersionId: false,
      hasImageViewer: false,
      imgSrc: false,
    };
    this.ref = React.createRef();
  }

  componentDidMount() {
    this.loadVersions();
    this.ref.current.addEventListener('click', this.onImageClick);
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setDocVersion(false);
    this.ref.current.removeEventListener('click', this.onImageClick);
  }

  componentWillReceiveProps(nextProps) {
    this.loadVersions();
  }

  loadVersions = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadVersion(docData.pageInfo.id).then(() => {
      const versions = store.getVersion;
      if (versions && versions.length) {
        store.loadDocByVersion(versions[0].id, docData.pageInfo.id);
        this.setState({
          firstVersionId: versions[0].id,
          secondVersionId: false,
        });
      } else {
        store.setDocVersion(docData.pageInfo.content);
      }
    });
  };

  isChecked = (id) => {
    const { firstVersionId, secondVersionId } = this.state;
    return firstVersionId === id || secondVersionId === id;
  };

  isDisabled = (id) => {
    const { firstVersionId, secondVersionId } = this.state;
    return firstVersionId && secondVersionId && firstVersionId !== id && secondVersionId !== id;
  };

  onCheckChange = (id) => {
    const { store } = this.props;
    const docData = store.getDoc;
    const { firstVersionId, secondVersionId } = this.state;
    if (firstVersionId === id) {
      this.setState({
        firstVersionId: false,
      });
      // 加载secondVersionId版本文档
      if (secondVersionId) {
        store.loadDocByVersion(secondVersionId, docData.pageInfo.id);
      }
    } else if (secondVersionId === id) {
      this.setState({
        secondVersionId: false,
      });
      // 加载firstVersionId版本文档
      if (firstVersionId) {
        store.loadDocByVersion(firstVersionId, docData.pageInfo.id);
      }
    } else if (!firstVersionId) {
      this.setState({
        firstVersionId: id,
      });
      if (secondVersionId) {
        // 加载比较结果
        store.compareVersion(id, secondVersionId, docData.pageInfo.id);
      } else {
        store.loadDocByVersion(id, docData.pageInfo.id);
      }
    } else if (!secondVersionId) {
      this.setState({
        secondVersionId: id,
      });
      if (firstVersionId) {
        // 加载比较结果
        store.compareVersion(firstVersionId, id, docData.pageInfo.id);
      } else {
        store.loadDocByVersion(id, docData.pageInfo.id);
      }
    }
  };

  handleRollback = (versionId) => {
    const that = this;
    const { store } = this.props;
    const docData = store.getDoc;
    Modal.open({
      title: '恢复版本',
      children: '你确定要恢复到所选的版本吗？',
      okText: '确定',
      cancelText: '取消',
      width: 520,
      onOk() {
        store.rollbackVersion(versionId, docData.pageInfo.id).then(() => {
          that.backToDoc();
        });
      },
      onCancel() {
      },
    });
  };

  renderVersionList = (versions) => {
    const versionList = [];
    versions.forEach((version) => {
      versionList.push(
        <div className={`c7n-docVersion-item ${this.isChecked(version.id) ? 'c7n-docVersion-checked' : ''}`}>
          <div className="c7n-docVersion-check">
            <Checkbox
              onChange={() => this.onCheckChange(version.id)}
              disabled={this.isDisabled(version.id)}
              checked={this.isChecked(version.id)}
            />
          </div>
          <div className="c7n-docVersion-message">
            <div>
              <UserHead
                color="var(--text-color3)"
                user={version.createUser}
              />
            </div>
            <div style={{ whiteSpace: 'nowrap' }}>
              {version.creationDate || ''}
            </div>
          </div>
          <div className="c7n-docVersion-rollback">
            <Tooltip placement="top" title="回滚">
              <Button
                size="small"
                onClick={() => this.handleRollback(version.id)}
                icon="restore"
                shape="circle"
              />
            </Tooltip>
          </div>
        </div>,
      );
    });
    return versionList;
  };

  backToDoc = () => {
    const urlParams = AppState.currentMenuType;
    const { store, history } = this.props;
    const { getDoc: { workSpace: { id: workSpaceId } } } = store;
    history.push(`/knowledge/${urlParams.type}/doc/${store.baseId}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
  };

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

  render() {
    const {
      firstVersionId, secondVersionId, hasImageViewer, imgSrc,
    } = this.state;
    const { store } = this.props;
    const docData = store.getDoc;
    const versions = store.getVersion;
    const doc = store.getDocVersion;
    const docCompare = store.getDocCompare || {};
    return (
      <div className="c7n-docVersion" ref={this.ref}>
        <div className="c7n-docVersion-content">
          <div className="c7n-docVersion-tip">
            <span className="c7n-tip-icon c7n-tip-add" />
            新增
            <span className="c7n-tip-icon c7n-tip-delete" />
            删除
          </div>
          <div className="c7n-docVersion-title">
            {firstVersionId && secondVersionId
              ? (
                <div
                  className="c7n-docVersion-compareTitle"
                  dangerouslySetInnerHTML={{ __html: escape(docCompare.diffTitle || '') }}
                />
              ) : doc.title}
          </div>
          <div className="c7n-docVersion-wrapper">
            {firstVersionId && secondVersionId
              ? (
                <div
                  className="c7n-docVersion-compare"
                  dangerouslySetInnerHTML={{ __html: escape(docCompare.diffContent || '') }}
                />
              ) : (
                <Viewer
                  key={doc.id}
                  initialValue={doc.content}
                />
              )}
          </div>
        </div>
        <div className="c7n-docVersion-list">
          <div className="c7n-docVersion-list-title">
            版本
          </div>
          {this.renderVersionList(versions)}
        </div>
        {hasImageViewer
          ? (
            <Lightbox
              clickOutsideToClose={false}
              mainSrc={imgSrc}
              onCloseRequest={this.onViewerClose}
              imageTitle="图片"
            />
          ) : ''}
      </div>
    );
  }
}

export default withRouter(injectIntl(DocAttachment));
