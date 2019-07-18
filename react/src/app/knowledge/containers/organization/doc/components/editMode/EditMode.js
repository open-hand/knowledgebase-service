import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Input } from 'choerodon-ui';
import DocEditor from '../../../../../components/DocEditor';

@observer
class Doc extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }

  componentDidMount() {
  }

  render() {
    const {
      docData, initialEditType, onTitleChange,
      handleSave, handleDeleteDraft, handleDocChange,
    } = this.props;
    return (
      <React.Fragment>
        <Input
          size="large"
          showLengthInfo={false}
          maxLength={40}
          style={{ width: 650, margin: 10 }}
          defaultValue={docData.pageInfo.title}
          onChange={onTitleChange}
        />
        <DocEditor
          data={docData.pageInfo.content}
          initialEditType={initialEditType}
          onSave={handleSave}
          onCancel={handleDeleteDraft}
          onChange={handleDocChange}
        />
      </React.Fragment>
    );
  }
}

export default withRouter(injectIntl(Doc));
