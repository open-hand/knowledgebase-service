import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button, Divider, Tooltip, Icon, Input, Dropdown, Menu, Breadcrumb,
} from 'choerodon-ui';
import { stores, Permission, axios } from '@choerodon/boot';
import './DocHeader.less';

const { AppState } = stores;

@observer
class DocHeader extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  handleBreadcrumbClick = (id) => {
    const { onBreadcrumbClick } = this.props;
    if (onBreadcrumbClick) {
      onBreadcrumbClick(id);
    }
  };

  renderBreadcrumb = () => {
    const { data, spaceData, store } = this.props;
    const breadcrumbRead = store.getSearchVisible || store.getFullScreen;
    const parentIds = data.route && data.route.split('.').filter(item => spaceData.items[item]);
    const itemClass = `c7n-docHeader-breadcrumb-item${breadcrumbRead || parentIds.length === 1 ? ' c7n-docHeader-breadcrumb-item-read' : ''}`;
    const breadcrumb = [];
    if (parentIds.length && parentIds.length > 3) {
      breadcrumb.push(
        <Breadcrumb.Item key={parentIds[0]}>
          <span
            className={itemClass}
            onClick={() => this.handleBreadcrumbClick(parentIds[0])}
          >
            {spaceData.items[parentIds[0]].data.title}
          </span>
        </Breadcrumb.Item>,
      );
      breadcrumb.push(
        <Breadcrumb.Item key={parentIds[1]}>
          <span
            className={itemClass}
            onClick={() => this.handleBreadcrumbClick(parentIds[1])}
          >
            {spaceData.items[parentIds[1]].data.title}
          </span>
        </Breadcrumb.Item>,
      );
      breadcrumb.push(
        <Breadcrumb.Item key="more">
          <span className="c7n-docHeader-breadcrumb-item c7n-docHeader-breadcrumb-more">...</span>
        </Breadcrumb.Item>,
      );
      breadcrumb.push(
        <Breadcrumb.Item key={parentIds[parentIds.length - 1]}>
          <span
            className="c7n-docHeader-breadcrumb-item c7n-docHeader-breadcrumb-item-read"
          >
            {spaceData.items[parentIds[parentIds.length - 1]].data.title}
          </span>
        </Breadcrumb.Item>,
      );
    } else {
      parentIds.forEach((item, index) => {
        console.log(item);
        console.log(spaceData.items[item]);
        breadcrumb.push(
          <Breadcrumb.Item key={item}>
            <span
              className={`${itemClass}${(index + 1) === parentIds.length ? ' c7n-docHeader-breadcrumb-item-read' : ''}`}
              onClick={() => this.handleBreadcrumbClick(item)}
            >
              {spaceData.items[item] && spaceData.items[item].data.title}
            </span>
          </Breadcrumb.Item>,
        );
      });
    }
    return breadcrumb;
  };

  handleCatalogChange = () => {
    const { store } = this.props;
    const { getCatalogVisible: catalogVisible } = store;
    store.setCatalogVisible(!catalogVisible);
  };

  render() {
    const { store, onBtnClick, breadcrumb = true } = this.props;
    const { getCatalogVisible: catalogVisible } = store;

    return (
      <div className="c7n-docHeader">
        <div className="c7n-docHeader-left">
          {breadcrumb
            ? (
              <div className="c7n-docHeader-breadcrumb">
                <Breadcrumb separator=">">
                  {this.renderBreadcrumb()}
                </Breadcrumb>
              </div>
            ) : null}
        </div>
        {!catalogVisible
          ? (
            <div className="c7n-docHeader-right">
              <span className="c7n-docHeader-control">
                <Tooltip placement="top" title={<FormattedMessage id="docHeader.catalog.open" />}>
                  <Button shape="circle" size="small" onClick={this.handleCatalogChange}>
                    <i className="icon icon-format_indent_decrease" />
                  </Button>
                </Tooltip>
              </span>
            </div>
          ) : null}
      </div>
    );
  }
}
export default withRouter(injectIntl(DocHeader));
