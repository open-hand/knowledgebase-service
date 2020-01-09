
export default function DataSetFactory({ pageStore, baseTemplate = {} } = {}) {
  return {
    selection: false,
    // autoCreate: true,
    transport: {
      create: {
        url: `${pageStore.apiGateway}/document_template/create?organizationId=${pageStore.orgId}${baseTemplate.id ? `&baseTemplateId=${baseTemplate.id}` : ''}`,
        method: 'post',
        transformRequest: (([data]) => JSON.stringify({
          ...data,
          parentWorkspaceId: 0,
          baseId: pageStore.baseId,
        })),
      },
    },
    fields: [
      {
        name: 'title', type: 'string', label: '模板名称', required: true,
        //  defaultValue: baseTemplate.title,
      },
      {
        name: 'description', type: 'string', label: '模板简介', 
        // defaultValue: baseTemplate.description,
      },
    ],
  };
}
