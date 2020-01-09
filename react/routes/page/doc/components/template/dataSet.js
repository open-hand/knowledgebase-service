import { DataSet } from 'choerodon-ui/pro';

export default ({ pageStore, selection = false } = {}) => ({
  selection,
  data: [],
  primaryKey: 'id',
  autoQuery: true,
  transport: {
    read: {
      url: `${pageStore.apiGateway}/document_template/template_list?organizationId=${pageStore.orgId}&baseId=${pageStore.baseId}`,
      method: 'post',
      transformRequest: (data) => {
        const { params, title, description, templateType } = data;
        return JSON.stringify({
          contents: (params || title) ? [params || title] : [],
          searchArgs: {
            description,
            title,
            templateType,
          },
        });
      },
    },
  },
  fields: [
    { name: 'title', type: 'string', label: '模板名称' },
    { name: 'description', type: 'string', label: '模板简介' },
    { name: 'lastUpdatedUser', type: 'object', label: '更新人' },
    { name: 'lastUpdateDate', type: 'string', label: '更新时间' },
    { name: 'createdUser', type: 'object', label: '创建人' },
    { name: 'creationDate', type: 'string', label: '创建时间' },
    { name: 'templateType', type: 'string', label: '模板类型' },
  ],
  queryFields: [
    { name: 'title', type: 'string', label: '模板名称' },
    { name: 'description', type: 'string', label: '模板简介' },
    {
      name: 'templateType',
      type: 'string',
      label: '模板类型',
      options: new DataSet({
        data: [{
          meaning: '用户自定义',
          value: 'custom',
        }, {
          meaning: '系统预置',
          value: 'sys_preset',
        }],
      }),
    },
  ],
});
