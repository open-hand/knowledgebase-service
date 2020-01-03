
export default function DataSetFactory({ apiGateway, baseId }) {
  return {
    selection: false,
    transport: {
      create: {
        url: `${apiGateway}/document_template/create`,
        method: 'post',
        // transformRequest: (([data]) => {
        //   data.roles = data.roles.map(v => ({ id: v }));
        //   return JSON.stringify(data);
        // }),
      },
    },
    fields: [
      {
        name: 'name', type: 'string', label: '模板名称', required: true,
      },
      {
        name: 'description', type: 'string', label: '模板简介',
      },
    ],
  };
}
