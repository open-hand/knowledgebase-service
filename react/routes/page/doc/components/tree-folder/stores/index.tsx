import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import tableDataSet from './tableDataSet';

interface ContextType {
  TableDataSet: any,
  data: any,
  onDelete: any,
  cRef: any,
  store: any,
  refresh: any,
  AppState: any,
}

const Store = createContext({} as ContextType);

export function useStore() {
  return useContext(Store);
}

export const StoreProvider = inject('AppState')((props: any) => {
  const {
    children,
    data,
    AppState: {
      menuType: {
        type,
      },
    },
  } = props;

  const {
    id,
  } = data;

  const TableDataSet = useMemo(() => new DataSet(tableDataSet(id, type)), [id, type]);

  const value = {
    ...props,
    TableDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
});
