import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon,
} from 'choerodon-ui';
import {
  Page, Header, Content,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import WorkSpace from '../../../components/WorkSpace';
import DocEditor from '../../../components/DocEditor';
import DocViewer from '../../../components/DocViewer';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import DocStore from '../../../stores/organization/doc/DocStore';
import './DocHome.scss';

const { Section, Divider } = ResizeContainer;

@observer
class PageHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      edit: false,
    };
  }

  componentDidMount() {
    DocStore.loadWorkSpace();
  }

  onSave = () => {
    this.setState({
      edit: false,
    });
  };

  render() {
    const { edit } = this.state;

    return (
      <Page
        className="c7n-knowledge"
      >
        <Header>
          <Button
            type="primary"
            funcType="raised"
            onClick={() => {}}
          >
            <FormattedMessage id="doc.create" />
          </Button>
          <Button
            funcType="flat"
            onClick={() => {
              this.setState({
                edit: true,
              });
            }}
          >
            <Icon type="playlist_add icon" />
            <FormattedMessage id="edit" />
          </Button>
          <Button
            funcType="flat"
            onClick={() => {}}
          >
            <Icon type="refresh icon" />
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content style={{ padding: 0 }}>
          <ResizeContainer type="horizontal">
            <Section size={{
              width: 248,
              minWidth: 100,
              maxWidth: 400,
            }}
            >
              <div className="c7n-knowledge-left">
                <WorkSpace />
              </div>
            </Section>
            <Divider />
            <Section size={{
              width: '100%',
            }}
            >
              <div className="c7n-knowledge-right">
                {edit
                  ? <DocEditor onSave={this.onSave} onSaveAndEdit={this.onSave} onCancel={this.onSave} />
                  : <DocViewer />
                }
              </div>
            </Section>
          </ResizeContainer>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(PageHome));
