import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import RecentUpdateItem from './RecentUpdateItem';

import './HomePage.less';

const prefix = 'home-page';

function HomePage(props) {
  const { pageStore, onClick } = props;
  const {
    getRecentUpdate: recentUpdate,
  } = pageStore;
  
  function renderRecentUpdateItem({ lastUpdateDateStr, workSpaceRecents }) {
    return <RecentUpdateItem key={lastUpdateDateStr} date={lastUpdateDateStr} data={workSpaceRecents} onClick={onClick} />;
  }

  function renderRecentUpdate() {
    if (recentUpdate) {
      if (recentUpdate.length) {
        return recentUpdate.map(renderRecentUpdateItem);
      } else {
        return <div className={`${prefix}-none`}>暂无数据</div>;
      }
    } else {
      return null;
    }
  }


  return (
    <div className={prefix}>
      <div className={`${prefix}-title`}>最近更新</div>
      {renderRecentUpdate()}
    </div>
  );
}

export default observer(HomePage);
