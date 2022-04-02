import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { injectIntl } from 'react-intl';
import {
  Modal,
} from 'choerodon-ui';

@observer
class DocModal extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    const { store, edit } = this.props;
    const draftVisible = store.getDraftVisible;
    // 草稿
    const docData = store.getDoc;
    const draftTime = docData.createDraftDate || '';

    return (
      <>
        {draftVisible && !edit
          ? (
            <Modal
              title="提示"
              visible={draftVisible && !edit}
              closable={false}
              onOk={this.handleLoadDraft}
              onCancel={this.handleDeleteDraft}
              maskClosable={false}
              cancelText="删除草稿"
            >
              {`当前知识文档在 ${draftTime} 由你修改后存为草稿，需要恢复草稿吗？`}
            </Modal>
          ) : null}
      </>
    );
  }
}

export default injectIntl(DocModal);
