import React from 'react';
import classnames from 'classnames';
import './Section.less';

const prefix = 'c7n-workSpace-Section';
export default function Section({ selected, onClick, children }) {  
  return (
    <div
      className={classnames(prefix, {
        [`${prefix}-selected`]: selected,
      })}
      onClick={onClick}
    >
      <span className={`${prefix}-text`}>{children}</span>
    </div>
  );
}
