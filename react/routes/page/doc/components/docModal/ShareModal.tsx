import React, { useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import {
  CheckBox, TextField, Button, Modal,
} from 'choerodon-ui/pro';
// @ts-ignore
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import copy from 'copy-to-clipboard';
import PageStore from '@/routes/page/stores/PageStore';
import './ShareModal.less';

interface Props {
  store: PageStore
}

const Share: React.FC<Props> = ({ store }) => {
  const share = store.getShare;
  const {
    // @ts-ignore
    type: shareType, token, workspaceId, objectVersionNumber, id,
  } = share || {};

  /**
   * 修改分享设置
   * @param mode
   */
  const handleCheckChange = useCallback((mode) => {
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
  }, [id, objectVersionNumber, share, shareType, store, workspaceId]);

  const handleCopy = useCallback(() => {
    const shareInput = document.getElementById('shareUrl');
    // @ts-ignore
    if (shareInput && shareInput.value) {
      // @ts-ignore
      copy(shareInput.value);
      Choerodon.prompt('复制成功！');
    }
  }, []);
  return (
    <div className="c7n-kb-doc-shareModal">
      <span><FormattedMessage id="doc.share.tip" /></span>
      <div className="c7n-kb-doc-input">
        <TextField
          id="shareUrl"
          label="分享链接"
          value={`${window.location.origin}/#/knowledge/share/${token}`}
        />
        <Button onClick={handleCopy}>
          <FormattedMessage id="doc.share.copy" />
        </Button>
      </div>
      <CheckBox
        disabled={shareType === 'disabled'}
        checked={shareType === 'include_page'}
        onChange={() => handleCheckChange('type')}
        className="c7n-kb-doc-checkBox"
      >
        <FormattedMessage id="doc.share.include" />
      </CheckBox>
    </div>
  );
};

// @ts-ignore
const ObserverShare = injectIntl(observer(Share));

const openShare = (props: Props) => {
  Modal.open({
    title: '分享链接',
    children: <ObserverShare {...props} />,
    okText: '取消',
    footer: (okBtn: any, cancelBtn: any) => okBtn,
  });
};

export default openShare;
