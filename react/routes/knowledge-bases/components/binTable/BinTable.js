import React, { useContext, useMemo } from 'react';
import { Table, TextField, Select, Modal, Form, DataSet } from 'choerodon-ui/pro';
import { Action } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import Store from '../../stores';
import { deleteDocOrBase, deleteOrgDocOrBase, recoverFromBin, recoverOrgFromBin, judgeBelongBaseIsExist, judgeOrgBelongBaseIsExist } from '../../../../api/knowledgebaseApi';
import UserHead from '../../../../components/UserHead';
import SmartTooltip from '../../../../components/SmartTooltip';
import RecoverDocModalDataSet from './RecoverDocModalDataSet';
import './BinTable.less';

const { Column } = Table;

const RecoverDocModal = observer(({ recoverType, recoverDocModalDataSet, record }) => (
  <div className="c7n-kb-recoverDocModal-children">
    <span className="c7n-kb-recoverDocModal-children-tip">{`此${recoverType === 'page' ? '文档' : '模板'}所在的知识库“${record.get('belongToBaseName')}”已被删除，请选择此${recoverType === 'page' ? '文档' : '模板'}存放的知识库地址。`}</span>
    <Form dataSet={recoverDocModalDataSet}>
      <Select name="baseId" />
    </Form>
  </div>
));

const BinTable = observer(() => {
  const { binTableDataSet, knowledgeHomeStore, type } = useContext(Store);
  const renderBelongTo = ({ record }) => {
    if (record.get('type') === 'base') {
      return '/';
    } else {
      return (
        <SmartTooltip title={record.get('belongToBaseName')}>
          {record.get('belongToBaseName')}
        </SmartTooltip>
      );
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

  const openSelectBaseModal = (record, dataSet, recoverType) => {
    const recoverDocModalDataSet = new DataSet(RecoverDocModalDataSet({ knowledgeHomeStore, type }));
    Modal.open({
      key: Modal.key(),
      title: recoverType === 'page' ? '恢复文档' : '恢复模板',
      children: <RecoverDocModal recoverType={recoverType} recoverDocModalDataSet={recoverDocModalDataSet} record={record} recoverProjectFromBin={recoverProjectFromBin} recoverOrganizationFromBin={recoverOrganizationFromBin} />,
      okText: '保存',
      cancel: '取消',
      onOk: () => handleRecoverDoc({ recoverDocModalDataSet, record, dataSet }),
      style: { width: '5.6rem' },
      className: 'c7n-kb-recoverDocModal',
    });
  };

  const handleRecoverFromBin = (record, dataSet) => {
    if (type === 'project') {
      if (record.get('type') !== 'base') {
        judgeBelongBaseIsExist(record.get('id')).then((isExist) => {
          if (isExist) {
            recoverProjectFromBin({ record, dataSet });
          } else {
            openSelectBaseModal(record, dataSet, record.get('type'));
          }
        });
      } else {
        recoverProjectFromBin({ record, dataSet });
      }
    } else if (record.get('type') !== 'base') {
      judgeOrgBelongBaseIsExist(record.get('id')).then((isExist) => {
        if (isExist) {
          recoverOrganizationFromBin({ record, dataSet });
        } else {
          openSelectBaseModal(record, dataSet, record.get('type'));
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
      children: `将彻底删除${typeName}“${record.get('name')}”，不可恢复。`,
      onOk: () => handleDeleteDocOrBin(record, dataSet),
    });
  };

  const renderName = ({ record }) => (
    <SmartTooltip title={record.get('name')}>
      {record.get('name')}
    </SmartTooltip>
  );

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
      <Column name="name" renderer={renderName} />
      <Column name="action" renderer={renderAction} />
      <Column name="belongToBaseName" renderer={renderBelongTo} />
      <Column name="type" renderer={renderType} />
      <Column name="lastUpdatedBy" renderer={renderDeletePerson} className="c7n-kb-binTable-deletePersonColumn" />
      <Column name="lastUpdateDate" />
    </Table>
  );
});

export default BinTable;
