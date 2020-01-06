
export default function DataSetFactory({ apiGateway, templateDataSet }) {
  return {
    fields: [
      {
        name: 'title', type: 'string', label: '文档名称', required: true, 
      },
    ],
  };
}
