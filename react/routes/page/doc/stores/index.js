import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import DocStore from './DocStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id }, currentMenuType }, intl, children } = props;
    const docStore = useMemo(() => new DocStore(), []);
    docStore.initCurrentMenuType(currentMenuType);
    const value = {
      ...props,
      prefixCls: 'lc-model-list',
      intlPrefix: type === 'organization' ? 'organization.model.list' : 'global.model.list',
      docStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
