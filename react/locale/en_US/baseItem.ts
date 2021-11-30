import { localeAppendPrefixObjectKey } from '@/utils/locale';

const baseItem = {
  name: 'Name',
  introduction: 'Introduction',
  no_introduction: '暂无',
  scope: '公开范围',
  setting: 'Setting',
  delete: 'Delete',
  delete_title: '确认删除',
  delete_des: '知识库“{name}”将会被移至回收站，您可以在回收站恢复此知识库。',
  move_recycle_failed: '移到回收站失败',
  update_in: '更新“{name}”于',
  project: 'Project',
  organization: 'Organization',
} as const;

const exportBaseItem = localeAppendPrefixObjectKey({ intlPrefix: 'baseItem' as const, intlObject: baseItem });
type ILocaleBaseItemType = {
  ['knowledge.baseItem']: Array<keyof typeof baseItem>[number]
}
export { exportBaseItem };
export type { ILocaleBaseItemType };
