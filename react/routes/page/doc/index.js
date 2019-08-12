import React from 'react';
import { StoreProvider } from './stores';
import DocHome from './DocHome';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <DocHome />
    </StoreProvider>
  );
}
