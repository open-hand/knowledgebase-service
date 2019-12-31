
export default function DataSetFactory({ apiGateway, repoId }) {
  return {
    autoCreate: true,    
    fields: [
      {
        name: 'name', type: 'string', label: '文档名称', required: true, 
      },  
      {
        name: 'template',
        type: 'number',
        label: '模板', 
        lookupUrl: `${apiGateway}/work_space/template?id=${repoId}`,
        textField: 'name',
        valueField: 'id',
      },
    ],
  };
}
