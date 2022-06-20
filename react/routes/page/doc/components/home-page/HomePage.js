import React, { useEffect, useState, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui/pro';
import recentEmpty from '@/assets/image/recentempty.png';
import RecentUpdateItem from './RecentUpdateItem';
import useFormatMessage from '@/hooks/useFormatMessage';

import './HomePage.less';

const prefix = 'home-page';

function HomePage(props) {
  const { pageStore, onClick } = props;
  const {
    getRecentUpdate: recentUpdate,
  } = pageStore;
  const formatMessage = useFormatMessage('knowledge');

  function renderRecentUpdateItem({ lastUpdateDateStr, workSpaceRecents }) {
    return <RecentUpdateItem key={lastUpdateDateStr} date={lastUpdateDateStr} data={workSpaceRecents} onClick={onClick} />;
  }

  function renderRecentUpdate() {
    if (recentUpdate) {
      if (recentUpdate.length) {
        return recentUpdate.map(renderRecentUpdateItem);
      }
      return (
        <div className={`${prefix}-none`}>
          <img style={{ width: 190 }} src={recentEmpty} alt="" />
          <p>暂无最近更新的文档/文件</p>
        </div>
      );
    }
    return null;
  }

  const { hasNextPage } = pageStore.recentPagination;

  return (
    <div className={prefix}>
      <div className={`${prefix}-title`}>
        {formatMessage({ id: 'document.recent_updates' })}
      </div>
      {renderRecentUpdate()}
      {hasNextPage && <div><Button loading={pageStore.recentPagination.loading} onClick={pageStore.queryNextRecentUpdate}>加载更多</Button></div>}
    </div>
  );
}

export default observer(HomePage);
