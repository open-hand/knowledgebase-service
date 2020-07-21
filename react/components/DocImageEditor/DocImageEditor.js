import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import {
  Button,
} from 'choerodon-ui';
import 'tui-image-editor/dist/tui-image-editor.css';
import ImageEditor from '@toast-ui/react-image-editor';
import whiteTheme from './assets/white-theme';
import zh from './assets/locale_zh';
import './DocImageEditor.less';

class DocImageEditor extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      saveTitle: '插入图像',
    };
  }

  editorRef = React.createRef();

  handleSave = () => {
    this.setState({
      loading: true,
      saveTitle: '上传中',
    });
    const { onSave } = this.props;
    if (onSave) {
      const editorInstance = this.editorRef.current.getInstance();
      onSave(editorInstance.toDataURL());
    }
  };

  render() {
    const { onCancel, data } = this.props;
    const { loading, saveTitle } = this.state;

    return (
      <div className="c7n-docImageEditor">
        <ImageEditor
          ref={this.editorRef}
          includeUI={{
            loadImage: {
              path: URL.createObjectURL(data),
              name: data.name,
            },
            theme: whiteTheme,
            menu: ['shape', 'crop', 'flip', 'rotate', 'text', 'draw'], // mask, icon, filter
            initMenu: 'draw',
            menuBarPosition: 'right',
            locale: zh,
            uiSize: {
              height: '600px',
            },
          }}
          cssMaxHeight={500}
          cssMaxWidth={650}
          usageStatistics={false}
        />
        <div className="c7n-docImageEditor-control">
          <Button
            className="c7n-docImageEditor-btn"
            type="primary"
            funcType="raised"
            loading={loading}
            onClick={this.handleSave}
          >
            <span>{saveTitle}</span>
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
export default withRouter(DocImageEditor);
