import { getProjectId, getOrganizationId } from '../../../../common/utils';

export default ({ type }) => ({
  autoQuery: true,
  selection: false,
  primaryKey: 'id',
  parentField: 'parentId', // table mode="tree"
  idField: 'id', // table mode="tree"
  expandField: 'expandable',
  transport: {
    read: ({ data, params, dataSet }) => ({
      url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/document_template/list_system_template?organizationId=${getOrganizationId()}` : `/knowledge/v1/organizations/${getOrganizationId()}/document_template/list_system_template`,
      method: 'post',
      data,
    }),
  },
  fields: [
    { name: 'check', type: 'boolean' },
    { name: 'name', type: 'string', label: '知识库名称' },
  ],
});
