import {
  workSpaceApiConfig,
} from '@choerodon/master';

const mapping: any = {
  name: {
    name: 'name',
    type: 'string',
    label: '名称',
  },
  attribute: {
    name: 'attribute',
    type: 'string',
    label: '属性',
  },
  creator: {
    name: 'creator',
    type: 'string',
    label: '创建者',
  },
  operation: {
    name: 'operation',
    type: 'string',
    label: '操作',
  },
};

const Index = (id: any): any => ({
  // autoCreate: true,
  autoQuery: true,
  transport: {
    read: () => ({
      ...workSpaceApiConfig.getFolderContent(id),
    }),
  },
  selection: false,
  fields: Object.keys(mapping).map((key) => {
    const item = mapping[key];
    return item;
  }),
});

export default Index;

export { mapping };
