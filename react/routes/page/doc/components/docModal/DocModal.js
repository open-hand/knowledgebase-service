import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Input, Modal, Button, Checkbox, Icon } from 'choerodon-ui';
import copy from 'copy-to-clipboard';
import { stores, Choerodon } from '@choerodon/boot';
import DocMove from '../../../../../components/DocMove';
import './index.less';

const { AppState } = stores;

@observer
class DocModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      uploading: false,
    };
  }

  handleCopy = () => {
    const shareInput = document.getElementById('shareUrl');
    if (shareInput && shareInput.value) {
      copy(shareInput.value);
      Choerodon.prompt('复制成功！');
    }
  };

  /**
   * 修改分享设置
   * @param mode
   */
  handleCheckChange = (mode) => {
    const { store } = this.props;
    const share = store.getShare;
    const { type: shareType, workspaceId, objectVersionNumber, id } = share || {};
    let newType = 'disabled';
    if (mode === 'share') {
      newType = shareType === 'disabled' ? 'current_page' : 'disabled';
    } else {
      newType = shareType === 'current_page' ? 'include_page' : 'current_page';
    }
    store.setShare({
      ...share,
      type: newType,
    });
    store.updateShare(id, workspaceId, {
      objectVersionNumber,
      type: newType,
    });
  };

  importWord = () => {
    this.uploadInput.click();
  };

  beforeUpload = (e) => {
    if (e.target.files[0]) {
      this.upload(e.target.files[0]);
    }
  };

  upload = (file) => {
    const { store } = this.props;
    if (!file) {
      Choerodon.prompt('请选择文件');
      return;
    }
    if (file.size > 1024 * 1024 * 10) {
      Choerodon.prompt('文件不能超过10M');
      return false;
    }
    const formData = new FormData();
    formData.append('file', file);
    this.setState({
      uploading: true,
    });
    store.importWord(formData).then((res) => {
      if (res.failed && res.code === 'error.importDocx2Md.fileIllegal') {
        Choerodon.prompt('导入失败，导入文件类型错误');
        this.setState({
          uploading: false,
        });
      } else {
        if (res.failed && res.code === 'Error reading from the stream (no bytes available)') {
          store.setImportDoc('');
        } else {
          store.setImportDoc(res.toString());
        }
        if (file.name) {
          const nameList = file.name.split('.');
          nameList.pop();
          store.setImportTitle(nameList.join());
        }
        this.setState({
          uploading: false,
        });
        store.setImportVisible(false);
        store.setImportMode(true);
      }
    }).catch((error) => {
      this.setState({
        uploading: false,
      });
      if (!(error.failed && error.code === 'error.import.word.empty')) {
        Choerodon.prompt('导入失败');
      }
    });
  };

  handleShareCancel = () => {
    const { store } = this.props;
    store.setShareVisible(false);
  };

  handleImportCancel = () => {
    const { store } = this.props;
    store.setImportVisible(false);
  };

  closeDocMove = () => {
    const { store } = this.props;
    store.setMoveVisible(false);
  };

  render() {
    const { uploading } = this.state;
    const { store, selectId, mode, refresh, handleLoadDraft, handleDeleteDraft } = this.props;
    const shareVisible = store.getShareVisible;
    const importVisible = store.getImportVisible;
    const moveVisible = store.getMoveVisible;
    const draftVisible = store.getDraftVisible;
    // 分享数据
    const share = store.getShare;
    const { type: shareType, token } = share || {};
    // 草稿
    const docData = store.getDoc;
    const draftTime = (docData && docData.createDraftDate) || '';

    return (
      <React.Fragment>
        {shareVisible
          ? (
            <Modal
              title="分享链接"
              visible={shareVisible}
              closable={false}
              footer={<Button onClick={this.handleShareCancel} funcType="flat">取消</Button>}
              maskClosable={false}
            >
              <div style={{ padding: '20px 0' }}>
                <FormattedMessage id="doc.share.tip" />
                <Checkbox
                  disabled={shareType === 'disabled'}
                  checked={shareType === 'include_page'}
                  onChange={() => this.handleCheckChange('type')}
                  className="c7n-kb-doc-checkBox"
                >
                  <FormattedMessage id="doc.share.include" />
                </Checkbox>
                <div className="c7n-kb-doc-input">
                  <Input
                    id="shareUrl"
                    label="分享链接"
                    value={`${window.location.origin}/#/knowledge/share/${token}`}
                  />
                  <Button onClick={this.handleCopy} type="primary" funcType="raised">
                    <FormattedMessage id="doc.share.copy" />
                  </Button>
                </div>
              </div>
            </Modal>
          ) : null
        }
        {importVisible
          ? (
            <Modal
              title="Word文档导入"
              visible={importVisible}
              closable={false}
              footer={<Button onClick={this.handleImportCancel} funcType="flat">取消</Button>}
              maskClosable={false}
            >
              <div style={{ paddingTop: 20 }}>
                <FormattedMessage id="doc.import.tip" />
                <div style={{ marginTop: 10 }}>
                  <Button
                    loading={uploading}
                    type="primary"
                    funcType="flat"
                    onClick={() => this.importWord()}
                    style={{ marginBottom: 2 }}
                  >
                    <Icon type="archive icon" />
                    <span>导入文档</span>
                  </Button>
                  <input
                    ref={
                      (uploadInput) => { this.uploadInput = uploadInput; }
                    }
                    type="file"
                    onChange={this.beforeUpload}
                    style={{ display: 'none' }}
                    accept=".docx"
                  />
                </div>
              </div>
            </Modal>
          ) : null
        }
        {moveVisible
          ? (
            <DocMove
              store={store}
              moveVisible={moveVisible}
              id={selectId}
              closeDocMove={this.closeDocMove}
              refresh={refresh}
            />
          ) : null
        }
        {draftVisible && mode === 'view'
          ? (
            <Modal
              title="提示"
              visible={draftVisible && mode === 'view'}
              closable={false}
              onOk={handleLoadDraft}
              onCancel={handleDeleteDraft}
              maskClosable={false}
              cancelText="删除草稿"
            >
              {`当前知识文档在 ${draftTime} 由你编辑后存为草稿，需要恢复草稿吗？`}
            </Modal>
          ) : null
        }
      </React.Fragment>
    );
  }
}

export default withRouter(injectIntl(DocModal));
