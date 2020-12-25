import { DataSet } from 'choerodon-ui/pro';
import { getOrganizationId, getProjectId } from '../../../../common/utils';

export default function BaseModalDataSet({ initValue = {}, type } = {}) {
  if (initValue.rangeProject) {
    initValue.rangeProjectIds = initValue.rangeProject.split(',').map((id) => id);
  }

  const rangeOptionDs = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'text', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { text: type === 'project' ? '项目私有' : '组织私有', value: 'range_private' },
      { text: '组织下公开', value: 'range_public' },
      { text: type === 'project' ? '指定项目' : '指定组织下项目', value: 'range_project' },
    ],
  });
  return {
    autoCreate: true,
    data: initValue.name ? [{ ...initValue }] : undefined,
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
        textField: 'text',
        valueField: 'value',
        defaultValue: 'range_private',
      },
      {
        name: 'rangeProjectIds',
        label: '指定项目',
        required: true,
        multiple: true,
        lookupAxiosConfig: () => ({
          url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/project_operate/list_project?organizationId=${getOrganizationId()}` : `/knowledge/v1/organizations/${getOrganizationId()}/project_operate/list_project`,
        }),
        textField: 'name',
        valueField: 'id',
      },
    ],
    events: {
      update: ({ record, name }) => {
        if (name === 'openRange') {
          record.set('rangeProjectIds', undefined);
          record.getField('rangeProjectIds').validator.reset();
        }
      },
    },
  };
}
