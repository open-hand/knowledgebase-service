import React, { useMemo, useContext, useEffect } from 'react';
import { Choerodon, Action } from '@choerodon/boot';
import { DataSet, Table } from 'choerodon-ui/pro';
import TimeAgo from 'timeago-react';
import PageStore from '../../../stores';
import UserHead from '../../../../../components/UserHead';
import DataSetFactory from './dataSet';
import './Template.less';

const { Column } = Table;
const prefix = 'c7n-knowledge-template';
function Template() {
  const { pageStore } = useContext(PageStore);
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ pageStore })), []);
  useEffect(() => {
    pageStore.setTemplateDataSet(dataSet);
  }, []);
  function handleDelete() {
    
  }
  function renderName({ text, record }) {
    const clickable = true;
    return (
      <span
        className={clickable ? 'link' : 'text-gray'} 
        // onClick={() => clickable && handleNavigateToOrganization(record)}
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
  return (
    <div className={prefix}>
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
    </div>

  );
}

export default Template;
