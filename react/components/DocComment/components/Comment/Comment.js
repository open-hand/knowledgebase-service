import React, { Component } from 'react';
import { stores, Permission, Choerodon } from '@choerodon/boot';
import { Icon, Popconfirm, Tooltip } from 'choerodon-ui';
import TimeAgo from 'timeago-react';
import 'codemirror/lib/codemirror.css';
import '@toast-ui/editor/dist/toastui-editor.css';
import { Viewer } from '@toast-ui/react-editor';
import UserHead from '../../../UserHead';
import DocEditor from '../../../Editor';
import './Comment.less';

const { AppState } = stores;

class Comment extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      editCommentId: undefined,
      editComment: undefined,
      expand: false,
    };
  }

  handleCommitDelete = (commentId, mode) => {
    const { onCommentDelete } = this.props;
    if (onCommentDelete) {
      onCommentDelete(commentId, mode);
    }
    this.handleCancel();
  };

  handleCommentEdit = (data) => {
    const { onCommentEdit, comment } = this.props;
    if (onCommentEdit) {
      onCommentEdit(comment, data);
    }
    this.handleCancel();
  };

  handleCancel = () => {
    this.setState({
      editCommentId: undefined,
      editComment: undefined,
    });
  };

  render() {
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    const { comment } = this.props;
    const { editComment, editCommentId, expand } = this.state;
    const {
      id, userId, createUser, lastUpdateDate, comment: content,
    } = comment;

    return (
      <div
        className={`c7n-comment ${id === editCommentId ? 'c7n-comment-focus' : ''}`}
      >
        <div className="line-justify">
          {
            expand ? (
              <Icon
                role="none"
                style={{ 
                  position: 'absolute',
                  left: 5,
                  top: 15,
                }}
                type="baseline-arrow_drop_down pointer"
                onClick={() => {
                  this.setState({
                    expand: false,
                  });
                }}
              />
            ) : null
          }
          {
            !expand ? (
              <Icon
                role="none"
                style={{ 
                  position: 'absolute',
                  left: 5,
                  top: 15,
                }}
                type="baseline-arrow_right pointer"
                onClick={() => {
                  this.setState({
                    expand: true,
                  });
                }}
              />
            ) : null
          }
          <div className="c7n-title-commit" style={{ flex: 1 }}>
            <UserHead
              user={createUser}
              color="#3f51b5"
            />
            <div style={{ color: 'rgba(0, 0, 0, 0.65)', marginLeft: 15 }}>
              <Tooltip placement="top" title={lastUpdateDate || ''}>
                <TimeAgo
                  datetime={lastUpdateDate || ''}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              </Tooltip>
            </div>
          </div>
          <div className="c7n-action">
            {AppState.userInfo.id === userId
              ? (
                <Icon
                  role="none"
                  type="mode_edit mlr-3 pointer"
                  onClick={() => {
                    this.setState({
                      editCommentId: id,
                      editComment: content,
                      expand: true,
                    });
                  }}
                />
              ) : null}
            {AppState.userInfo.id === userId
              ? (
                <Popconfirm
                  title="确认要删除该评论吗?"
                  placement="left"
                  onConfirm={() => this.handleCommitDelete(id)}
                  onCancel={this.cancel}
                  okText="删除"
                  cancelText="取消"
                  okType="danger"
                >
                  <Icon
                    role="none"
                    type="delete_forever mlr-3 pointer"
                  />
                </Popconfirm>
              ) : (
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={orgId}
                  service={type === 'project' 
                    ? ['choerodon.code.project.cooperation.knowledge.ps.page_comment.delete']
                    : ['choerodon.code.organization.knowledge.ps.page_comment.delete']}
                >
                  <Popconfirm
                    title="确认要删除该评论吗?"
                    placement="left"
                    onConfirm={() => this.handleCommitDelete(id, 'admin')}
                    onCancel={this.cancel}
                    okText="删除"
                    cancelText="取消"
                    okType="danger"
                  >
                    <Icon
                      role="none"
                      type="delete_forever mlr-3 pointer"
                    />
                  </Popconfirm>
                </Permission>
              )}
          </div>
        </div>
        {
          expand && (
            <div className="c7n-comment-content" style={{ marginTop: 10 }}>
              {
                id === editCommentId ? (
                  <DocEditor
                    data={editComment}
                    comment
                    hideModeSwitch
                    initialEditType="wysiwyg"
                    height="300px"
                    onSave={this.handleCommentEdit}
                    onCancel={this.handleCancel}
                  />
                ) : (
                  <Viewer
                    initialValue={content}
                  />
                )
              }
            </div>
          )
        }
        
      </div>
    );
  }
}

export default Comment;
