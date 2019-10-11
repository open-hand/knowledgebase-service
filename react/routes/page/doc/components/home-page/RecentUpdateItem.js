import React, { useEffect, useState } from 'react';
import TimeAgo from 'timeago-react';
import { Icon, Tooltip } from 'choerodon-ui/pro';


const prefix = 'home-page-item';

function RecentUpdateItem(props) {
  const { data, date, onClick } = props;
  const [expend, setExpend] = useState(true);

  function handleHeaderClick() {
    setExpend(!expend);
  }

  function renderHeader() {
    return (
      <div className={`${prefix}-header`}>
        <Icon className={`${prefix}-headerIcon`} type={expend ? 'expand_more' : 'navigate_next'} onClick={handleHeaderClick} />
        <span className={`${prefix}-headerDate`}>{date}</span>
        {`（${data ? data.length : 0}）`}
      </div>
    );
  }

  function handleTitleClick(id) {
    if (onClick) {
      onClick(id);
    }
  }

  function renderItemLine(record) {
    const {
      title,
      lastUpdatedUser: {
        loginName,
        realName,
        email,
        ldap,
      },
      lastUpdateDate,
      id,
    } = record;
    return (
      <div className={`${prefix}-body`}>
        <span className={`${prefix}-bodyLeft`}>
          <Icon className={`${prefix}-bodyIcon`} type="assignment" />
          <span className={`${prefix}-bodyTitle`} onClick={() => handleTitleClick(id)}>{title}</span>
        </span>
        <span className={`${prefix}-bodyRight`}>
          <Tooltip placement="top" title={ldap ? `${realName}（${loginName}）` : `${realName}（${email}）`}>
            <span className={`${prefix}-bodyUser`}>{realName || loginName}</span>
          </Tooltip>
          <Tooltip placement="top" title={lastUpdateDate || ''}>
            <TimeAgo
              className={`${prefix}-bodyDate`}
              datetime={lastUpdateDate}
              locale={Choerodon.getMessage('zh_CN', 'en')}
            />
          </Tooltip>
        </span>
      </div>
    );
  }

  function renderBody() {
    if (expend) {
      return data.map(renderItemLine);
    } else {
      return null;
    }
  }

  return (
    <div className={prefix}>
      {renderHeader()}
      {renderBody()}
    </div>
  );
}

export default RecentUpdateItem;
