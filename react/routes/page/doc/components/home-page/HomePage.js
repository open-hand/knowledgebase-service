import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
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

  return (
    <div className={prefix}>
      <div className={`${prefix}-title`}>
        {formatMessage({ id: 'document.recent_updates' })}
      </div>
      {renderRecentUpdate()}
    </div>
  );
}

export default observer(HomePage);
