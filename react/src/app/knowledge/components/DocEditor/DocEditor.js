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

  handleSave = (type) => {
    const { onSave, data } = this.props;
    if (onSave) {
      const md = this.editorRef.current.editorInst.getMarkdown();
      onSave(data.workSpace.id, md, type);
    }
  };

  render() {
    const { onCancel, data } = this.props;

    return (
      <div className="c7n-docEditor">
        <Editor
          usageStatistics={false}
          initialValue={data && data.pageInfo.souceContent}
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
            onClick={() => this.handleSave('edit')}
          >
            <span>保存并继续</span>
          </Button>
          <Button
            className="c7n-docEditor-btn"
            funcType="raised"
            onClick={() => this.handleSave('save')}
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
