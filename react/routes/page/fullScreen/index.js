import React from 'react';
import { StoreProvider } from './stores';
import FullScreenHome from './FullScreenHome';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <FullScreenHome />
    </StoreProvider>
  );
}
