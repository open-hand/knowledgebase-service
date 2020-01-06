import { DataSet } from 'choerodon-ui/pro';
import { getOrganizationId, getProjectId } from '../../../../common/utils';

export default function BaseModalDataSet({ initValue = {}, type } = {}) {
  const rangeOptionDs = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'key', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { value: '私有', key: 'range_private' },
      { value: '组织下所有项目', key: 'range_public' },
      { value: '组织下指定项目', key: 'range_project' },
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
        name: 'openRange',
        type: 'string',
        label: '公开范围',
        require: true,
        options: rangeOptionDs,
        textField: 'value',
        valueField: 'key',
      },
      {
        name: 'rangeProjectIds',
        type: 'number',
        label: '指定项目', 
        required: true,
        multiple: true,      
        lookupAxiosConfig: ({ record, dataSet: ds }) => ({
          url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/project_operate/list_project?organizationId=${getOrganizationId()}` : `/knowledge/v1/organizations/${getOrganizationId()}/project_operate/list_project`,
        }),
        textField: 'name',
        valueField: 'id',
      },
    ],
    events: {
      update: ({
        dataSet, record, name, value, oldValue, 
      }) => {
        if (name === 'openRange') {
          record.set('rangeProjectIds', undefined);
          record.getField('rangeProjectIds').validator.reset();
        }
      },
    },
  };
}
