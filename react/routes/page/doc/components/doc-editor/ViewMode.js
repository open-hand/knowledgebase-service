import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { stores } from '@choerodon/boot';
import PageStore from '../../../stores';
import DocViewer from '../../../../../components/DocViewer';

const { AppState } = stores;

function ViewMode(props) {
  const { readOnly, loadWorkSpace, fullScreen, exitFullScreen, editDoc, searchText, editTitleBefore } = props;
  const { pageStore } = useContext(PageStore);
  const { getWorkSpace: workSpace, getSpaceCode: spaceCode } = pageStore;
  const { getMode: mode, getDoc: data } = pageStore;

  return (
    <span>
      <DocViewer
        key={data.id}
        readOnly={readOnly}
        fullScreen={fullScreen}
        spaceData={workSpace[spaceCode].data}
        data={data}
        loginUserId={AppState.userInfo.id}
        store={pageStore}
        onBreadcrumbClick={loadWorkSpace}
        exitFullScreen={exitFullScreen}
        editDoc={editDoc}
        searchText={searchText}
        editTitleBefore={editTitleBefore}
      />
    </span>
  );
}

export default observer(ViewMode);
