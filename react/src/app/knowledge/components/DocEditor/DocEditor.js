import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Button,
} from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Editor } from '@toast-ui/react-editor';
import './DocEditor.scss';

class DocEditor extends Component {
  editorRef = React.createRef();

  handleClickButton = () => {
    this.viewerRef.current.viewerInst.setMarkdown(this.editorRef.current.editorInst.getMarkdown());
  };

  render() {
    const { onSave, onSaveAndEdit, onCancel } = this.props;

    return (
      <div className="c7n-docEditor">
        <Editor
          usageStatistics={false}
          initialValue="hello react editor world!"
          previewStyle="vertical"
          height="600px"
          initialEditType="markdown"
          useCommandShortcut
          language="zh"
          ref={this.editorRef}
          exts={[
            {
              name: 'chart',
              minWidth: 100,
              maxWidth: 600,
              minHeight: 100,
              maxHeight: 300,
            },
            'scrollSync',
            'colorSyntax',
            'uml',
            'mark',
            'table',
          ]}
        />
        <div className="c7n-docEditor-control">
          <Button
            className="c7n-docEditor-btn"
            type="primary"
            funcType="raised"
            onClick={onSaveAndEdit}
          >
            <span>保存并继续</span>
          </Button>
          <Button
            className="c7n-docEditor-btn"
            funcType="raised"
            onClick={onSave}
          >
            <span>保存</span>
          </Button>
          <Button
            funcType="raised"
            onClick={onCancel}
          >
            <span>取消</span>
          </Button>
        </div>
      </div>
    );
  }
}
export default withRouter(DocEditor);
