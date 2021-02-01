import React, { useState } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Icon, Popconfirm, Input, Button,
} from 'choerodon-ui';
import { stores, Permission, Choerodon } from '@choerodon/boot';
import TimeAgo from 'timeago-react';
import { Tooltip } from 'choerodon-ui/pro/lib';
import UserHead from '../../../../components/UserHead';
import './CommentList.less';

const { AppState } = stores;
const { TextArea } = Input;

function CommentList(props) {
  const { store } = props;
  const menu = AppState.currentMenuType;
  const { type, id: projectId, organizationId: orgId } = menu;
  const [editCommentId, setEditCommentId] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);
  const { getCommentList: commentList, getDoc: { pageInfo } } = store;

  function handleTextChange(e) {
    setNewComment(e.target.value);
  }

  function handleEditClick(comment) {
    setEditCommentId(comment.id);
    setNewComment(comment.comment);
  }

  function handleCancelEdit() {
    setEditCommentId(false);
    setNewComment('');
  }

  function handleEditComment(comment) {
    setLoading(true);
    const vo = {
      pageId: pageInfo.id,
      comment: newComment,
      objectVersionNumber: comment.objectVersionNumber,
    };
    store.editComment(comment.id, vo).then(() => {
      setLoading(false);
      handleCancelEdit();
    });
  }

  function handleCommentDelete(id, mode) {
    if (mode === 'admin') {
      store.adminDeleteComment(id);
    } else {
      store.deleteComment(id);
    }
  }

  function renderComment(comment) {
    const { id, createUser, lastUpdateDate } = comment;
    const userId = createUser.id;
    return (
      <div key={id} className="c7n-kb-commentItem">
        {id === editCommentId
          ? (
            <div key={id} className="c7n-kb-commentEdit">
              <TextArea value={newComment} onChange={handleTextChange} autosize={{ minRows: 2, maxRows: 6 }} />
              <div style={{ marginTop: 10, marginRight: 5 }}>
                <Button
                  type="primary"
                  funcType="raised"
                  style={{ marginRight: 10 }}
                  onClick={() => handleEditComment(comment)}
                  loading={loading}
                  disabled={!newComment}
                >
                  评论
                </Button>
                <Button funcType="raised" onClick={handleCancelEdit}>取消</Button>
              </div>
            </div>
          ) : (
            <span>
              <div className="c7n-kb-commentItem-header">
                <div className="c7n-kb-commentItem-msg">
                  <UserHead
                    size={30}
                    user={createUser}
                    color="#000"
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
                <div className="c7n-kb-commentItem-action">
                  {AppState.userInfo.id === userId
                    ? (
                      <Icon
                        role="none"
                        type="mode_edit mlr-3 pointer"
                        onClick={() => handleEditClick(comment)}
                      />
                    ) : null}
                  {AppState.userInfo.id === userId
                    ? (
                      <Popconfirm
                        title="确认要删除该评论吗?"
                        placement="left"
                        onConfirm={() => handleCommentDelete(id)}
                        okText="删除"
                        cancelText="取消"
                        okType="danger"
                        getPopupContainer={(triggerNode) => triggerNode.parentNode}
                      >
                        <Icon
                          id="page-comment-delete"
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
                          onConfirm={() => handleCommentDelete(id, 'admin')}
                          okText="删除"
                          cancelText="取消"
                          okType="danger"
                          getPopupContainer={(triggerNode) => triggerNode.parentNode}
                        >
                          <Icon
                            id="page-comment-delete"
                            role="none"
                            type="delete_forever mlr-3 pointer"
                          />
                        </Popconfirm>
                      </Permission>
                    )}
                </div>
              </div>
              <div className="c7n-kb-commentItem-content">
                {comment.comment}
              </div>
            </span>
          )}
      </div>
    );
  }

  return (
    <div className="c7n-kb-commentList">
      {commentList ? commentList.map((comment) => renderComment(comment)) : null}
    </div>
  );
}

export default withRouter(CommentList);
