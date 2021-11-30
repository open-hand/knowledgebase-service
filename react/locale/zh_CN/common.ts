import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeCommon = {
  knowledge: '知识库',
  document: '文档',
  create: '创建知识库',
  project: '本项目知识库',
  organization: '本组织知识库',
  'organization.title': '本组织知识库',
  recycle_bin: '回收站',
  'knowledge.document.name': '知识库/文档名称',
  type: '类型',
  deleter: '删除人',
  restore: '恢复',
  delete: '删除',
  delete_time: '删除时间',
  knowledge_base: '所属知识库',
  template: '模板',
  no_data: '暂无数据',
  save: '保存',
  cancel: '取消',
  comment: '评论',
  attachment: '附件',
} as const;

const exportCommon = localeAppendPrefixObjectKey({ intlPrefix: 'common' as const, intlObject: localeCommon });
type ILocaleCommonType = {
  ['knowledge.common']: Array<keyof typeof localeCommon>[number]
}
export { exportCommon };
export type { ILocaleCommonType };
