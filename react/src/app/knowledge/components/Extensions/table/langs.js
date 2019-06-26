import Editor from 'tui-editor/dist/tui-editor-Editor';

/**
 * 官方多语言处理存在引用问题，zh->zh_CN->zh(tw)，导致zh_CN被zh_TW覆盖
 */
const { i18n } = Editor;
if (i18n) {
  i18n.setLanguage(['zh', 'zh_CN'], {
    'Merge cells': '合并单元格',
    'Unmerge cells': '取消合并单元格',
    'Cannot change part of merged cell': '无法更改合并单元格的一部分。',
    'Cannot paste row merged cells into the table header': '无法将行合并单元格粘贴到标题中。',
  });
}
