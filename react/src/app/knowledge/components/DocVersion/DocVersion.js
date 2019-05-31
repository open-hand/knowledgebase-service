import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Checkbox, Tooltip, Modal, Button } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import TimeAgo from 'timeago-react';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Viewer } from '@toast-ui/react-editor';
import UserHead from '../UserHead';
import './DocVersion.scss';

const { confirm } = Modal;

@inject('AppState')
@observer class DocAttachment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      firstVersionId: false,
      secondVersionId: false,
    };
  }

  componentDidMount() {
    this.loadVersions();
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setDocVersion(false);
  }

  escape = str => str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');

  loadVersions = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadVersion(docData.pageInfo.id).then(() => {
      const versions = store.getVersion;
      if (versions && versions.length) {
        store.loadDocByVersion(versions[0].id, docData.pageInfo.id);
        this.setState({
          firstVersionId: versions[0].id,
        });
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
    const { store, onRollback } = this.props;
    const docData = store.getDoc;
    confirm({
      title: '恢复版本',
      content: '你确定要恢复文章之前的版本吗？',
      okText: '确定',
      cancelText: '取消',
      width: 520,
      onOk() {
        store.rollbackVersion(versionId, docData.pageInfo.id).then(() => {
          if (onRollback) {
            onRollback();
          }
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
                color="#3F51B5"
                user={{
                  id: version.createdBy,
                  loginName: version.createUserLoginName,
                  realName: version.createUserRealName,
                  avatar: version.createUserImageUrl,
                }}
              />
            </div>
            <div>
              <Tooltip placement="top" title={version.creationDate || ''}>
                <TimeAgo
                  datetime={version.creationDate || ''}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              </Tooltip>
            </div>
          </div>
          <div className="c7n-docVersion-rollback">
            <Tooltip placement="top" title="回滚">
              <Button
                shape="circle"
                size="small"
                onClick={() => this.handleRollback(version.id)}
              >
                <i className="icon icon-restore" />
              </Button>
            </Tooltip>
          </div>
        </div>,
      );
    });
    return versionList;
  };

  render() {
    const { firstVersionId, secondVersionId } = this.state;
    const { store } = this.props;
    const docData = store.getDoc;
    const versions = store.getVersion;
    const doc = store.getDocVersion;
    const docCompare = store.getDocCompare || '';

    return (
      <div className="c7n-docVersion">
        <div className="c7n-docVersion-content">
          <div className="c7n-docVersion-tip">
            <span className="c7n-tip-icon c7n-tip-add" />
            新增
            <span className="c7n-tip-icon c7n-tip-delete" />
            删除
            <span className="c7n-tip-icon c7n-tip-update" />
            更新
          </div>
          <div className="c7n-docVersion-title">
            {docData.pageInfo.title}
          </div>
          <div className="c7n-docVersion-wrapper">
            {firstVersionId && secondVersionId
              ? (
                <div
                  className="c7n-docVersion-compare"
                  dangerouslySetInnerHTML={{ __html: this.escape(docCompare) }}
                />
              ) : (
                <Viewer
                  initialValue={doc}
                />
              )
            }
          </div>
        </div>
        <div className="c7n-docVersion-list">
          <div className="c7n-docVersion-list-title">
            版本
          </div>
          {this.renderVersionList(versions)}
        </div>
      </div>
    );
  }
}

export default withRouter(injectIntl(DocAttachment));
