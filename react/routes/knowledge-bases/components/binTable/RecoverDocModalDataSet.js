import { DataSet } from 'choerodon-ui/pro';

export default function RecoverDocModalDataSet({ knowledgeHomeStore, type }) {
  const baseListDataSet = new DataSet({
    fields: [
      { name: 'id', type: 'string' },
      { name: 'name', type: 'string' },
    ],
    data: type === 'project' ? knowledgeHomeStore.projectBaseList : knowledgeHomeStore.orgBaseList,
  });

  return {
    fields: [
      {
        name: 'baseId',
        type: 'string',
        label: '知识库地址',
        textField: 'name',
        valueField: 'id',
        options: baseListDataSet,
      },
    ],
  };
}
