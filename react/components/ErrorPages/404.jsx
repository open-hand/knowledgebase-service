import React, { Component } from 'react';
import './style/404.less';

export default class NoMatch extends Component {
  render() {
    return (
      <div className="c7n-knowledge-404-page">
        <div className="c7n-404-page-banner" />
        <div className="c7n-404-page-banner-text">
          <span>抱歉 ，您访问的页面不存在！</span>
        </div>
      </div>
    );
  }
}
