import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Tooltip, Icon } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import _ from 'lodash';
import './DocDetailNav.scss';

const navList = ['attachment', 'comment', 'log', 'version'];
const navIcon = {
  attachment: {
    name: '附件',
    icon: 'attach_file',
  },
  comment: {
    name: '评论',
    icon: 'sms_outline',
  },
  log: {
    name: '日志',
    icon: 'insert_invitation',
  },
  version: {
    name: '版本',
    icon: 'versionline',
  },
};

let sign = true;

@inject('AppState')
@observer class DocDetailNav extends Component {
  constructor(props) {
    super(props);
    this.state = {
      nav: 'attachment',
    };
  }

  componentDidMount() {
    document.getElementById('scroll-area').addEventListener('scroll', (e) => {
      if (sign) {
        const { nav } = this.state;
        const currentNav = this.getCurrentNav(e);
        if (nav !== currentNav && currentNav) {
          this.setState({
            nav: currentNav,
          });
        }
      }
    });
  }

  componentWillReceiveProps(nextProps) {
    this.scrollToAnchor(nextProps.currentNav);
    this.setState({
      nav: nextProps.currentNav,
    });
  }

  isInLook = (ele) => {
    if (ele) {
      const a = ele.offsetTop;
      const target = document.getElementById('scroll-area');
      return a + ele.offsetHeight > target.scrollTop;
    } else {
      return false;
    }
  };

  getCurrentNav = () => _.find(navList, i => this.isInLook(document.getElementById(i)));

  scrollToAnchor = (anchorName) => {
    if (anchorName) {
      const anchorElement = document.getElementById(anchorName);
      if (anchorElement) {
        sign = false;
        anchorElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
          inline: 'end',
        });
        setTimeout(() => {
          sign = true;
        }, 2000);
      }
    }
  };

  render() {
    const { nav } = this.state;
    const {
      intl,
    } = this.props;

    return (
      <div className="c7n-detail-nav">
        <ul className="c7n-nav-ul">
          {navList.map(navItem => (
            <Tooltip placement="right" title={intl.formatMessage({ id: `doc.${navItem}` })} key={navItem}>
              <li id={`${navItem}-nav`} className={`c7n-li ${nav === navItem ? 'c7n-li-active' : ''}`}>
                <Icon
                  type={`${navIcon[navItem] && navIcon[navItem].icon} c7n-icon-li`}
                  role="none"
                  onClick={() => {
                    this.setState({ nav: navItem });
                    this.scrollToAnchor(navItem);
                  }}
                />
              </li>
            </Tooltip>
          ))
          }
        </ul>
      </div>
    );
  }
}

export default withRouter(injectIntl(DocDetailNav));
