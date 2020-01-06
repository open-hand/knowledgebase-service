import React, { useContext } from 'react';
import { StoreProvider } from './stores';
import VersionHome from './VersionHome';
import PageStore from '../stores';

export default function Index(props) {
  const { pageStore } = useContext(PageStore);
  const { match: { params: { baseId } } } = props;
  pageStore.setBaseId(baseId);
  return (
    <StoreProvider {...props}>
      <VersionHome />
    </StoreProvider>
  );
}
