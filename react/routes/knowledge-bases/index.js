import React from 'react';
import { StoreProvider } from './stores';
import KnowledgeHome from './KnowledgeHome';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <KnowledgeHome />
    </StoreProvider>
  );
}
