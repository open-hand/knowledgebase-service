import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import BinTableDataSet from './BinTableDataSet';
import KnowledgeHomeStore from './KnowledgeHomeStore';

const Store = createContext();

export default Store;

export const StoreProvider = (props) => {
  const { children } = props;
  const binTableDataSet = useMemo(() => new DataSet(BinTableDataSet()), []);
  const knowledgeHomeStore = useMemo(() => new KnowledgeHomeStore(), []);

  const value = {
    ...props,
    prefixCls: 'c7n-kb-kbHome',
    binTableDataSet,
    knowledgeHomeStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
