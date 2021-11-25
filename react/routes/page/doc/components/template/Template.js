import React, {
  useMemo, useContext, useEffect, useState, Fragment, useCallback,
} from 'react';
import { Choerodon, Action } from '@choerodon/boot';
import {
  DataSet, Table, Modal, Tooltip,
} from 'choerodon-ui/pro';
import PageStore from '../../../stores';
import UserHead from '../../../../../components/UserHead';
import SmartTooltip from '../../../../../components/SmartTooltip';
import { onOpenPreviewModal } from '../../../../knowledge-bases/components/baseModal';
import EditTemplate from './edit';
import CreateTemplate from '../create-template';
import DataSetFactory from './dataSet';
import './Template.less';
import useFormatMessage from '@/hooks/useFormatMessage';

const { Column } = Table;
const prefix = 'c7n-knowledge-template';
function Template() {
  const { pageStore } = useContext(PageStore);
  const [editing, setEditing] = useState(false);
  const formatMessage = useFormatMessage('knowledge.template');
  const bootFormatMessage = useFormatMessage('boot');
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ pageStore, formatMessage, bootFormatMessage })), []);
  useEffect(() => {
    pageStore.setTemplateDataSet(dataSet);
  }, []);
  async function handleDelete(record) {
    Modal.open({
      title: '确认删除',
      children: `模板“${record.get('title')}”将会被移至回收站，您可以在回收站恢复此模板。`,
      onOk: async () => {
        await pageStore.deleteTemplate(record.get('id'));
        dataSet.query();
      },
    });
  }
  function handlePreview(record) {
    const id = record.get('id');
    onOpenPreviewModal(id);
  }
  function handleCreateTemplate(record) {
    CreateTemplate({
      pageStore,
      baseTemplate: record.toData(),
    });
  }
  function loadDoc(id) {
    pageStore.loadDoc(id).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        Choerodon.prompt(res.message);
      } else {
        setEditing(true);
        pageStore.setMode('edit');
      }
    }).catch(() => {
    });
  }
  function renderName({ text, record }) {
    return (
      <SmartTooltip title={text} placement="topLeft">
        <span
          style={{ color: 'var(--text-color)' }}
        >
          {text}
        </span>
      </SmartTooltip>
    );
  }

  const handleEdit = useCallback((record) => {
    loadDoc(record.get('id'));
  }, [loadDoc]);

  function renderAction({ record }) {
    const actionData = record.get('templateType') === 'custom' ? [{
      text: formatMessage({ id: 'edit' }),
      action: () => handleEdit(record),
    }, {
      text: formatMessage({ id: 'preview' }),
      action: () => handlePreview(record),
    }, {
      text: formatMessage({ id: 'create_from_base' }),
      action: () => handleCreateTemplate(record),
    }, {
      text: bootFormatMessage({ id: 'delete' }),
      action: () => handleDelete(record),
    }] : [{
      text: formatMessage({ id: 'preview' }),
      action: () => handlePreview(record),
    }, {
      text: formatMessage({ id: 'create_from_base' }),
      action: () => handleCreateTemplate(record),
    }];
    return <Action data={actionData} style={{ color: 'var(--text-color)' }} />;
  }
  function renderEditor() {
    return (
      <EditTemplate
        onCancel={() => {
          setEditing(false);
        }}
        onEdit={() => {
          setEditing(false);
          dataSet.query();
        }}
      />
    );
  }
  return (
    <div className={prefix}>
      {editing ? renderEditor() : (
        <>
          <div className={`${prefix}-title`}>
            {formatMessage({ id: 'manage' })}
          </div>
          <Table dataSet={dataSet}>
            <Column name="title" renderer={renderName} />
            <Column renderer={renderAction} width={60} align="right" />
            <Column
              name="description"
              renderer={({ text }) => <SmartTooltip title={text} placement="topLeft">{text}</SmartTooltip>}
            />
            <Column name="lastUpdatedUser" renderer={({ record }) => record.get('lastUpdatedUser') && <UserHead style={{ display: 'inline-flex' }} user={record.get('lastUpdatedUser')} />} />
            <Column
              name="lastUpdateDate"
            />
            <Column name="createdUser" renderer={({ record }) => (record.get('createdUser') ? <UserHead style={{ display: 'inline-flex' }} user={record.get('createdUser')} /> : '系统')} />
            <Column
              name="creationDate"
            />
            <Column name="templateType" renderer={({ text }) => (formatMessage({ id: text === 'custom' ? 'type_custom' : 'type_system' }))} />
          </Table>
        </>
      )}

    </div>

  );
}

export default Template;
