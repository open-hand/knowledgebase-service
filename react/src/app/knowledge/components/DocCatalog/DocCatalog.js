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
  }

  loadCatalog = () => {
    const { store } = this.props;
    const docData = store.getDoc;
    store.loadCatalog(docData.pageInfo.id);
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
            <span style={{ fontSize: 18, color: '#3F51B5' }}>目录</span>
          )
        }
      </div>
    );
  }
}

export default withRouter(injectIntl(DocCatalog));
