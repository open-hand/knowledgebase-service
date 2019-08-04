const docServer = 'http://choerodon.io';

const pageDetail = {
  'knowledge.title': 'Knowledge Introduction',
  'knowledge.description': 'It is a platform for knowledge management and sharing for projects and organizations.',
  'knowledge.link': `${docServer}/zh/docs/user-guide/wiki/space/`,
};

const enUS = {
  refresh: 'Refresh',
  operating: 'operating',
  success: 'success',
  deleted: 'deleted',
  failed: 'failed',
  create: 'create',
  edit: 'edit',
  editor: 'editor',
  delete: 'delete',
  cancel: 'cancel',
  required: 'This field is mandatory',
  sync: 'synchronization',
  retry: 'retry',
  import: 'Import',

  'doc.create': 'Create',
  'doc.attachment': 'Attachment',
  'doc.comment': 'Comment',
  'doc.log': 'Log',

  'docHeader.attach': 'attach',
  'docHeader.comment': 'comment',
  'docHeader.log': 'log',
  'docHeader.catalog': 'catalog',
  'docHeader.share': 'Share',

  'doc.share.tip': 'You have successfully created a share link, and public sharing of the article will be open to everyone.',
  'doc.share': 'Share this article publicly',
  'doc.share.include': 'Share the subpage of this article',
  'doc.share.link': 'Share link',
  'doc.share.copy': 'Copy Link',
  'doc.import.tip': 'The imported document may not match the style in the word, please adjust it manually.',

  ...pageDetail,
};

export default enUS;
