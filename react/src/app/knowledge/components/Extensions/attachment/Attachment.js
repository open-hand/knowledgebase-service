/**
 * @fileoverview Implements Task counter
 * @author NHN FE Development Lab <dl_javascript@nhn.com>
 */
import axios from 'axios';
import Editor from 'tui-editor/dist/tui-editor-Editor';
import Viewer from 'tui-editor/dist/tui-editor-Viewer';
import AttachmentRender from './AttachmentRender';

/**
 * task counter extension
 * @param {Editor} editor - editor instance
 * @ignore
 */
function attachmentExtension(editor) {
  // runs while markdown-it transforms code block to HTML
  Editor.codeBlockManager.setReplacer('attachment', attachmentName => `<a href="http://www.baidu.com">${attachmentName}</a>`);
}

Editor.defineExtension('attachment', attachmentExtension);

let attachments = {};

function loadAttachment() {
  axios.get('/knowledge/v1/projects/254/page_attachment/list?pageId=2481').then((res) => {
    attachments = {};
    if (res && res.length) {
      res.forEach((item) => {
        attachments[item.name.trim()] = item.url;
      });
    }
  });
}

loadAttachment();

/**
 * task counter extension
 * @param {Viewer} viewer - viewer instance
 * @ignore
 */
function attachmentViewerExtension(viewer) {
  // runs while markdown-it transforms code block to HTML
  Viewer.codeBlockManager.setReplacer('attachment', attachmentName => `<a href=${attachments[attachmentName.trim()]} target="_blank" rel="noopener noreferrer" title=${attachmentName.trim()}>${attachmentName.trim()}</a>`);
}

Viewer.defineExtension('attachment', attachmentViewerExtension);
