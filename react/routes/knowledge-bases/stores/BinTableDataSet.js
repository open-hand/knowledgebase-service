import { DataSet } from 'choerodon-ui/pro';
import { getProjectId, getOrganizationId } from '../../../common/utils';

export default function BinTableDataSet({ type, formatMessage }) {
  const typeDataSet = new DataSet({
    autoQuery: false,
    selection: false,
    fields: [
      { name: 'key', type: 'string' },
      { name: 'value', type: 'string' },
    ],
    data: [
      { value: formatMessage({ id: 'document' }), key: 'page' },
      { value: formatMessage({ id: 'knowledge' }), key: 'base' },
      { value: formatMessage({ id: 'template' }), key: 'template' },
    ],
  });

  return {
    autoCreate: true,
    autoQuery: false,
    selection: false,
    transport: {
      read: ({ data, params }) => {
        let postData = { searchArgs: {} };
        if (data && Object.keys(data).length) {
          if (data.name) {
            postData.searchArgs.name = data.name;
          }
          if (data.type) {
            postData.searchArgs.type = data.type;
          }
          if (data.belongToBaseName) {
            postData.searchArgs.belongToBaseName = data.belongToBaseName;
          }
          if (data.params) {
            postData.contents = [data.params];
          }
        } else {
          postData = {};
        }

        return {
          url: type === 'project' ? `/knowledge/v1/projects/${getProjectId()}/recycle/page_by_options` : `/knowledge/v1/organizations/${getOrganizationId()}/recycle/page_by_options`,
          method: 'post',
          data: postData,
          params: {
            organizationId: getOrganizationId(),
            ...params,
          },
        };
      },
    },
    fields: [
      {
        name: 'name', type: 'string', label: formatMessage({ id: 'knowledge.document.name' }),
      },
      {
        name: 'belongToBaseName', type: 'string', label: formatMessage({ id: 'knowledge_base' }),
      },
      {
        name: 'type',
        type: 'string',
        label: formatMessage({ id: 'type' }),
      },
      {
        name: 'lastUpdatedBy',
        type: 'string',
        label: formatMessage({ id: 'deleter' }),
      },
      {
        name: 'lastUpdateDate',
        type: 'string',
        label: formatMessage({ id: 'delete_time' }),
      },
    ],
    queryFields: [
      {
        name: 'name', type: 'string', label: formatMessage({ id: 'knowledge.document.name' }),
      },
      {
        name: 'belongToBaseName', type: 'string', label: formatMessage({ id: 'knowledge_base' }),
      },
      {
        name: 'type',
        type: 'string',
        label: formatMessage({ id: 'type' }),
        textField: 'value',
        valueField: 'key',
        options: typeDataSet,
      },
    ],
  };
}
