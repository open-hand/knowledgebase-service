import React, { useContext } from 'react';
import { Table, TextField, Select } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import Store from '../../stores';
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

  const getQueryFields = () => ({
    name: <TextField clearButton labelLayout="float" />,
    belongTo: <TextField clearButton labelLayout="float" />,
    type: <Select labelLayout="float" searchable />,
  });

  return (
    <Table className="c7n-kb-binTable" dataSet={binTableDataSet} queryFields={getQueryFields()}>
      <Column name="name" />
      <Column name="belongTo" renderer={renderBelongTo} />
      <Column name="type" renderer={renderType} />
      <Column name="deletePerson" />
      <Column name="deleteTime" />
    </Table>
  );
});

export default BinTable;
