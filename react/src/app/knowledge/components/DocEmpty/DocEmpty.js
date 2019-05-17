import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import emptyPng from '../../assets/images/emptyDoc.svg';
import './DocEmpty.scss';

@inject('AppState')
@observer class DocEmpty extends Component {
  render() {
    return (
      <div className="c7n-docEmpty">
        <img className="c7n-docEmpty-img" alt="空doc" src={emptyPng} />
        <div className="c7n-docEmpty-text">
          <p className="c7n-docEmpty-title">没有任何页面</p>
          <p className="c7n-docEmpty-content">
            点击
            <span className="c7n-docEmpty-content-blue">创建新页面</span>
            开启你的知识管理。
          </p>
        </div>
      </div>
    );
  }
}

export default DocEmpty;
