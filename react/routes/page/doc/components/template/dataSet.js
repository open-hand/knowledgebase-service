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
        const { params, name } = data;
        return JSON.stringify({
          contents: (params || name) ? [params || name] : [],           
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
  queryFields: [
    { name: 'name', type: 'string', label: '模板名称' },
  ],
});
