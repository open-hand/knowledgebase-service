import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { withRouter } from 'react-router-dom';
import {
  Modal,
} from 'choerodon-ui';
import './index.less';

@observer
class DocModal extends Component {
  render() {
    const {
      store, mode, handleLoadDraft, handleDeleteDraft,
    } = this.props;
    const draftVisible = store.getDraftVisible;
    // 草稿
    const docData = store.getDoc;
    const draftTime = (docData && docData.createDraftDate) || '';

    return (
      <>
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
              className="c7n-draft-modal"
            >
              {`当前知识文档在 ${draftTime} 由你编辑后存为草稿，需要恢复草稿吗？`}
            </Modal>
          ) : null}
      </>
    );
  }
}

export default withRouter(injectIntl(DocModal));
