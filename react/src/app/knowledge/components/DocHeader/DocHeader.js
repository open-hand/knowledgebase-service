import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button, Divider, Tooltip, Icon, Input, Dropdown, Menu, Breadcrumb,
} from 'choerodon-ui';
import { stores, Permission } from '@choerodon/boot';
import './DocHeaser.scss';

const { AppState } = stores;

class DocHeader extends Component {
  constructor(props) {
    super(props);
    this.state = {
      edit: false,
      newTitle: false,
    };
  }

  handleClickTitle = () => {
    this.setState({
      edit: true,
    });
  };

  handleCancel = () => {
    this.setState({
      edit: false,
      newTitle: false,
    });
  };

  handleTitleChange = (e) => {
    this.setState({
      newTitle: e.target.value,
    });
  };

  handleSubmit = () => {
    const { newTitle } = this.state;
    const { onTitleEdit } = this.props;
    if (newTitle && newTitle.trim() && onTitleEdit) {
      onTitleEdit(newTitle);
    }
    this.handleCancel();
  };

  getMenus = () => {
    const { onBtnClick, data } = this.props;
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    return (
      <Menu onClick={e => onBtnClick(e.key)}>
        <Menu.Item key="export">
          导出
        </Menu.Item>
        <Menu.Item key="version">
          版本对比
        </Menu.Item>
        <Menu.Item key="log">
          活动日志
        </Menu.Item>
        <Menu.Item key="move">
          移动
        </Menu.Item>
        {AppState.userInfo.id === data.createdBy
          ? (
            <Menu.Item key="delete">
              删除
            </Menu.Item>
          ) : (
            <Permission
              key="adminDelete"
              type={type}
              projectId={projectId}
              organizationId={orgId}
              service={[`knowledgebase-service.work-space-${type}.delete`]}
            >
              <Menu.Item key="adminDelete">
                删除
              </Menu.Item>
            </Permission>
          )
        }
      </Menu>
    );
  };

  handleBreadcrumbClick = (id) => {
    const { onBreadcrumbClick } = this.props;
    if (onBreadcrumbClick) {
      onBreadcrumbClick(id);
    }
  };

  renderBreadcrumb = () => {
    const { data, spaceData } = this.props;
    const breadcrumb = [];
    const parentIds = data.route && data.route.split('.').filter(item => spaceData.items[Number(item)]);
    if (parentIds.length && parentIds.length > 3) {
      breadcrumb.push(
        <Breadcrumb.Item key={parentIds[0]}>
          <span
            className="c7n-docHeader-breadcrumb-item"
            onClick={() => this.handleBreadcrumbClick(Number(parentIds[0]))}
          >
            {spaceData.items[Number(parentIds[0])].data.title}
          </span>
        </Breadcrumb.Item>,
      );
      breadcrumb.push(
        <Breadcrumb.Item key={parentIds[1]}>
          <span
            className="c7n-docHeader-breadcrumb-item"
            onClick={() => this.handleBreadcrumbClick(Number(parentIds[1]))}
          >
            {spaceData.items[Number(parentIds[1])].data.title}
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
            className="c7n-docHeader-breadcrumb-item"
            onClick={() => this.handleBreadcrumbClick(Number(parentIds[parentIds.length - 1]))}
          >
            {spaceData.items[Number(parentIds[parentIds.length - 1])].data.title}
          </span>
        </Breadcrumb.Item>,
      );
    } else {
      parentIds.forEach((item) => {
        breadcrumb.push(
          <Breadcrumb.Item key={item}>
            <span
              className="c7n-docHeader-breadcrumb-item"
              onClick={() => this.handleBreadcrumbClick(Number(item))}
            >
              {spaceData.items[Number(item)] && spaceData.items[Number(item)].data.title}
            </span>
          </Breadcrumb.Item>,
        );
      });
    }
    return breadcrumb;
  };

  render() {
    const { edit } = this.state;
    const { data, onBtnClick, catalogVisible, mode } = this.props;

    return (
      <div className="c7n-docHeader">
        <div className="c7n-docHeader-wrapper">
          <span className="c7n-docHeader-breadcrumb">
            <Breadcrumb separator=">">
              {this.renderBreadcrumb()}
            </Breadcrumb>
          </span>
          <span className="c7n-docHeader-control">
            {!mode
              ? (
                <React.Fragment>
                  <Tooltip placement="top" title={<FormattedMessage id="edit" />}>
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('edit')}>
                      <i className="icon icon-mode_edit" />
                    </Button>
                  </Tooltip>
                  <Tooltip placement="top" title={<FormattedMessage id="docHeader.share" />}>
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('share')}>
                      <i className="icon icon-share" />
                    </Button>
                  </Tooltip>
                  <Tooltip placement="top" title={<FormattedMessage id="docHeader.attach" />}>
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('attach')}>
                      <i className="icon icon-attach_file" />
                    </Button>
                  </Tooltip>
                  <Tooltip placement="top" title={<FormattedMessage id="docHeader.comment" />}>
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('comment')}>
                      <i className="icon icon-chat_bubble_outline" />
                    </Button>
                  </Tooltip>
                  <Dropdown overlay={this.getMenus()} trigger={['click']}>
                    <Button
                      className="c7n-docHeader-btn"
                      shape="circle"
                      size="small"
                    >
                      <i className="icon icon-more_vert" />
                    </Button>
                  </Dropdown>
                  <Divider type="vertical" />
                </React.Fragment>
              ) : null
            }
            {mode === 'share'
              ? (
                <React.Fragment>
                  <Tooltip placement="top" title={<FormattedMessage id="docHeader.attach" />}>
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('attach')}>
                      <i className="icon icon-attach_file" />
                    </Button>
                  </Tooltip>
                  <Tooltip placement="top" title="导出">
                    <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('export')}>
                      <i className="icon icon-export_PDF" />
                    </Button>
                  </Tooltip>
                  <Divider type="vertical" />
                </React.Fragment>
              ) : null
            }
            <Tooltip placement="top" title={<FormattedMessage id="docHeader.catalog" />}>
              <Button shape="circle" size="small" onClick={() => onBtnClick('catalog')}>
                <i className={`icon icon-${catalogVisible ? 'format_indent_increase' : 'format_indent_decrease'}`} />
              </Button>
            </Tooltip>
          </span>
        </div>
        <div className="c7n-docHeader-title">
          {edit
            ? (
              <span>
                <Input
                  size="large"
                  showLengthInfo={false}
                  maxLength={40}
                  style={{ width: 650 }}
                  defaultValue={data.pageInfo.title || ''}
                  onChange={this.handleTitleChange}
                />
                <Button
                  className="c7n-docHeader-title-save"
                  shape="circle"
                  size="small"
                  onClick={this.handleSubmit}
                >
                  <i className="icon icon-done" />
                </Button>
                <Button
                  className="c7n-docHeader-title-cancel"
                  shape="circle"
                  size="small"
                  onClick={this.handleCancel}
                >
                  <i className="icon icon-close" />
                </Button>
              </span>
            ) : (
              <span>
                {data.pageInfo.title}
                {mode !== 'share'
                  ? (
                    <Icon
                      type="mode_edit"
                      className="c7n-docHeader-title-edit"
                      onClick={this.handleClickTitle}
                    />
                  ) : null
                }
              </span>
            )
          }
        </div>
      </div>
    );
  }
}
export default withRouter(injectIntl(DocHeader));
