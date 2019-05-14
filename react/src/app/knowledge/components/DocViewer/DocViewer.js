import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import DocHeader from '../DocHeader';
import './DocViewer.scss';

class Hello extends Component {
  escape = str => str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');

  render() {
    const { data, onBtnClick, permission } = this.props;
    return (
      <div className="c7n-docViewer">
        <DocHeader data={data && data.pageInfo.title} onBtnClick={onBtnClick} permission={permission} />
        <span dangerouslySetInnerHTML={{ __html: this.escape(data.pageInfo.content) }} />
      </div>
    );
  }
}
export default withRouter(Hello);
