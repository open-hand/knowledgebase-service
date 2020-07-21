import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Modal, Table } from 'choerodon-ui';
import { escape } from '../../utils';
import './SearchList.less';

@observer
class SearchList extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
  }

  handleClickSearch = (id) => {
    const { onClickSearch, searchText } = this.props;
    if (onClickSearch) {
      onClickSearch(id, false, searchText);
    }
  };

  handleClearSearch = () => {
    const { onClearSearch } = this.props;
    if (onClearSearch) {
      onClearSearch();
    }
  };

  replaceMDKeyword = html => html
    .replace(/#|\*|\[|\]|`/g, '');

  renderTitle = (title) => {
    const { searchText } = this.props;
    return title.split(searchText).join(`<span class="c7n-searchList-redTitle">${searchText}</span>`);
  };

  renderList = () => {
    const { store, searchId } = this.props;
    const searchList = store.getSearchList;
    const list = [];
    searchList.forEach((item) => {
      list.push(
        <div
          key={item.pageId}
          className={`c7n-searchList-item${searchId === item.pageId ? ' c7n-searchList-item-selected' : ''}`}
          onClick={() => this.handleClickSearch(item.workSpaceId)}
        >
          <div className="c7n-searchList-title" title={item.title}>
            <span dangerouslySetInnerHTML={{ __html: this.renderTitle(item.title) }} />
          </div>
          <div className="c7n-searchList-content">
            <span dangerouslySetInnerHTML={{ __html: this.replaceMDKeyword(escape(item.highlightContent || '')) }} />
          </div>
        </div>,
      );
    });
    return list;
  };

  render() {
    return (
      <div className="c7n-searchList">
        <div className="c7n-searchList-header">
          <span className="c7n-searchList-header-title">搜索结果</span>
          <a onClick={this.handleClearSearch}>清除结果</a>
        </div>
        <div className="c7n-searchList-wrapper">
          {this.renderList()}
        </div>
      </div>
    );
  }
}

export default withRouter(injectIntl(SearchList));
