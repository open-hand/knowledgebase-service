import React, {
  useCallback, useMemo, useState,
} from 'react';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import {
  Button, Switch, CheckBox,
} from 'choerodon-ui/pro';
import { Popover, Input } from 'choerodon-ui';
import copy from 'copy-to-clipboard';
import PageStore from '@/routes/page/stores/PageStore';
import useFormatMessage from '@/hooks/useFormatMessage';

import Styles from './index.less';

interface Props {
  store: PageStore
  disabled: boolean,
  hasText?: boolean,
}

const ShareDoc: React.FC<Props> = ({ store, disabled, hasText = false }) => {
  // @ts-ignore
  const formatMessage = useFormatMessage('knowledge.document');
  const shareUrl = useMemo(() => {
    if (store.getShare) {
      // @ts-ignore
      return `${window.location.origin}/#/knowledge/share/${store.getShare.token}`;
    }
    return '';
  }, [store, store.getShare]);

  const loadData = useCallback(async () => {
    // @ts-ignore
    const { workSpace } = store.getDoc || {};
    if (workSpace?.id) {
      await store.queryShareMsg(workSpace.id);
    }
  }, [store]);

  /**
   * 修改分享设置
   * @param mode
   */
  const handleCheckChange = useCallback(async (mode: 'type' | 'share', value: boolean) => {
    const {
      // @ts-ignore
      objectVersionNumber, id, workspaceId,
    } = store.getShare || {};
    const newType = value ? 'include_page' : 'current_page';
    await store.updateShare(id, workspaceId, {
      objectVersionNumber,
      type: mode === 'type' ? newType : undefined,
      enabled: mode === 'share' ? value : undefined,
    });
  }, [store]);

  const handleCopy = useCallback(() => {
    if (shareUrl) {
      copy(shareUrl);
      Choerodon.prompt('复制成功！');
    }
  }, [shareUrl]);

  const renderContent = () => (
    <div className={Styles.content}>
      <Input
        className={Styles.content_link}
        value={shareUrl}
        suffix={(() => (
          <div
            className={Styles.content_link_suffix}
            onClick={handleCopy}
            role="none"
          >
            {formatMessage({ id: 'share_copy' })}
          </div>
        ))()}
      />
      {/* @ts-ignore */}
      <CheckBox
        // @ts-ignore
        checked={store.getShare?.type === 'include_page'}
        onChange={(value) => handleCheckChange('type', value)}
        className={Styles.content_checkbox}
      >
        {formatMessage({ id: 'share_include' })}
      </CheckBox>
    </div>
  );

  const renderTitle = () => (
    <div className={Styles.title}>
      <div className={Styles.title_text}>
        <span>
          {formatMessage({ id: 'share_title' })}
        </span>
        <span className={Styles.title_text_des}>
          {formatMessage({ id: 'share_des' })}
        </span>
      </div>
      {/* @ts-ignore */}
      <Switch
        className={Styles.title_switch}
        // @ts-ignore
        checked={store.getShare && store.getShare.enabled}
        onChange={(value) => handleCheckChange('share', value)}
      />
      <div className="c7n-knowledge-doc-share-arrow" />
    </div>
  );

  if (disabled) {
    return (
      <Button
        icon="share"
        disabled
      />
    );
  }

  return (
    <Popover
      overlayClassName={Styles.shareOverlay}
      trigger="click"
      content={renderContent()}
      title={renderTitle()}
      placement="bottomRight"
      visible={store.getShareVisible}
      onVisibleChange={(visible) => store.setShareVisible(visible)}
    >
      <Button
        icon="share"
        onClick={loadData}
      >
        { hasText && '分享' }
      </Button>
    </Popover>
  );
};

ShareDoc.defaultProps = {
  hasText: false,
};

export default observer(ShareDoc);
