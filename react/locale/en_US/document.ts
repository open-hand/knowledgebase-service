import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeDocument = {
  recent_updates: 'Recent Updates',
  all_document: 'All Documents',
  template_manage: 'Template Manage',
  create: 'Create Document',
  more_actions: 'More Actions',
  move: 'Move',
  operation_history: 'Operation History',
  version_comparison: 'Version Comparision',
  share: 'Share',
  search: 'Search',
  update_time: 'Update Time',
  create_template: 'Create Template',
  click: 'Click the ',
  'empty.title': 'No Documents',
  'empty.des': 'button to start your knowledge management',
  name: 'Name',
  last_edit: 'Last Edit',
} as const;

const exportDocument = localeAppendPrefixObjectKey({ intlPrefix: 'document' as const, intlObject: localeDocument });
type ILocaleDocumentType = {
  ['knowledge.document']: Array<keyof typeof localeDocument>[number]
}
export { exportDocument };
export type { ILocaleDocumentType };
