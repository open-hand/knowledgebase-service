import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Button, Modal,
} from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import 'tui-color-picker/dist/tui-color-picker.min.css';

import 'tui-editor/dist/tui-editor-extScrollSync';
import 'tui-editor/dist/tui-editor-extColorSyntax';
import 'tui-editor/dist/tui-editor-extUML';
import 'tui-editor/dist/tui-editor-extChart';
import 'tui-editor/dist/tui-editor-extTable';

import { Editor } from '@toast-ui/react-editor';
import uploadImage from '../../api/FileApi';
import { convertBase64UrlToBlob } from '../../common/utils';
import DocImageEditor from '../DocImageEditor';
import './DocEditor.scss';

class DocEditor extends Component {
  constructor(props) {
    super(props);
    this.state = {
      imageEditorVisible: false,
      image: false,
      callback: false,
    };
  }

  editorRef = React.createRef();

  handleSave = (type) => {
    const { onSave } = this.props;
    if (onSave) {
      const md = this.editorRef.current.editorInst.getMarkdown();
      onSave(md, type);
    }
  };

  onPasteOrUploadIamge = (file, callback) => {
    this.setState({
      imageEditorVisible: true,
      image: file,
      callback,
    });
  };

  handleImageCancel = () => {
    this.setState({
      imageEditorVisible: false,
      image: false,
      callback: false,
    });
  };

  handleImageSave = (data) => {
    const { callback } = this.state;
    const formData = new FormData();
    formData.append('file', convertBase64UrlToBlob(data), 'blob.png');
    uploadImage(formData).then((res) => {
      if (res && !res.failed) {
        callback(res[0], 'image');
        this.handleImageCancel();
      }
    });
  };

  render() {
    const {
      onCancel, data, initialEditType = 'markdown',
      hideModeSwitch = false, height = 'calc(100% - 70px)',
      comment = false,
    } = this.props;
    const { imageEditorVisible, image } = this.state;

    let toolbarItems = [
      'heading',
      'bold',
      'italic',
      'strike',
      'divider',
      'hr',
      'quote',
      'divider',
      'ul',
      'ol',
      'task',
      'indent',
      'outdent',
      'divider',
      'table',
      'image',
      'link',
      'divider',
      'code',
      'codeblock',
    ];

    if (comment) {
      toolbarItems = [
        'heading',
        'bold',
        'italic',
        'strike',
        'hr',
        'quote',
        'task',
        'image',
        'link',
      ];
    }

    return (
      <div className="c7n-docEditor">
        <Editor
          toolbarItems={toolbarItems}
          hideModeSwitch={hideModeSwitch}
          usageStatistics={false}
          initialValue={data}
          previewStyle="vertical"
          height={height}
          initialEditType={initialEditType}
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
          hooks={
            {
              // 图片上传的 hook
              addImageBlobHook: (file, callback) => {
                this.onPasteOrUploadIamge(file, callback);
              },
            }
          }
        />
        {/* 底部按钮应该作为参数传入，考虑到目前其它不会使用，暂不修改 */}
        {comment
          ? (
            <div className="c7n-docEditor-comment-control">
              <Button
                className="c7n-docEditor-btn"
                type="primary"
                onClick={() => this.handleSave('comment')}
              >
                <span>保存</span>
              </Button>
              <Button
                type="primary"
                onClick={onCancel}
              >
                <span>取消</span>
              </Button>
            </div>
          ) : (
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
          )
        }
        {imageEditorVisible
          ? (
            <Modal
              visible={imageEditorVisible}
              width={1024}
              height={700}
              footer={null}
              closable={false}
            >
              <DocImageEditor
                data={image}
                onSave={this.handleImageSave}
                onCancel={this.handleImageCancel}
              />
            </Modal>
          )
          : null
        }
      </div>
    );
  }
}
export default withRouter(DocEditor);
