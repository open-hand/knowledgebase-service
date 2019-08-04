import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { stores } from '@choerodon/boot';
import PageStore from '../../../stores';
import DocViewer from '../../../../../components/DocViewer';

const { AppState } = stores;

function ViewMode() {
  const { pageStore } = useContext(PageStore);
  const { getWorkSpace: workSpace, getSpaceCode: spaceCode } = pageStore;
  const { getMode: mode, getDoc: data } = pageStore;

  return (
    <span>
      <DocViewer
        spaceData={workSpace[spaceCode].data}
        data={data}
        loginUserId={AppState.userInfo.id}
        store={pageStore}
      />
    </span>
  );
}

export default observer(ViewMode);
