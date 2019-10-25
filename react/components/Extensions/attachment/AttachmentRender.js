import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { stores, axios } from '@choerodon/boot';
import Viewer from 'tui-editor/dist/tui-editor-Viewer';
import Editor from 'tui-editor/dist/tui-editor-Editor';
import { randomString } from '../../../utils';

const { AppState } = stores;

class AttachmentRender extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  componentDidMount() {
    Viewer.defineExtension('attachment', this.attachmentViewerExtension);
    Editor.defineExtension('attachment', this.attachmentEditorExtension);
  }

  componentWillUnmount() {
  }

  renderAttachmentByName = (name) => {
    const { type, id, organizationId } = AppState.currentMenuType;
    const eleId = randomString(10);
    axios.get(`/knowledge/v1/${type}s/${id}/page_attachment/query_by_file_name?organizationId=${organizationId}&&fileName=${name.trim()}`).then((res) => {
      if (res && !res.failed) {
        const replaceEle = document.getElementById(eleId);
        replaceEle.innerHTML = `<a href=${res.url} target="_blank" rel="noopener noreferrer" title=${name.trim()}>${name.trim()}</a>`;
      }
    });
    return `<div id=${eleId}>${name.trim()}</div>`;
  };

  attachmentViewerExtension = () => {
    Viewer.codeBlockManager.setReplacer('attachment', attachmentName => this.renderAttachmentByName(attachmentName));
  };

  attachmentEditorExtension = () => {
    Editor.codeBlockManager.setReplacer('attachment', attachmentName => this.renderAttachmentByName(attachmentName));
  };

  render() {
    return ('');
  }
}
export default withRouter(AttachmentRender);
