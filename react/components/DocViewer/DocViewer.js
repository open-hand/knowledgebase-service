import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import TimeAgo from 'timeago-react';
import {
  BackTop,
} from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import '../Extensions/table/table';
import { Viewer } from '@toast-ui/react-editor';
import Lightbox from 'react-image-lightbox';
import DocHeader from '../DocHeader';
import DocAttachment from '../doc-attachment';
import DocComment from '../doc-comment';
import './DocViewer.scss';

class DocViewer extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      hasImageViewer: false,
      imgSrc: false,
    };
  }

  componentDidMount() {
    window.addEventListener('click', this.onImageClick);
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.onImageClick);
  }

  onImageClick = (e) => {
    const { hasImageViewer } = this.state;
    if (!hasImageViewer) {
      if (e && e.target && e.target.nodeName === 'IMG' && e.target.className === '') {
        this.setState({
          hasImageViewer: true,
          imgSrc: e.target.src,
        });
        e.stopPropagation();
      }
    }
  };

  onViewerClose = () => {
    this.setState({
      hasImageViewer: false,
      imgSrc: false,
    });
  };

  render() {
    const { hasImageViewer, imgSrc } = this.state;
    const { data, searchVisible = false, store, readOnly } = this.props;
    return (
      <div className="c7n-docViewer">
        <DocHeader {...this.props} breadcrumb={!searchVisible} />
        <div className="c7n-docViewer-wrapper" id="docViewer-scroll">
          <DocAttachment store={store} readOnly={readOnly} />
          <div className="c7n-docViewer-content">
            <Viewer
              initialValue={searchVisible ? data.pageInfo.highlightContent : data.pageInfo.content}
              usageStatistics={false}
              exts={[
                'table',
                'attachment',
              ]}
            />
          </div>
          <div className="c7n-docViewer-footer">
            <div>
              <span className="c7n-docViewer-mRight">创建者</span>
              <span className="c7n-docViewer-mRight">{data.pageInfo.createName}</span>
              {'（'}
              <TimeAgo
                datetime={data.pageInfo.creationDate}
                locale={Choerodon.getMessage('zh_CN', 'en')}
              />
              {'）'}
            </div>
            <div>
              <span className="c7n-docViewer-mRight">最近编辑</span>
              <span className="c7n-docViewer-mRight">{data.pageInfo.lastUpdatedName}</span>
              {'（'}
              <TimeAgo
                datetime={data.pageInfo.lastUpdateDate}
                locale={Choerodon.getMessage('zh_CN', 'en')}
              />
              {'）'}
            </div>
          </div>
          {!readOnly
            ? <DocComment data={data} store={store} />
            : null
          }
          <BackTop target={() => document.getElementById('docViewer-scroll')} />
        </div>
        {hasImageViewer
          ? (
            <Lightbox
              mainSrc={imgSrc}
              onCloseRequest={this.onViewerClose}
              imageTitle="images"
            />
          ) : ''
        }
      </div>
    );
  }
}
export default withRouter(DocViewer);
