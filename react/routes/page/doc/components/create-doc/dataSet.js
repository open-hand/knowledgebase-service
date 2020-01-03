
export default function DataSetFactory({ apiGateway, templateDataSet }) {
  return {
    transport: {
      create: {
        url: `${apiGateway}/document_template/create`,
        method: 'post',
        // transformRequest: (data) => {
        //   const { params, name } = data;
        //   return JSON.stringify({
        //     contents: (params || name) ? [params || name] : [],           
        //   });
        // },
      },
    },
    fields: [
      {
        name: 'name', type: 'string', label: '文档名称', required: true, 
      },
    ],
  };
}
