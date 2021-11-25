import { localeAppendPrefixObjectKey } from '@/utils/locale';

const localeTemplate = {
  create_from_base: '基于此模板创建',
  edit: '编辑',
  preview: '预览',
  manage: '模板管理',
  name: '模板名称',
  introduction: '模板简介',
  type: '模板类型',
  create: '创建模板',
  type_custom: '用户自定义',
  type_system: '系统预置',
} as const;

const exportTemplate = localeAppendPrefixObjectKey({ intlPrefix: 'template' as const, intlObject: localeTemplate });
type ILocaleTemplateType = {
  ['knowledge.template']: Array<keyof typeof localeTemplate>[number]
}
export { exportTemplate };
export type { ILocaleTemplateType };
