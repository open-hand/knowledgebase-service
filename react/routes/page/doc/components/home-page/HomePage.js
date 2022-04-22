import React, { useEffect, useState, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui/pro';
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
          {formatMessage({ id: 'common.no_data' })}
        </div>
      );
    }
    return null;
  }

  const { hasNextPage } = pageStore.recentPagination;
  console.log('pageStore.recentPagination.loading', pageStore.recentPagination.loading);

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
