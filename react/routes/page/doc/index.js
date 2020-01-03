import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { StoreProvider } from './stores';
import DocHome from './DocHome';
import ImportHome from './ImportHome';
import PageStore from '../stores';

function Index(props) {
  const { pageStore: { getImportMode: mode } } = useContext(PageStore);
  
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
