import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Input, Modal, Button, Checkbox, Icon,
} from 'choerodon-ui';
import copy from 'copy-to-clipboard';
import DocMove from '../../../../../components/DocMove';

@observer
class DocModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      uploading: false,
    };
  }

  closeDocMove = () => {
    const { store } = this.props;
    store.setMoveVisible(false);
  }

  render() {
    const { uploading } = this.state;
    const { store, selectId, edit } = this.props;
    const moveVisible = store.getMoveVisible;
    const draftVisible = store.getDraftVisible;
    // 分享数据
    const share = store.getShare;
    const { type: shareType, token } = share || {};
    // 草稿
    const docData = store.getDoc;
    const draftTime = docData.createDraftDate || '';

    return (
      <>
        {moveVisible
          ? (
            <DocMove
              store={store}
              moveVisible={moveVisible}
              id={selectId}
              closeDocMove={this.closeDocMove}
            />
          ) : null}
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
              {`当前知识文档在 ${draftTime} 由你编辑后存为草稿，需要恢复草稿吗？`}
            </Modal>
          ) : null}
      </>
    );
  }
}

export default injectIntl(DocModal);
