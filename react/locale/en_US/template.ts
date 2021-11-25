import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeTemplate = {
  create_from_base: 'Create From This Template',
  edit: 'Edit',
  preview: 'Preview',
  manage: 'Template Management',
  name: 'Template Name',
  introduction: 'Introduction',
  type: 'Type',
  create: 'Create Template',
  type_custom: '用户自定义',
  type_system: '系统预置',
} as const;

const exportTemplate = localeAppendPrefixObjectKey({ intlPrefix: 'template' as const, intlObject: localeTemplate });
type ILocaleTemplateType = {
  ['knowledge.template']: Array<keyof typeof localeTemplate>[number]
}
export { exportTemplate };
export type { ILocaleTemplateType };
