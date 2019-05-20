import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import TimeAgo from 'timeago-react';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import DocHeader from '../DocHeader';
import './DocViewer.scss';

class DocViewer extends Component {
  escape = str => str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');

  render() {
    const { data, onBtnClick, permission, onTitleEdit } = this.props;
    return (
      <div className="c7n-docViewer">
        <DocHeader
          onTitleEdit={onTitleEdit}
          data={data && data.pageInfo.title}
          onBtnClick={onBtnClick}
          permission={permission}
        />
        <div
          className="c7n-docViewer-content"
          dangerouslySetInnerHTML={{ __html: this.escape(data.pageInfo.content) }}
        />
        <div className="c7n-docViewer-footer">
          <div className="c7n-docViewer-mBottom">
            <span className="c7n-docViewer-mRight">创建者</span>
            <span className="c7n-docViewer-mRight">{data.createName}</span>
            {'（'}
            <TimeAgo
              datetime={data.creationDate}
              locale={Choerodon.getMessage('zh_CN', 'en')}
            />
            {'）'}
          </div>
          <div>
            <span className="c7n-docViewer-mRight">编辑者</span>
            <span className="c7n-docViewer-mRight">{data.lastUpdatedName}</span>
            {'（'}
            <TimeAgo
              datetime={data.lastUpdateDate}
              locale={Choerodon.getMessage('zh_CN', 'en')}
            />
            {'）'}
          </div>
        </div>
      </div>
    );
  }
}
export default withRouter(DocViewer);
