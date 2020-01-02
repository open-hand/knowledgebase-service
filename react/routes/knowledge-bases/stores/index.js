import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import BinTableDataSet from './BinTableDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = (props) => {
  const { children } = props;
  const binTableDataSet = useMemo(() => new DataSet(BinTableDataSet()), []);
  
  const value = {
    ...props,
    prefixCls: 'c7n-kb-kbHome',
    binTableDataSet,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
