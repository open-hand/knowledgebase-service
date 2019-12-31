import React, { useMemo, useContext } from 'react';
import { DataSet, Table } from 'choerodon-ui/pro';
import PageStore from '../../../stores';

const { Column } = Table;

function Template() {
  const { pageStore } = useContext(PageStore);
  const dataSet = useMemo(() => new DataSet({
    primaryKey: 'caseId',
    autoQuery: true,
    selection: 'multiple',
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
      { name: 'summary', type: 'string', label: '用例名称' },
      { name: 'caseNum', type: 'string', label: '用例编号' },
      { name: 'folderName', type: 'string', label: '目录' },
    ],
    queryFields: [
      { name: 'summary', type: 'string', label: '用例名称' },
      { name: 'caseNum', type: 'string', label: '用例编号' },
    ],
  }), []);
  return (
    <div>
      <h3>模板管理</h3>
      <Table dataSet={dataSet}>
        <Column name="summary" />
        <Column name="caseNum" />
        <Column name="folderName" />
      </Table>
    </div>

  );
}

export default Template;
