import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import './DocCatalog.scss';

@observer
class DocCatalog extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }

  componentDidMount() {
    this.loadCatalog();
    window.addEventListener('click', this.onCatalogClick);
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.onCatalogClick);
  }

  /**
   * 监听标题点击事件
   * @param e
   */
  onCatalogClick = (e) => {
    if (e.target.nodeName === 'LI') {
      const { children } = e.target;
      if (children.length && children[0].href && children[0].href.split('#').length === 2) {
        const eleId = e.target.children[0].href.split('#')[1];
        const scrollEle = document.getElementById(decodeURI(eleId));
        if (scrollEle) {
          scrollEle.scrollIntoView(true);
        }
      }
    }
  };

  loadCatalog = () => {
    const { store, mode, token } = this.props;
    if (mode === 'share') {
      const docData = store.getShareDoc;
      store.getCatalogByToken(docData.pageInfo.id, token);
    } else {
      const docData = store.getDoc;
      store.loadCatalog(docData.pageInfo.id);
    }
  };

  escape = str => str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');

  render() {
    const { store } = this.props;
    const catalog = store.getCatalog;
    return (
      <div className="c7n-docCatalog">
        {catalog
          ? (
            <div
              dangerouslySetInnerHTML={{ __html: this.escape(catalog) }}
            />
          ) : (
            <span style={{ fontSize: 18, color: '#3F51B5', fontWeight: 500 }}>目录</span>
          )
        }
      </div>
    );
  }
}

export default withRouter(injectIntl(DocCatalog));
