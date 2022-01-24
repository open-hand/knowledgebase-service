import React, { Component } from 'react';
import useFormatMessage from '@/hooks/useFormatMessage';
import emptyPng from './assets/emptyDoc.svg';
import './DocEmpty.less';

export default function DocEmpty(props) {
  const { mode, handleCreateClick, readOnly } = props;
  const formatMessage = useFormatMessage('knowledge.document');
  if (mode === 'search') {
    return (
      <div className="c7n-docEmpty">
        <img className="c7n-docEmpty-img" alt="空doc" src={emptyPng} />
        <div className="c7n-docEmpty-text">
          <p className="c7n-docEmpty-title">没有搜索到相关知识文档</p>
          <p className="c7n-docEmpty-content">
            请尝试输入其他关键词查找
          </p>
        </div>
      </div>
    );
  }
  return (
    <div className="c7n-docEmpty">
      <img className="c7n-docEmpty-img" alt="空doc" src={emptyPng} />
      <div className="c7n-docEmpty-text">
        <p className="c7n-docEmpty-title">
          {formatMessage({ id: 'empty.title' })}
        </p>
        <p className="c7n-docEmpty-content">
          {formatMessage({ id: 'click' })}
          {!readOnly && handleCreateClick ? (
            <span
              className="c7n-docEmpty-content-blue"
              onClick={handleCreateClick}
              role="none"
            >
              {formatMessage({ id: 'create' })}
            </span>
          ) : (
            <span>{formatMessage({ id: 'create' })}</span>
          )}
          {formatMessage({ id: 'empty.des' })}
        </p>
      </div>
    </div>
  );
}
