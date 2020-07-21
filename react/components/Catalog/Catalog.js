import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tooltip } from 'choerodon-ui';
import $ from 'jquery';
import './Catalog.less';

@observer
class Catalog extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  /**
   * 点击事件，滚动
   * @param id
   */
  onCatalogClick = (id) => {
    const scrollEle = document.getElementById(decodeURI(id));
    if (scrollEle) {
      scrollEle.scrollIntoView(true);
    }
  };

  /**
   * 判断是几级标题
   * @param nodeName
   */
  getHx = (nodeName) => {
    switch (nodeName) {
      case 'H1':
        return 1;
      case 'H2':
        return 2;
      case 'H3':
        return 3;
      case 'H4':
        return 4;
      case 'H5':
        return 5;
      case 'H6':
        return 6;
      default:
        return 7;
    }
  };

  /**
   * 计算左侧margin值（注意：标题号越小，级别越高）
   * @param item
   * @param index
   * @param hList
   * @param marginLeftList
   */
  getMarginLeft = (item, index, hList, marginLeftList) => {
    const level = this.getHx(item.nodeName);
    if (level === 1 || index === 0) {
      // 一级标题或第一个标题，不偏移
      return 0;
    } else if (level === this.getHx(hList[index - 1].nodeName)) {
      // 与上一个标题同级，偏移同上
      return marginLeftList[index - 1];
    } else if (level > this.getHx(hList[index - 1].nodeName)) {
      // 低于上一个标题，相对上一个偏移15
      return marginLeftList[index - 1] + 15;
    } else {
      // 高于上一个标题，向前查找
      let destIndex = index - 1;
      while (destIndex > 0 && level < this.getHx(hList[destIndex].nodeName)) {
        destIndex -= 1;
      }
      // 找到高一级标题，偏移+15
      if (level > this.getHx(hList[destIndex].nodeName)) {
        return marginLeftList[destIndex] + 15;
      }
      // 找到同级标题或第一个标题，取相同偏移
      return marginLeftList[destIndex];
    }
  };

  renderCatalog = () => {
    const { eleId = '#docViewer-scroll' } = this.props;
    const catalog = [];
    const hList = $(eleId).find('h1,h2,h3,h4,h5,h6');
    const marginLeftList = [];
    hList.each((index, item) => {
      $(item).attr('id', `toc${index}`);
      const marginLeft = this.getMarginLeft(item, index, hList, marginLeftList);
      marginLeftList.push(marginLeft);
      catalog.push(
        <li style={{ marginLeft, listStyle: 'none' }}>
          <span
            title={item.innerText}
            onClick={() => this.onCatalogClick(`toc${index}`)}
          >
            {item.innerText}
          </span>
        </li>,
      );
    });
    return catalog;
  };

  handleCatalogChange = () => {
    const { store } = this.props;
    const { getCatalogVisible: catalogVisible } = store;
    store.setCatalogVisible(!catalogVisible);
  };

  render() {
    return (
      <div className="c7n-docCatalog">
        <span className="catalog">目录</span>
        <span style={{ float: 'right' }}>
          <Tooltip placement="top" title={<FormattedMessage id="docHeader.catalog.close" />}>
            <Button shape="circle" size="small" onClick={this.handleCatalogChange}>
              <i className="icon icon-format_indent_increase" />
            </Button>
          </Tooltip>
        </span>
        <div className="toc">
          {this.renderCatalog()}
        </div>
      </div>
    );
  }
}

export default withRouter(injectIntl(Catalog));
