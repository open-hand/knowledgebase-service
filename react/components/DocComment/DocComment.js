import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Icon, Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import Comment from './components/Comment';
import DocEditor from '../Editor';
import './DocComment.less';

@inject('AppState')
@observer class DocComment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      addComment: false,
    };
  }

  componentDidMount() {
    // 加载附件
    this.loadCommentList();
  }

  loadCommentList = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadComment(docData.pageInfo.id);
  };

  handleCreateComment = (data) => {
    if (data && data.trim()) {
      const { store } = this.props;
      const docData = store.getDoc;
      const comment = {
        pageId: docData.pageInfo.id,
        comment: data,
      };
      store.createComment(comment).then(() => {
        this.setState({ addComment: false });
      });
    }
  };

  handleDeleteComment = (id, mode) => {
    const { store } = this.props;
    if (mode === 'admin') {
      store.adminDeleteComment(id);
    } else {
      store.deleteComment(id);
    }
  };

  handleEditComment = (comment, data) => {
    const { store } = this.props;
    const docData = store.getDoc;
    const newComment = {
      pageId: docData.pageInfo.id,
      comment: data,
      objectVersionNumber: comment.objectVersionNumber,
    };
    store.editComment(comment.id, newComment);
  };

  renderCommits() {
    const { store } = this.props;
    const commentList = store.getComment;
    const { addComment } = this.state;
    return (
      <div>
        {
          addComment && (
            <div style={{ width: '100%' }}>
              <DocEditor
                comment
                hideModeSwitch
                initialEditType="wysiwyg"
                height="250px"
                onSave={this.handleCreateComment}
                onCancel={() => this.setState({ addComment: false })}
              />
            </div>
          )
        }
        {
          commentList && commentList.map(comment => (
            <Comment
              key={comment.id}
              comment={comment}
              onCommentDelete={this.handleDeleteComment}
              onCommentEdit={this.handleEditComment}
            />
          ))
        }
      </div>
    );
  }

  render() {
    return (
      <div id="comment" className="c7n-docComment">
        <div className="c7n-head-wrapper">
          <div className="c7n-head-left">
            <Icon type="sms_outline c7n-icon-title" />
            <FormattedMessage id="doc.comment" />
          </div>
          <div
            style={{
              flex: 1, height: 1, borderTop: '1px solid rgba(0, 0, 0, 0.08)', marginLeft: '14px',
            }}
          />
          <div className="c7n-head-right">
            <Button className="leftBtn" type="primary" funcType="flat" onClick={() => this.setState({ addComment: true })}>
              <Icon type="playlist_add icon" />
              <FormattedMessage id="doc.comment.create" />
            </Button>
          </div>
        </div>
        {this.renderCommits()}
      </div>
    );
  }
}

export default withRouter(injectIntl(DocComment));
