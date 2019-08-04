import React, { Component, useContext, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import ViewMode from './ViewMode';
import EditMode from './EditMode';
import PageStore from '../../../stores';
import DocEmpty from '../../../../../components/DocEmpty/DocEmpty';

function DocEditor() {
  const { pageStore } = useContext(PageStore);
  const { getMode: mode, getDoc: data, getSearchVisible: searchVisible } = pageStore;

  function renderDocEditor() {
    if (data) {
      if (mode === 'edit') {
        return (
          <EditMode />
        );
      } else {
        return (
          <ViewMode />
        );
      }
    } else {
      return (
        <DocEmpty mode={searchVisible ? 'search' : 'view'} />
      );
    }
  }

  return renderDocEditor();
}

export default observer(DocEditor);
