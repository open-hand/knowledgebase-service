import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { StoreProvider } from './stores';
import DocHome from './DocHome';
import ImportHome from './ImportHome';
import PageStore from '../stores';

function Index(props) {
  const { pageStore } = useContext(PageStore);
  const { match: { params: { baseId } } } = props;
  pageStore.setBaseId(baseId);
  const { getImportMode: mode } = pageStore;
  return (
    <StoreProvider {...props}>
      {mode
        ? (
          <ImportHome />
        ) : (
          <DocHome />
        )
      }
    </StoreProvider>
  );
}

export default observer(Index);
