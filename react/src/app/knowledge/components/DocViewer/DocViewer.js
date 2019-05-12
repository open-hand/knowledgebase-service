import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Viewer } from '@toast-ui/react-editor';
import DocHeader from '../DocHeader';
import './DocViewer.scss';

class Hello extends Component {
  viewerRef = React.createRef();

  render() {
    const { data, onBtnClick, permission } = this.props;
    return (
      <div className="c7n-docViewer">
        <DocHeader data={data.title} onBtnClick={onBtnClick} permission={permission} />
        <Viewer
          ref={this.viewerRef}
          initialValue={data.content}
        />
      </div>
    );
  }
}
export default withRouter(Hello);
