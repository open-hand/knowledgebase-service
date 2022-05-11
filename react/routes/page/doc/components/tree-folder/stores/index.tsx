import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import tableDataSet from './tableDataSet';

interface ContextType {
  TableDataSet: any,
  data: any,
  onDelete: any,
  cRef: any,
}

const Store = createContext({} as ContextType);

export function useStore() {
  return useContext(Store);
}

export const StoreProvider = (props: any) => {
  const {
    children,
    data,
  } = props;

  const {
    id,
  } = data;

  const TableDataSet = useMemo(() => new DataSet(tableDataSet(id)), [id]);

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
