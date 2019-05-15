import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button, Divider, Tooltip,
} from 'choerodon-ui';
import './DocHeaser.scss';

class DocHeader extends Component {
  render() {
    const { data, onBtnClick, permission } = this.props;

    return (
      <div className="c7n-docHeader">
        <span className="c7n-docHeader-title">
          {data}
        </span>
        <span className="c7n-docHeader-control">
          {permission
            ? (
              <Tooltip placement="top" title={<FormattedMessage id="edit" />}>
                <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('edit')}>
                  <i className="icon icon-mode_edit" />
                </Button>
              </Tooltip>
            ) : ''
          }
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
          <Tooltip placement="top" title={<FormattedMessage id="docHeader.log" />}>
            <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('log')}>
              <i className="icon icon-insert_invitation" />
            </Button>
          </Tooltip>
          {permission
            ? (
              <Tooltip placement="top" title={<FormattedMessage id="delete" />}>
                <Button className="c7n-docHeader-btn" shape="circle" size="small" onClick={() => onBtnClick('delete')}>
                  <i className="icon icon-delete" />
                </Button>
              </Tooltip>
            ) : ''
          }
          <Divider type="vertical" />
          <Tooltip placement="top" title={<FormattedMessage id="docHeader.catalog" />}>
            <Button shape="circle" size="small" onClick={() => onBtnClick('catalog')}>
              <i className="icon icon-format_indent_increase" />
            </Button>
          </Tooltip>
        </span>
      </div>
    );
  }
}
export default withRouter(injectIntl(DocHeader));
