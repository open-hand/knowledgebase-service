import React, { useContext, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { Icon, Input, Button } from 'choerodon-ui';
import CommentList from '../../routes/page/components/comment-list';

const { TextArea } = Input;

function DocAttachment(props) {
  const { data: { pageInfo }, store } = props;
  const [visible, setVisivble] = useState(true);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);
  const commentList = store.getCommentList;

  function handleClick() {
    setVisivble(!visible);
  }

  function handleCreateComment() {
    setLoading(true);
    const comment = {
      pageId: pageInfo.id,
      comment: newComment,
    };
    store.createComment(comment).then(() => {
      setNewComment('');
      setLoading(false);
    });
  }

  function handleCancelComment() {
    setNewComment('');
  }

  function handleTextChange(e) {
    setNewComment(e.target.value);
  }

  return (
    <div>
      <div style={{ padding: '10px 0px', borderBottom: '1px solid #d8d8d8' }}>
        <Icon
          style={{ marginLeft: 10, verticalAlign: 'top', marginRight: 5, cursor: 'pointer' }}
          onClick={handleClick}
          type={visible ? 'expand_less' : 'expand_more'}
        />
        {`回复 (${commentList.length})`}
        {visible
          ? <CommentList store={store} />
          : null
        }
      </div>
      <div style={{ marginTop: 15 }}>
        <TextArea value={newComment} onChange={handleTextChange} />
        <div style={{ marginTop: 10, float: 'right', marginRight: 5 }}>
          <Button
            type="primary"
            funcType="raised"
            style={{ marginRight: 10 }}
            onClick={handleCreateComment}
            loading={loading}
            disabled={!newComment}
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
