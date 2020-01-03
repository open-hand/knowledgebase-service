import React, { useContext, useEffect } from 'react';
import { Table, TextField, Select } from 'choerodon-ui/pro';
import { Action } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import Store from '../../stores';
import { deleteDocOrBase, recoverFromBin } from '../../../../api/knowledgebaseApi';
import './BinTable.less';

const { Column } = Table;

const BinTable = observer(() => {
  const { binTableDataSet } = useContext(Store);

  const renderBelongTo = ({ value, text, name, record, dataSet }) => {
    if (record.get('type') === 'base') {
      return '/';
    } else {
      return '文档所属知识库名';
    }
  };

  const renderType = ({ value, text, name, record, dataSet }) => {
    if (record.get('type') === 'base') {
      return '知识库';
    } else {
      return '文档';
    }
  };

  const renderAction = ({ value, text, name, record, dataSet }) => {
    const actionDatas = [{
      text: '恢复',
      action: () => recoverFromBin(record.get('id')).then(() => dataSet.query()),
    }, {
      text: '删除',
      action: () => deleteDocOrBase(record.get('id')).then(() => dataSet.query()),
    }];
    return <Action data={actionDatas} />;
  };

  const getQueryFields = () => ({
    name: <TextField clearButton labelLayout="float" />,
    belongToBaseName: <TextField clearButton labelLayout="float" />,
    type: <Select labelLayout="float" searchable />,
  });

  return (
    <Table className="c7n-kb-binTable" dataSet={binTableDataSet} queryFields={getQueryFields()}>
      <Column name="name" />
      <Column name="action" renderer={renderAction} />
      <Column name="belongToBaseName" renderer={renderBelongTo} />
      <Column name="type" renderer={renderType} />
      <Column name="lastUpdatedBy" />
      <Column name="lastUpdateDate" />
    </Table>
  );
});

export default BinTable;
