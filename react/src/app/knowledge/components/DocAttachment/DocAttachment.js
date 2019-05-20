import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Icon, Upload, Button } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import './DocAttachment.scss';

@inject('AppState')
@observer class DocAttachment extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.loadFiles();
  }

  loadFiles = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadAttachment(docData.pageInfo.id);
  };

  /**
   * 上传附件
   * @param newFile
   */
  onChangeFileList = (newFile) => {
    const { store } = this.props;
    const fileList = store.getAttachment;
    const docData = store.getDoc;
    const config = {
      pageId: docData.pageInfo.id,
      versionId: docData.pageInfo.versionId,
    };
    // formData
    const formData = new FormData();
    formData.append('file', newFile);
    store.uploadFile(formData, config);
  };

  render() {
    const { store } = this.props;
    const fileList = store.getAttachment;

    const props = {
      action: '',
      multiple: true,
      beforeUpload: (file) => {
        if (file.size > 1024 * 1024 * 30) {
          Choerodon.prompt('文件不能超过30M');
          return false;
        } else if (fileList.length >= 10) {
          Choerodon.prompt('最多上传10个文件');
          return false;
        } else if (file.name && encodeURI(file.name).length > 210) {
          Choerodon.prompt('文件名过长，建议不超过20个字');
          return false;
        } else {
          this.onChangeFileList(file);
        }
        return false;
      },
      onRemove: (file) => {
        const index = fileList.indexOf(file);
        const newFileList = fileList.slice();
        if (file.url) {
          store.deleteFile(file.uid);
        } else {
          newFileList.splice(index, 1);
          store.setAttachment(newFileList);
        }
      },
    };

    return (
      <div className="c7n-docAttachment" id="attachment">
        <div className="c7n-title-wrapper">
          <div className="c7n-title-left">
            <Icon type="attach_file c7n-icon-title" />
            <span>附件</span>
          </div>
          <div style={{
            flex: 1, height: 1, borderTop: '1px solid rgba(0, 0, 0, 0.08)', marginLeft: '14px', marginRight: 114.67,
          }}
          />
        </div>
        <div className="c7n-content-wrapper" style={{ marginTop: '-47px', justifyContent: 'flex-end' }}>
          <Upload
            {...props}
            fileList={fileList}
            className="upload-button"
          >
            <Button type="primary" funcType="flat">
              <Icon type="file_upload" />
              {'上传附件'}
            </Button>
          </Upload>
        </div>
      </div>
    );
  }
}

export default withRouter(injectIntl(DocAttachment));
