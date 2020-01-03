import { getProjectId, getOrganizationId } from '../../../../common/utils';

export default () => ({
  autoQuery: true,
  selection: false,
  primaryKey: 'id',
  parentField: 'groupId', // table mode="tree"
  idField: 'id', // table mode="tree"
  expandField: 'expandable',
  // defaultRowExpanded: true,
  //   checkField: 'ischecked',
  transport: {
    read: ({ data, params, dataSet }) => ({
      url: `/knowledge/v1/projects/${getProjectId()}/document_template/list_system_template?organizationId=${getOrganizationId()}`,
      method: 'post',
      data,
    }),
  },
  fields: [
    { name: 'check', type: 'boolean' },
    { name: 'name', type: 'string', label: '知识库名称' },
  ],
  events: {
    load: ({ dataSet }) => {
        
    },
    select: ({ dataSet, record }) => {
        
    },
    unselect: ({ dataSet, record }) => {
       
    },
  },
});
