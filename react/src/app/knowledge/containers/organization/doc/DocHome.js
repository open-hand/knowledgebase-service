import React, { Component } from 'react';
import { observer } from 'mobx-react';
import {
  Button, Icon,
} from 'choerodon-ui';
import {
  Page, Header, Content,
} from '@choerodon/boot';
import WorkSpace from '../../../components/WorkSpace';
import DocEditor from '../../../components/DocEditor';
import DocViewer from '../../../components/DocViewer';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
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

  componentDidMount() {}

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
            <span>创建新页面</span>
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
            <span>编辑</span>
          </Button>
          <Button
            funcType="flat"
            onClick={() => {}}
          >
            <Icon type="refresh icon" />
            <span>刷新</span>
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
              <div style={{ flex: 1, height: '100%', padding: 10, overflow: 'auto' }}>
                <WorkSpace />
              </div>
            </Section>
            <Divider />
            <Section size={{
              width: '100%',
            }}
            >
              <div style={{ flex: 1, height: '100%' }}>
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

export default PageHome;
