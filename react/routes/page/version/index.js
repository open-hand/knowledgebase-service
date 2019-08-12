import React from 'react';
import { StoreProvider } from './stores';
import VersionHome from './VersionHome';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <VersionHome />
    </StoreProvider>
  );
}
