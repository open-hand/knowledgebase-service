import React, { useContext, useMemo } from 'react';
import { Table, TextField, Select, Modal, Form, DataSet } from 'choerodon-ui/pro';
import { Action } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import Store from '../../stores';
import { deleteDocOrBase, deleteOrgDocOrBase, recoverFromBin, recoverOrgFromBin, judgeBelongBaseIsExist, judgeOrgBelongBaseIsExist } from '../../../../api/knowledgebaseApi';
import UserHead from '../../../../components/UserHead';
import RecoverDocModalDataSet from './RecoverDocModalDataSet';
import './BinTable.less';

const { Column } = Table;

const RecoverDocModal = observer(({ recoverDocModalDataSet, record }) => {
  return (
    <div className="c7n-kb-recoverDocModal-children">
      <span className="c7n-kb-recoverDocModal-children-tip">{`此文档所在的知识库“${record.get('belongToBaseName')}”已被删除，请选择此文档存放的知识库地址。`}</span>
      <Form dataSet={recoverDocModalDataSet}>
        <Select name="baseId" />
      </Form>
    </div>
  );
});

const BinTable = observer(() => {
  const { binTableDataSet, knowledgeHomeStore, type } = useContext(Store);
  const renderBelongTo = ({ record }) => {
    if (record.get('type') !== 'page') {
      return '/';
    } else {
      return record.get('belongToBaseName');
    }
  };

  const renderType = ({ record }) => {
    if (record.get('type') === 'base') {
      return '知识库';
    } else if (record.get('type') === 'page') {
      return '文档';
    } else {
      return '模板';
    }
  };

  const renderDeletePerson = ({ record }) => (
    <UserHead user={record.get('lastUpdatedUser')} />
  );

  const recoverProjectFromBin = ({ record, dataSet, baseId }) => {
    recoverFromBin(record.get('id'), record.get('type'), baseId).then(() => {
      dataSet.query();
      knowledgeHomeStore.axiosProjectBaseList();
    });
  };

  const recoverOrganizationFromBin = ({ record, dataSet, baseId }) => {
    recoverOrgFromBin(record.get('id'), record.get('type'), baseId).then(() => {
      dataSet.query();
      knowledgeHomeStore.axiosOrgBaseList();
    });
  };

  const handleRecoverDoc = async ({ recoverDocModalDataSet, record, dataSet }) => {
    const data = recoverDocModalDataSet.toData()[0];
    if (data && data.baseId) {
      if (type === 'project') {
        recoverProjectFromBin({ record, dataSet, baseId: data.baseId });
      } else {
        recoverOrganizationFromBin({ record, dataSet, baseId: data.baseId });
      }
      return true;
    } else {
      return false;
    }
  };

  const openSelectBaseModal = (record, dataSet) => {
    const recoverDocModalDataSet = new DataSet(RecoverDocModalDataSet({ knowledgeHomeStore, type }));
    Modal.open({
      key: Modal.key(),
      title: '恢复文档',
      children: <RecoverDocModal recoverDocModalDataSet={recoverDocModalDataSet} record={record} recoverProjectFromBin={recoverProjectFromBin} recoverOrganizationFromBin={recoverOrganizationFromBin} />,
      okText: '保存',
      cancel: '取消',
      onOk: () => handleRecoverDoc({ recoverDocModalDataSet, record, dataSet }),
      style: { width: '5.6rem' },
      className: 'c7n-kb-recoverDocModal',
    });
  };

  const handleRecoverFromBin = (record, dataSet) => {
    if (type === 'project') {
      if (record.get('type') === 'page') {
        judgeBelongBaseIsExist(record.get('id')).then((isExist) => {
          if (isExist === 'true') {
            recoverProjectFromBin({ record, dataSet });
          } else {
            openSelectBaseModal(record, dataSet);
          }
        });
      } else {
        recoverProjectFromBin({ record, dataSet });
      }
    } else if (record.get('type') === 'page') {
      judgeOrgBelongBaseIsExist(record.get('id')).then((isExist) => {
        if (isExist === 'true') {
          recoverOrganizationFromBin({ record, dataSet });
        } else {
          openSelectBaseModal(record, dataSet);
        }
      });
    } else {
      recoverOrganizationFromBin({ record, dataSet });
    }
  };

  const handleDeleteDocOrBin = (record, dataSet) => {
    if (type === 'project') {
      deleteDocOrBase(record.get('id'), record.get('type')).then(() => {
        dataSet.query();
        knowledgeHomeStore.axiosProjectBaseList();
      });
    } else {
      deleteOrgDocOrBase(record.get('id'), record.get('type')).then(() => {
        dataSet.query();
        knowledgeHomeStore.axiosOrgBaseList();
      });
    }
  };

  const openDeletePromptModal = (record, dataSet) => {
    let typeName = '知识库';
    if (record.get('type') === 'page') {
      typeName = '文档';
    } else if (record.get('type') === 'template') {
      typeName = '模板';
    }
    Modal.confirm({
      title: '确认删除',
      children: `确认从回收站删除${typeName}“${record.get('name')}”？`,
      onOk: () => handleDeleteDocOrBin(record, dataSet),
    });
  };

  const renderAction = ({ record, dataSet }) => {
    const actionDatas = [{
      text: '恢复',
      action: () => { handleRecoverFromBin(record, dataSet); },
    }, {
      text: '删除',
      action: () => openDeletePromptModal(record, dataSet),
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
      <Column name="lastUpdatedBy" renderer={renderDeletePerson} className="c7n-kb-binTable-deletePersonColumn" />
      <Column name="lastUpdateDate" />
    </Table>
  );
});

export default BinTable;
