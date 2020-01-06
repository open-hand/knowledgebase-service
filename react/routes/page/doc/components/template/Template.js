import React, { useMemo, useContext, useEffect, useState, Fragment } from 'react';
import { Choerodon, Action } from '@choerodon/boot';
import { DataSet, Table } from 'choerodon-ui/pro';
import TimeAgo from 'timeago-react';
import PageStore from '../../../stores';
import UserHead from '../../../../../components/UserHead';
import EditTemplate from './edit';
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
  function handleDelete() {
    
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
    const clickable = true;
    return (
      <span
        className={clickable ? 'link' : 'text-gray'} 
        onClick={() => loadDoc(record.get('id'))}
      >
        {text}
      </span>
    );
  }
  function renderAction({ record }) {
    const actionData = [{
      text: '删除',
      action: () => handleDelete(record),
    }];
    return <Action data={actionData} />;
  }
  function renderEditor() {
    return (
      <EditTemplate
        onCancel={() => {
          setEditing(false);
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
            <Column name="description" className="text-gray" />
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
            <Column name="createdUser" className="text-gray" renderer={({ record }) => record.get('createdUser') && <UserHead style={{ display: 'inline-flex' }} user={record.get('createdUser')} />} />
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
            <Column name="type" className="text-gray" renderer={({ text }) => (text === 'user' ? '用户自定义' : '系统预置')} />
          </Table>
        </Fragment>
      )}
      
    </div>

  );
}

export default Template;
