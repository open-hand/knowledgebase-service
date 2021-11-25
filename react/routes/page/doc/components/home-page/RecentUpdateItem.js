import React, { useEffect, useState, memo } from 'react';
import { Choerodon } from '@choerodon/boot';
import { Icon, Tooltip } from 'choerodon-ui/pro';
import useFormatMessage from '@/hooks/useFormatMessage';
import UserHead from '../../../../../components/UserHead';

const prefix = 'home-page-item';

function RecentUpdateItem(props) {
  const { data, date, onClick } = props;
  const [expend, setExpend] = useState(true);
  const formatMessage = useFormatMessage('knowledge.document');

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
      lastUpdatedUser,
      lastUpdateDate,
      id,
      knowledgeBaseName,
    } = record;
    const type = 'doc';
    return (
      <div className={`${prefix}-body`}>
        <span className={`${prefix}-bodyLeft`}>
          <div className={`${prefix}-bodyIcon ${type}`}>
            <Icon type="description" />
          </div>
          <div className={`${prefix}-bodyInfo`}>
            <div className={`${prefix}-bodyTitle`} onClick={() => handleTitleClick(id)}>{title}</div>
            <div className={`${prefix}-bodyRepo`}>{knowledgeBaseName}</div>
          </div>
        </span>
        <span className={`${prefix}-bodyRight`}>
          <UserHead user={lastUpdatedUser} style={{ maxWidth: 250 }} />
          <Tooltip placement="top" title={lastUpdateDate || ''}>
            <span className={`${prefix}-bodyDate`}>
              {formatMessage({ id: 'update_time' })}
              ：
              {lastUpdateDate}
            </span>

          </Tooltip>
        </span>
      </div>
    );
  }

  function renderBody() {
    if (expend) {
      return data.map(renderItemLine);
    }
    return null;
  }

  return (
    <div className={prefix}>
      {renderHeader()}
      {renderBody()}
    </div>
  );
}

export default RecentUpdateItem;
