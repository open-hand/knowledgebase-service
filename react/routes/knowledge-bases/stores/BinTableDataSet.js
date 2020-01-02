export default function BinTableDataSet() {
  return {
    autoCreate: true,
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
  };
}
