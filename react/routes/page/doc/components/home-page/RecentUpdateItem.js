import React, { useEffect, useState, memo } from 'react';
import { Choerodon } from '@choerodon/boot';
import { Icon, Tooltip } from 'choerodon-ui/pro';
import folder from '@/assets/image/folder.svg';
import document from '@/assets/image/document.svg';
import xlsx from '@/assets/image/xlsx.svg';
import doc from '@/assets/image/word.svg';
import other from '@/assets/image/mp4.svg';
import pdf from '@/assets/image/pdf.svg';
import ppt from '@/assets/image/ppt.svg';
import txt from '@/assets/image/txt.svg';
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

  const renderHeader = () => (
    <div className={`${prefix}-header`}>
      {/* eslint-disable-next-line react/jsx-no-bind */}
      <Icon className={`${prefix}-headerIcon`} type={expend ? 'expand_more' : 'navigate_next'} onClick={handleHeaderClick} />
      <span className={`${prefix}-headerDate`}>{date}</span>
      {`（${data ? data.length : 0}）`}
    </div>
  );

  function handleTitleClick(id) {
    if (onClick) {
      onClick(id);
    }
  }

  const getImage = (type, name) => {
    switch (type) {
      case 'folder': {
        return folder;
        break;
      }
      case 'document': {
        return document;
        break;
      }
      case 'file': {
        if (name.includes('xlsx')) {
          return xlsx;
        } if (name.includes('doc')) {
          return doc;
        } if (name.includes('pdf')) {
          return pdf;
        } if (name.includes('ppt')) {
          return ppt;
        } if (name.includes('txt')) {
          return txt;
        }
        return other;
        break;
      }
      default: {
        return other;
        break;
      }
    }
  };

  function renderIcons(record) {
    switch (record.type) {
      case 'document': {
        return <Icon type="description" />;
      }
      case 'file': {
        return (
          <img src={getImage(record.type, record.title)} alt="" />
        );
      }
      default: {
        return '';
      }
    }
  }

  function renderPath(path) {
    if (path && path?.length > 0) {
      if (path?.length <= 5) {
        return path?.join('/');
      }
      const str = path.slice(0, 4).join('/');
      const lastone = path[path.length - 1];
      return (
        <Tooltip title={path?.join('/')}>
          <span>
            {`${str}/.../${lastone}`}
          </span>
        </Tooltip>
      );
    }
    return '/';
  }

  function renderItemLine(record) {
    const {
      title,
      lastUpdatedUser,
      lastUpdateDate,
      id,
      knowledgeBaseName,
      parentPath,
    } = record;
    console.log(record);
    const type = 'doc';
    return (
      <div className={`${prefix}-body`}>
        <span className={`${prefix}-bodyLeft`}>
          <div className={`${prefix}-bodyIcon ${type}`}>
            {
              renderIcons(record)
            }
          </div>
          <div className={`${prefix}-bodyInfo`}>
            <div role="none" className={`${prefix}-bodyTitle`} onClick={() => handleTitleClick(id)}>{title}</div>
            <div className={`${prefix}-bodyRepo`}>
              {renderPath(parentPath)}
            </div>
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
