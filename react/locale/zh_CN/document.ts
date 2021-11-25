import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeDocument = {
  recent_updates: '最近更新',
  all_document: '所有文档',
  template_manage: '模板管理',
  create: '创建文档',
  more_actions: '更多操作',
  move: '移动',
  operation_history: '操作历史',
  version_comparison: '版本对比',
  share: '分享',
  search: '搜索',
  update_time: '最近更新',
  create_template: '创建模板',
  click: '点击',
  'empty.title': '没有任何知识文档',
  'empty.des': '按钮开启你的知识管理。',
  name: '文档标题',
  last_edit: '最近编辑',
} as const;

const exportDocument = localeAppendPrefixObjectKey({ intlPrefix: 'document' as const, intlObject: localeDocument });
type ILocaleDocumentType = {
  ['knowledge.document']: Array<keyof typeof localeDocument>[number]
}
export { exportDocument };
export type { ILocaleDocumentType };
