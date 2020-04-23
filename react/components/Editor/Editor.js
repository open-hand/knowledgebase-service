import React, { Component } from 'react';
import { withRouter, Prompt } from 'react-router-dom';
import { Choerodon } from '@choerodon/boot';
import { Modal } from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import 'tui-color-picker/dist/tui-color-picker.min.css';

import 'tui-editor/dist/tui-editor-extScrollSync';
import 'tui-editor/dist/tui-editor-extColorSyntax';
import 'tui-editor/dist/tui-editor-extTable';
import { Editor as ToastEditor } from '@toast-ui/react-editor';
import uploadImage, { convertBase64UrlToBlob } from '../../utils';
import DocImageEditor from '../DocImageEditor';
import './Editor.scss';

const REFRESH_INTERVAL = 60 * 1000;

class Editor extends Component {
  constructor(props) {
    super(props);
    this.state = {
      imageEditorVisible: false,
      image: false,
      callback: false,
      changeCount: -1,
      saveLoading: false,
    };
    this.timer = null;
  }

  componentDidMount() {
    window.addEventListener('beforeunload', this.beforeClose);
    window.addEventListener('keydown', this.onKeyDown);
    this.editorRef.current.editorInst.focus();
    this.timer = setInterval(() => {
      this.handleSave('autoSave');
    }, REFRESH_INTERVAL);
    this.props.editorRef(this.editorRef);
    const { initialEditType } = this.props;
    if (initialEditType === 'wysiwyg') {
      // 这里是编辑器自己的bug，首次渲染如果是wysiwyg格式，渲染合并单元格会有bug，这里切换一下就可以显示正常
      this.editorRef.current.editorInst.changeMode('markdown');
      this.editorRef.current.editorInst.changeMode('wysiwyg');
    }
  }

  componentWillReceiveProps(nextProps) {
    nextProps.editorRef(this.editorRef);
  }

  componentWillUnmount() {
    window.removeEventListener('beforeunload', this.beforeClose);
    window.removeEventListener('keydown', this.onKeyDown);
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  editorRef = React.createRef();

  beforeClose = (e) => {
    // 已无法自定义提示信息，由浏览器通用确认信息代替
    const { changeCount } = this.state;
    if (changeCount === 1) {
      e.preventDefault();
      e.returnValue = '你正在编辑的内容尚未保存，确定离开吗？';
      return '你正在编辑的内容尚未保存，确定离开吗？';
    }
  };

  onKeyDown = (e) => {
    const keyCode = e.keyCode || e.which || e.charCode;
    const ctrlKey = e.ctrlKey || e.metaKey;
    if (ctrlKey && keyCode === 83) {
      e.preventDefault();
      this.handleSave('edit');
      return false;
    }
  };

  handleSave = (type) => {
    const { changeCount } = this.state;
    const { onSave, onChange, initialEditType } = this.props;
    // 保存后，清空更新次数
    if (type === 'autoSave') {
      // 有修改才自动保存
      if (onSave && changeCount === 1) {
        const md = this.editorRef.current.editorInst.getMarkdown();
        onSave(md, type);
      }
    } else {
      this.setState({
        changeCount: 0,
      });
      if (type === 'save') {
        this.setState({
          saveLoading: true,
        });
      }
      if (onChange) {
        onChange(false);
      }
      if (onSave) {
        const md = this.editorRef.current.editorInst.getMarkdown();
        const mode = this.editorRef.current.editorInst.currentMode;
        onSave(md, type, initialEditType === mode ? false : mode);
      }
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
      data, initialEditType = 'markdown', wrapperHeight = false,
      hideModeSwitch = false, height = '100%', onChange,
    } = this.props;
    const { imageEditorVisible, image, changeCount, saveLoading } = this.state;

    const toolbarItems = [
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

    return (
      <div className="c7n-docEditor" style={{ height: wrapperHeight || 'calc(100% - 49px)' }}>
        <ToastEditor
          toolbarItems={toolbarItems}
          hideModeSwitch={hideModeSwitch}
          usageStatistics={false}
          initialValue={data}
          previewStyle="vertical"
          height={height}
          initialEditType={initialEditType}
          useCommandShortcut={false}
          language="zh_CN"
          ref={this.editorRef}
          exts={[
            'scrollSync',
            'colorSyntax',
            'table',
            'attachment',
          ]}
          hooks={
            {
              // 图片上传的 hook
              addImageBlobHook: (file, callback) => {
                this.onPasteOrUploadIamge(file, callback);
              },
              change: (e) => {
                // 第一次渲染会默认触发change
                const { changeCount: count } = this.state;
                if (count <= 0) {
                  this.setState({
                    changeCount: count + 1,
                  });
                  // 通知父组件，文章被修改
                  if (count === 0) {
                    if (onChange) {
                      onChange(true);
                    }
                  }
                }
              },
            }
          }
        />
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
        <Prompt
          when={changeCount === 1}
          message={`编辑提示${Choerodon.STRING_DEVIDER}你这在编辑的内容尚未保存，确定离开吗？`}
        />
      </div>
    );
  }
}
export default withRouter(Editor);
