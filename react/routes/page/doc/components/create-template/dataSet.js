
export default function DataSetFactory({ apiGateway, repoId }) {
  return {
    selection: false,
    transport: {
      create: {
        url: `${apiGateway}/users?repoId=${repoId}`,
        method: 'post',
        transformRequest: (([data]) => {
          data.roles = data.roles.map(v => ({ id: v }));
          return JSON.stringify(data);
        }),
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
