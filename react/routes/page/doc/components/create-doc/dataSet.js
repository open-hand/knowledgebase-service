
export default function DataSetFactory({ apiGateway, templateDataSet }) {
  return {
    autoCreate: true,
    fields: [
      {
        name: 'title', type: 'string', label: '文档名称', required: true, 
      },
    ],
  };
}
