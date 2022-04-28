import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import tableDataSet from './tableDataSet';

interface ContextType {
  TableDataSet: any
}

const Store = createContext({} as ContextType);

export function useStore() {
  return useContext(Store);
}

export const StoreProvider = (props: any) => {
  const {
    children,
  } = props;

  const TableDataSet = useMemo(() => new DataSet(tableDataSet()), []);

  const value = {
    ...props,
    TableDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
