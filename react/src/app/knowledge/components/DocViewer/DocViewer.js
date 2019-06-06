import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import TimeAgo from 'timeago-react';
import {
  BackTop,
} from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Viewer } from '@toast-ui/react-editor';
import ImageViewer from 'react-viewer';
import 'react-viewer/dist/index.css';
import DocHeader from '../DocHeader';
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
    const { data } = this.props;
    return (
      <div className="c7n-docViewer">
        <DocHeader {...this.props} />
        <div className="c7n-docViewer-wrapper" id="docViewer-scroll">
          <div className="c7n-docViewer-content">
            <Viewer
              initialValue={data.pageInfo.content}
            />
          </div>
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
          <BackTop target={() => document.getElementById('docViewer-scroll')} />
        </div>
        {hasImageViewer
          ? (
            <ImageViewer
              key="image-viewer"
              visible={hasImageViewer}
              onClose={this.onViewerClose}
              images={[{ src: imgSrc, alt: '' }]}
              noNavbar
              scalable={false}
              noImgDetails
              changeable={false}
            />
          ) : ''
        }
      </div>
    );
  }
}
export default withRouter(DocViewer);
