import React from 'react';
import useFormatMessage from '@/hooks/useFormatMessage';

import Styles from './index.less';

const EmptyPage = () => {
  const formatMessage = useFormatMessage('knowledge.common');

  return (
    <div className={Styles.emptyPage_wrap}>
      <div className={Styles.img} />
      <div className={Styles.text}>
        {formatMessage({ id: 'share.cancel.des' })}
      </div>
    </div>
  );
};

export default EmptyPage;
