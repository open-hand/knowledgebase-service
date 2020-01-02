import { DataSet } from 'choerodon-ui/pro';
import { getOrganizationId } from '../../../../common/utils';

export default function BaseModalDataSet({ initValue = {} } = {}) {
  const rangeOptionDs = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'key', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { value: '私有', key: 'private' },
      { value: '组织下所有项目', key: 'allProjects' },
      { value: '组织下指定项目', key: 'designatedProject' },
    ],
  });
  return {
    autoCreate: true,
    data: [initValue],
    fields: [
      {
        name: 'name', type: 'string', label: '知识库名称', required: true, 
      },
      {
        name: 'description', type: 'string', label: '知识库简介',
      },
      {
        name: 'range',
        type: 'string',
        label: '公开范围',
        require: true,
        options: rangeOptionDs,
        textField: 'value',
        valueField: 'key',
      },
      {
        name: 'projectId',
        type: 'array',
        label: '指定项目', 
        required: true,      
        lookupAxiosConfig: ({ record, dataSet: ds }) => ({
          url: `/base/v1/organizations/${getOrganizationId()}/projects/all`,
        }),
        textField: 'name',
        valueField: 'id',
      },
    ],
    events: {
      update: ({
        dataSet, record, name, value, oldValue, 
      }) => {
        if (name === 'projectId') {
          record.set('projectId', []);
          record.getField('projectId').validator.reset();
        }
      },
    },
  };
}
