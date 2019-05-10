import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Button, Divider,
} from 'choerodon-ui';
import './DocHeaser.scss';

class DocHeader extends Component {
  render() {
    const { data } = this.props;

    return (
      <div className="c7n-docHeader">
        <span className="c7n-docHeader-title">
          {data}
        </span>
        <span className="c7n-docHeader-control">
          <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-mode_edit" />
          </Button>
          <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-attach_file" />
          </Button>
          <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-chat_bubble_outline" />
          </Button>
          <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-insert_invitation" />
          </Button>
          <Button shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-delete" />
          </Button>
          <Divider type="vertical" />
          <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={this.handleEdit}>
            <i className="icon icon-format_indent_increase" />
          </Button>
        </span>
      </div>
    );
  }
}
export default withRouter(DocHeader);
