import React, { Component } from 'react';
import emptyPng from './assets/emptyDoc.svg';
import './DocEmpty.less';

export default function DocEmpty(props) {
  const { mode } = props;
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
        <p className="c7n-docEmpty-title">没有任何知识文档</p>
        <p className="c7n-docEmpty-content">
          点击
          <span className="c7n-docEmpty-content-blue">创建文档</span>
          按钮开启你的知识管理。
        </p>
      </div>
    </div>
  );
}
