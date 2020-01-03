import { DataSet } from 'choerodon-ui/pro';
import { getProjectId, getOrganizationId } from '../../../common/utils';

export default function BinTableDataSet() {
  const typeDataSet = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'key', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { value: '文档', key: 'doc' },
      { value: '知识库', key: 'base' },
    ],
  });

  return {
    autoCreate: true,
    autoQuery: true,
    transport: {
      read: ({ data, params, dataSet }) => {
        let postData = { searchArgs: {} };
        if (data && Object.keys(data).length) {
          if (data.name) {
            postData.searchArgs.name = data.name;
          } 
          if (data.type) {
            postData.searchArgs.type = data.type;
          }
          if (data.belongTo) {
            postData.searchArgs.belongTo = data.belongTo;
          } 
        } else {
          postData = {};
        }
        
        return {
          url: `/knowledge/v1/projects/${getProjectId()}/recycle/page_by_options`,
          method: 'post',
          data: postData,
          params: {
            organizationId: getOrganizationId(),
            ...params,
          },
        };
      },
    },
    fields: [
      {
        name: 'name', type: 'string', label: '知识库/文档名称',
      },
      {
        name: 'belongTo', type: 'string', label: '所属知识库',
      },
      {
        name: 'type',
        type: 'string',
        label: '类型',
      },
      {
        name: 'deletePerson',
        type: 'string',
        label: '删除人',
      },
      {
        name: 'deleteTime',
        type: 'string',
        label: '删除时间',
      },
    ],
    queryFields: [
      {
        name: 'name', type: 'string', label: '知识库/文档名称',
      },
      {
        name: 'belongTo', type: 'string', label: '所属知识库',
      },
      {
        name: 'type',
        type: 'string',
        label: '类型',
        textField: 'value',
        valueField: 'key',
        options: typeDataSet,
      },
    ],
  };
}
