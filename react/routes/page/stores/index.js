import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import PageStore from './PageStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId, name }, currentMenuType }, children } = props;    
    const pageStore = useMemo(() => new PageStore(), []);
    pageStore.initCurrentMenuType(currentMenuType);
    const value = {
      type,
      id,
      organizationId,
      name,
      ...props,
      pageStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
