
export default function DataSetFactory({ pageStore }) {
  return {
    selection: false,
    transport: {
      create: {
        url: `${pageStore.apiGateway}/document_template/create?organizationId=${pageStore.orgId}`,
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
      },
      {
        name: 'description', type: 'string', label: '模板简介',
      },
    ],
  };
}
