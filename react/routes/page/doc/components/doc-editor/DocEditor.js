import React, {
  Component, useContext, useEffect, useState,
} from 'react';
import { observer } from 'mobx-react-lite';
import { LoadingHiddenWrap } from '@choerodon/agile/lib/components/Loading';
import ViewMode from './ViewMode';
import EditMode from './EditMode';
import PageStore from '../../../stores';
import DocEmpty from '../../../../../components/DocEmpty/DocEmpty';

function DocEditor(props) {
  const {
    searchText,
    readOnly,
    loadWorkSpace,
    fullScreen,
    exitFullScreen,
    editDoc,
    editTitleBefore,
    handleCreateClick,
  } = props;
  const { pageStore } = useContext(PageStore);
  const { getMode: mode, getDoc: data, getSearchVisible: searchVisible } = pageStore;
  function renderDocEditor() {
    if (data) {
      if (mode === 'edit') {
        return (
          <EditMode searchText={searchText} fullScreen={fullScreen} />
        );
      }
      return (
        <ViewMode
          searchText={searchText}
          readOnly={readOnly}
          fullScreen={fullScreen}
          loadWorkSpace={loadWorkSpace}
          exitFullScreen={exitFullScreen}
          editDoc={editDoc}
          editTitleBefore={editTitleBefore}
        />
      );
    }
    return (
      <LoadingHiddenWrap loadIds={['doc']}>
        <DocEmpty mode={searchVisible ? 'search' : 'view'} readOnly={readOnly} handleCreateClick={handleCreateClick} />
      </LoadingHiddenWrap>
    );
  }

  return renderDocEditor();
}

export default observer(DocEditor);
