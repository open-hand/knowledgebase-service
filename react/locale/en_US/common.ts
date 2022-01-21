import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeCommon = {
  knowledge: 'Knowledge Base',
  document: 'Document',
  create: 'Create',
  project: 'Knowledge Base',
  organization: 'Shared Knowledge Base',
  'organization.title': 'Knowledge Base',
  recycle_bin: 'Recycle Bin',
  'knowledge.document.name': 'Name',
  type: 'Type',
  deleter: 'Deleter',
  restore: 'Restore',
  delete: 'Delete',
  delete_time: 'Delete Time',
  knowledge_base: 'Knowledge Base',
  template: 'Template',
  no_data: 'No Data',
  save: 'Save',
  cancel: 'Cancel',
  comment: 'Comment',
  attachment: 'Attachment',
  'share.cancel.des': '该文档已取消对外分享，您不可查看文档内容',
} as const;

const exportCommon = localeAppendPrefixObjectKey({ intlPrefix: 'common' as const, intlObject: localeCommon });
type ILocaleCommonType = {
  ['knowledge.common']: Array<keyof typeof localeCommon>[number]
}
export { exportCommon };
export type { ILocaleCommonType };
