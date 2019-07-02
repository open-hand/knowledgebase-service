import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import Viewer from 'tui-editor/dist/tui-editor-Viewer';

class AttachmentRender extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  componentDidMount() {
    Viewer.defineExtension('attachment', this.attachmentViewerExtension);
  }

  componentWillUnmount() {
  }

  getUrlByName = (name) => {
  };

  renderT = (attachmentName) => {
    setTimeout(() => {
      const sss = document.getElementById('123');
      sss.innerHTML = `<a href="http://www.baidu.com/" target="_blank" rel="noopener noreferrer" title=${attachmentName.trim()}>${attachmentName.trim()}</a>`;
    }, 1000);
    return '<div id="123"></div>';
  };

  attachmentViewerExtension = () => {
    Viewer.codeBlockManager.setReplacer('attachment', attachmentName => this.renderT(attachmentName));
  };

  render() {
    return ('');
  }
}
export default withRouter(AttachmentRender);
