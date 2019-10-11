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
  
  function renderRecentUpdateItem(key) {
    return <RecentUpdateItem key={key} date={key} data={recentUpdate[key]} onClick={onClick} />;
  }

  function renderRecentUpdate() {
    if (recentUpdate) {
      const keyList = Object.keys(recentUpdate);
      if (keyList.length) {
        return keyList.map(renderRecentUpdateItem);
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
