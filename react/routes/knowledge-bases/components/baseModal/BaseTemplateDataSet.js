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
      url: '/notify/v1/projects/28/message_settings/devops',
      method: 'get',
      transformResponse(res) {
        return {
          list: [...JSON.parse(res).notifyEventGroupList, ...JSON.parse(res).customMessageSettingList],
        };
      },
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
