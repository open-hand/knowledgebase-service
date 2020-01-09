import { getProjectId, getOrganizationId } from '../../../../common/utils';

export default ({ type }) => ({
  autoQuery: true,
  selection: false,
  primaryKey: 'id',
  parentField: 'parentId', // table mode="tree"
  idField: 'id', // table mode="tree"
  expandField: 'expandable',
  transport: {
    read: ({ data }) => {
      let postData = { searchArgs: {} };
      if (data && Object.keys(data).length) {
        if (data.name) {
          postData.searchArgs.name = data.name;
        }
        if (data.params) {
          postData.contents = [data.params];
        }
      } else {
        postData = {};
      }
      return {
        url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/document_template/list_system_template?organizationId=${getOrganizationId()}` : `/knowledge/v1/organizations/${getOrganizationId()}/document_template/list_system_template`,
        method: 'post',
        data: postData,
      };
    },
  },
  fields: [
    { name: 'check', type: 'boolean' },
    { name: 'name', type: 'string', label: '知识库名称' },
  ],
  queryFields: [
    {
      name: 'name', type: 'string', label: '知识库/文档名称',
    },
  ],
});
