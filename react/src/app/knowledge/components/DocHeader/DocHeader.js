import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button, Divider, Tooltip, Icon, Input,
} from 'choerodon-ui';
import './DocHeaser.scss';

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

  render() {
    const { edit } = this.state;
    const { data, onBtnClick, permission, catalogVisible, mode } = this.props;

    return (
      <div className="c7n-docHeader">
        <span className="c7n-docHeader-title" style={{ marginTop: edit ? '0px' : '2px' }}>
          {edit
            ? (
              <span>
                <Input
                  size="large"
                  showLengthInfo={false}
                  maxLength={40}
                  style={{ width: 650 }}
                  defaultValue={data}
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
                {data}
                <Icon
                  type="mode_edit"
                  className="c7n-docHeader-title-edit"
                  onClick={this.handleClickTitle}
                />
              </span>
            )
          }
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
    );
  }
}
export default withRouter(injectIntl(DocHeader));
