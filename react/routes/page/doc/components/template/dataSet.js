import { DataSet } from 'choerodon-ui/pro';

export default ({
  pageStore, selection = false, formatMessage, bootFormatMessage,
} = {}) => ({
  selection,
  data: [],
  primaryKey: 'id',
  autoQuery: true,
  transport: {
    read: {
      url: `${pageStore.apiGateway}/document_template/template_list?organizationId=${pageStore.orgId}&baseId=${pageStore.baseId}`,
      method: 'post',
      transformRequest: (data) => {
        const {
          params, title, description, templateType,
        } = data;
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
    { name: 'title', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'introduction' }) },
    { name: 'lastUpdatedUser', type: 'object', label: bootFormatMessage({ id: 'updater' }) },
    { name: 'lastUpdateDate', type: 'string', label: bootFormatMessage({ id: 'updateTime' }) },
    { name: 'createdUser', type: 'object', label: bootFormatMessage({ id: 'creator' }) },
    { name: 'creationDate', type: 'string', label: bootFormatMessage({ id: 'creationTime' }) },
    { name: 'templateType', type: 'string', label: formatMessage({ id: 'type' }) },
  ],
  queryFields: [
    { name: 'title', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'introduction' }) },
    {
      name: 'templateType',
      type: 'string',
      label: formatMessage({ id: 'type' }),
      options: new DataSet({
        data: [{
          meaning: formatMessage({ id: 'type_custom' }),
          value: 'custom',
        }, {
          meaning: formatMessage({ id: 'type_system' }),
          value: 'sys_preset',
        }],
      }),
    },
  ],
});
