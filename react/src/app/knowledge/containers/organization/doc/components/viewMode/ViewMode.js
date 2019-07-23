import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Modal, Table } from 'choerodon-ui';
import { stores } from '@choerodon/boot';
import DocViewer from '../../../../../components/DocViewer';
import DocVersion from '../../../../../components/DocVersion';
import DocStore from '../../../../../stores/organization/doc/DocStore';

const { AppState, MenuStore } = stores;

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
      versionVisible, docData,
      catalogVisible, searchVisible,
      onBackBtnClick, beforeQuitEdit, handleBtnClick, handleTitleChange,
    } = this.props;
    const spaceCode = DocStore.getSpaceCode;
    const spaceData = DocStore.getWorkSpaceMap;

    return (
      <span>
        {versionVisible
          ? (
            <DocVersion store={DocStore} onRollback={onBackBtnClick} />
          ) : (
            <DocViewer
              mode={docData.isOperate ? '' : 'share'}
              data={docData}
              spaceData={spaceData[spaceCode]}
              onBreadcrumbClick={id => beforeQuitEdit('handleBreadcrumbClick', id)}
              onBtnClick={handleBtnClick}
              loginUserId={AppState.userInfo.id}
              onTitleEdit={handleTitleChange}
              store={DocStore}
              catalogVisible={catalogVisible}
              searchVisible={searchVisible}
            />
          )
        }
      </span>
    );
  }
}

export default withRouter(injectIntl(Doc));
