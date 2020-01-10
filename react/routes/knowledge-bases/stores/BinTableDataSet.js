import { DataSet } from 'choerodon-ui/pro';
import { getProjectId, getOrganizationId } from '../../../common/utils';

export default function BinTableDataSet({ type }) {
  const typeDataSet = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'key', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { value: '文档', key: 'page' },
      { value: '知识库', key: 'base' },
      { value: '模板', key: 'template' },
    ],
  });

  return {
    autoCreate: true,
    autoQuery: false,
    selection: false,
    transport: {
      read: ({ data, params }) => {
        let postData = { searchArgs: {} };
        if (data && Object.keys(data).length) {
          if (data.name) {
            postData.searchArgs.name = data.name;
          }
          if (data.type) {
            postData.searchArgs.type = data.type;
          }
          if (data.belongToBaseName) {
            postData.searchArgs.belongToBaseName = data.belongToBaseName;
          }
          if (data.params) {
            postData.contents = [data.params];
          }
        } else {
          postData = {};
        }

        return {
          url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/recycle/page_by_options` : `/knowledge/v1/organizations/${getOrganizationId()}/recycle/page_by_options`,
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
        name: 'belongToBaseName', type: 'string', label: '所属知识库',
      },
      {
        name: 'type',
        type: 'string',
        label: '类型',
      },
      {
        name: 'lastUpdatedBy',
        type: 'string',
        label: '删除人',
      },
      {
        name: 'lastUpdateDate',
        type: 'string',
        label: '删除时间',
      },
    ],
    queryFields: [
      {
        name: 'name', type: 'string', label: '知识库/文档名称',
      },
      {
        name: 'belongToBaseName', type: 'string', label: '所属知识库',
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
