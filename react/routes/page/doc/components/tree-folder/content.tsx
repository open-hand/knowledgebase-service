import React from 'react';
import { observer } from 'mobx-react-lite';
import { Table } from 'choerodon-ui/pro';
import { mapping } from './stores/tableDataSet';
import { useStore } from './stores';

import './index.less';

const prefix = 'c7ncd-treeFolder';

const Index = observer(() => {
  const {
    TableDataSet,
    data,
  } = useStore();

  const {
    data: {
      title,
    },
  } = data;

  return (
    <div className={prefix}>
      <p className={`${prefix}-title`}>{title}</p>
      <Table queryBar={'none' as any} dataSet={TableDataSet}>
        <Table.Column name={mapping.name.name} />
        <Table.Column name={mapping.attribute.name} />
        <Table.Column name={mapping.creator.name} />
        <Table.Column name={mapping.operation.name} />
      </Table>
    </div>
  );
});

export default Index;
