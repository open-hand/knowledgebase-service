import React, { useMemo, useContext, useEffect, useState, Fragment } from 'react';
import { Choerodon, Action } from '@choerodon/boot';
import { DataSet, Table, Modal, Tooltip } from 'choerodon-ui/pro';
import TimeAgo from 'timeago-react';
import PageStore from '../../../stores';
import UserHead from '../../../../../components/UserHead';
import SmartTooltip from '../../../../../components/SmartTooltip';
import { onOpenPreviewModal } from '../../../../knowledge-bases/components/baseModal';
import EditTemplate from './edit';
import CreateTemplate from '../create-template';
import DataSetFactory from './dataSet';
import './Template.less';

const { Column } = Table;
const prefix = 'c7n-knowledge-template';
function Template() {
  const { pageStore } = useContext(PageStore);
  const [editing, setEditing] = useState(false);
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ pageStore })), []);
  useEffect(() => {
    pageStore.setTemplateDataSet(dataSet);
  }, []);
  async function handleDelete(record) {
    Modal.confirm({
      title: '确认删除',
      children: `确认删除模板${record.get('title')}？`,
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
        pageStore.setImportVisible(false);
        pageStore.setShareVisible(false);
      }
    }).catch(() => {
      pageStore.setImportVisible(false);
      pageStore.setShareVisible(false);
    });
  }
  function renderName({ text, record }) {
    const clickable = record.get('templateType') === 'custom';
    return (
      <SmartTooltip title={text} placement="topLeft">
        <span
          className={clickable ? 'link' : 'text-gray'}
          onClick={() => clickable && loadDoc(record.get('id'))}
        >
          {text}
        </span>
      </SmartTooltip>
    );
  }
  function renderAction({ record }) {
    const actionData = record.get('templateType') === 'custom' ? [{
      text: '预览',
      action: () => handlePreview(record),
    }, {
      text: '基于此模板创建',
      action: () => handleCreateTemplate(record),
    }, {
      text: '删除',
      action: () => handleDelete(record),
    }] : [{
      text: '预览',
      action: () => handlePreview(record),
    }, {
      text: '基于此模板创建',
      action: () => handleCreateTemplate(record),
    }];
    return <Action data={actionData} />;
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
        <Fragment>
          <div className={`${prefix}-title`}>模板管理</div>
          <Table dataSet={dataSet}>
            <Column name="title" renderer={renderName} />
            <Column renderer={renderAction} width={50} align="right" />
            <Column 
              name="description"
              className="text-gray"          
              renderer={({ text }) => <SmartTooltip title={text} placement="topLeft">{text}</SmartTooltip>}
            />
            <Column name="lastUpdatedUser" className="text-gray" renderer={({ record }) => record.get('lastUpdatedUser') && <UserHead style={{ display: 'inline-flex' }} user={record.get('lastUpdatedUser')} />} />
            <Column
              name="lastUpdateDate"
              className="text-gray"
              renderer={({ text }) => (
                <TimeAgo
                  datetime={text}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              )}
            />
            <Column name="createdUser" className="text-gray" renderer={({ record }) => (record.get('createdUser') ? <UserHead style={{ display: 'inline-flex' }} user={record.get('createdUser')} /> : '系统')} />
            <Column
              name="creationDate"
              className="text-gray"
              renderer={({ text }) => (
                <TimeAgo
                  datetime={text}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              )}
            />
            <Column name="templateType" className="text-gray" renderer={({ text }) => (text === 'custom' ? '用户自定义' : '系统预置')} />
          </Table>
        </Fragment>
      )}

    </div>

  );
}

export default Template;
