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
      selectId: false,
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

  handleSpaceClick = (data, selectId) => {
    DocStore.setWorkSpace(data);
    DocStore.loadDoc(selectId);
    this.setState({
      selectId,
    });
  };

  handleSpaceExpand = (data) => {
    DocStore.setWorkSpace(data);
  };

  handleSpaceCollapse = (data) => {
    DocStore.setWorkSpace(data);
  };

  handleSpaceDragEnd = (data) => {
    DocStore.setWorkSpace(data);
  };

  render() {
    const { edit, selectId } = this.state;
    const spaceData = DocStore.getWorkSpace;
    const docData = DocStore.getDoc;

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
                <WorkSpace
                  data={spaceData}
                  selectId={selectId}
                  onClick={this.handleSpaceClick}
                  onExpand={this.handleSpaceExpand}
                  onCollapse={this.handleSpaceCollapse}
                  onDragEnd={this.handleSpaceDragEnd}
                />
              </div>
            </Section>
            <Divider />
            <Section size={{
              width: '100%',
            }}
            >
              <div className="c7n-knowledge-right">
                {selectId
                  ? (edit
                    ? <DocEditor onSave={this.onSave} onSaveAndEdit={this.onSave} onCancel={this.onSave} />
                    : <DocViewer data={docData} />
                  )
                  : (
                    <div>请选择文档</div>
                  )
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
