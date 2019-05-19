import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Button, Modal,
} from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Editor } from '@toast-ui/react-editor';
import { uploadImage } from '../../api/FileApi';
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
    const { onSave, data } = this.props;
    if (onSave) {
      const md = this.editorRef.current.editorInst.getMarkdown();
      onSave(data.workSpace.id, md, type);
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
        callback(`http://minio.staging.saas.hand-china.com/knowledgebase-service/${res[0]}`, 'image');
        this.handleImageCancel();
      }
    });
  };

  render() {
    const { onCancel, data } = this.props;
    const { imageEditorVisible, image } = this.state;

    return (
      <div className="c7n-docEditor">
        <Editor
          usageStatistics={false}
          initialValue={data && data.pageInfo.souceContent}
          previewStyle="vertical"
          height="calc(100% - 130px)"
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
          hooks={
            {
              // 图片上传的 hook
              addImageBlobHook: (file, callback) => {
                this.onPasteOrUploadIamge(file, callback);
              },
            }
          }
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
