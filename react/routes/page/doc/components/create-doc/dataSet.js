
export default function DataSetFactory({ apiGetway, repoId }) {
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
        lookupUrl: `${apiGetway}/work_space/template?id=${repoId}`,
        textField: 'name',
        valueField: 'id',
      },
    ],
  };
}
