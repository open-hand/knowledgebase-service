import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { stores } from '@choerodon/boot';
import BinTableDataSet from './BinTableDataSet';
import KnowledgeHomeStore from './KnowledgeHomeStore';

const Store = createContext();

export default Store;

const { AppState } = stores;

export const StoreProvider = (props) => {
  const { children } = props;
  const binTableDataSet = useMemo(() => new DataSet(BinTableDataSet()), []);
  const knowledgeHomeStore = useMemo(() => new KnowledgeHomeStore(), []);

  const value = {
    ...props,
    prefixCls: 'c7n-kb-kbHome',
    binTableDataSet,
    knowledgeHomeStore,
    type: AppState.currentMenuType.type,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
