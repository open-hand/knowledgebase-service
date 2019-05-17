import React, { Component } from 'react';
import './DocCatalog.scss';

class DocCatalog extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }

  render() {
    const { modes, children, defaultSize } = this.props;
    const { resizing, mode } = this.state;
    return (
      <div className="c7n-docCatalog">
        <span style={{ fontSize: 18, color: '#3F51B5' }}>目录</span>
      </div>
    );
  }
}

export default DocCatalog;
