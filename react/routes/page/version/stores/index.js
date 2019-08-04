import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import DocStore from './DocStore';
import PageStore from '../../stores/PageStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id } }, intl, children } = props;
    const docStore = useMemo(() => new DocStore(), []);
    const pageStore = useMemo(() => new PageStore(), []);
    const value = {
      ...props,
      docStore,
      pageStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
