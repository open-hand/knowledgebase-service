import React, { useMemo, useContext } from 'react';
import { Choerodon, Action } from '@choerodon/boot';
import { DataSet, Table } from 'choerodon-ui/pro';
import TimeAgo from 'timeago-react';
import PageStore from '../../../stores';
import UserHead from '../../../../../components/UserHead';
import './Template.less';

const { Column } = Table;
const prefix = 'c7n-knowledge-template';
function Template() {
  const { pageStore } = useContext(PageStore);
  const dataSet = useMemo(() => new DataSet({
    selection: false,
    data: [{
      name: 'wangwang',
      description: 'wangwang',
      lastUpdateUser: {
        id: 7121,
        loginName: '16433',
        email: 'kaiwen.abcli@hand-china.abccom',
        realName: '李楷文',
        imageUrl: 'https://minio.choerodon.com.cn/iam-service/file_37110ff0ff674617abd5ef0e5fb2d165_ualb20p2uus.jpg',
        ldap: true,
      },
      lastUpdateTime: '2019-07-24 23:39:38',
      creator: {
        id: 7121,
        loginName: '16433',
        email: 'kaiwen.abcli@hand-china.abccom',
        realName: '李楷文',
        imageUrl: 'https://minio.choerodon.com.cn/iam-service/file_37110ff0ff674617abd5ef0e5fb2d165_ualb20p2uus.jpg',
        ldap: true,
      },
      create_time: '2019-07-24 23:39:38',
      type: 'user',
    }],
    primaryKey: 'id',
    autoQuery: true,
    transport: {
      read: {
        url: `${pageStore.apiGateway}/case/list_by_folder_id`,
        method: 'post',
        transformRequest: (data) => {
          const { params, summary, caseNum } = data;
          return JSON.stringify({
            contents: params ? [params] : [],
            searchArgs: {
              summary,
              caseNum,
            },
          });
        },
      },
    },
    fields: [
      { name: 'name', type: 'string', label: '模板名称' },
      { name: 'description', type: 'string', label: '模板简介' },
      { name: 'lastUpdateUser', type: 'object', label: '更新人' },
      { name: 'lastUpdateTime', type: 'string', label: '更新时间' },
      { name: 'creator', type: 'object', label: '创建人' },
      { name: 'create_time', type: 'string', label: '创建时间' },
      { name: 'type', type: 'string', label: '模板类型' },
    ],
    // queryFields: [
    //   { name: 'summary', type: 'string', label: '用例名称' },
    //   { name: 'caseNum', type: 'string', label: '用例编号' },
    // ],
  }), []);
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
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width={50} align="right" />
        <Column name="description" className="text-gray" />
        <Column name="lastUpdateUser" className="text-gray" renderer={({ record }) => <UserHead style={{ display: 'inline-flex' }} user={record.get('lastUpdateUser')} />} />
        <Column
          name="lastUpdateTime"
          className="text-gray"
          renderer={({ text }) => (
            <TimeAgo
              datetime={text}
              locale={Choerodon.getMessage('zh_CN', 'en')}
            />
          )}
        />
        <Column name="creator" className="text-gray" renderer={({ record }) => <UserHead style={{ display: 'inline-flex' }} user={record.get('creator')} />} />
        <Column
          name="create_time" 
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
