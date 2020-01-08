import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Icon, Input, Button } from 'choerodon-ui';
import CommentList from '../../routes/page/components/comment-list';
import './DocComment.less';

const { TextArea } = Input;

function DocAttachment(props) {
  const { data: { pageInfo }, store } = props;
  const [visible, setVisivble] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);
  const commentList = store.getCommentList;

  function handleClick() {
    setVisivble(!visible);
  }

  function handleCreateComment() {
    if (newComment && newComment.trim()) {
      setLoading(true);
      const comment = {
        pageId: pageInfo.id,
        comment: newComment.trim(),
      };
      store.createComment(comment).then(() => {
        setNewComment('');
        setLoading(false);
        setVisivble(true);
      });
    }
  }

  function handleCancelComment() {
    setNewComment('');
  }

  function handleTextChange(e) {
    setNewComment(e.target.value);
  }

  return (
    <div className="doc-comment">
      <div className="doc-comment-list" style={{ padding: '10px 0px' }}>
        <Icon
          style={{ marginLeft: 10, verticalAlign: 'top', marginRight: 5, cursor: 'pointer' }}
          onClick={handleClick}
          type={visible ? 'expand_less' : 'expand_more'}
        />
        {`评论 (${commentList.length})`}
        {visible
          ? <CommentList store={store} />
          : null
        }
      </div>
      <div style={{ marginTop: 15 }}>
        <TextArea value={newComment} onChange={handleTextChange} autosize={{ minRows: 2, maxRows: 6 }} />
        <div style={{ padding: '10px 0 10px 1px' }}>
          <Button
            type="primary"
            funcType="raised"
            style={{ marginRight: 10 }}
            onClick={handleCreateComment}
            loading={loading}
          >
            评论
          </Button>
          <Button funcType="raised" onClick={handleCancelComment}>取消</Button>
        </div>
      </div>
    </div>
  );
}

export default observer(DocAttachment);
