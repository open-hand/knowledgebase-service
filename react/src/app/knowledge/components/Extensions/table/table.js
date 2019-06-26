/**
* @fileoverview Implements table extension
* @author NHN FE Development Lab <dl_javascript@nhn.com>
*/
import $ from 'jquery';

import Editor from '../editorProxy';
import './langs';
import createMergedTable from './mergedTableCreator';
import toMarkRenderer from './toMarkRenderer';

/**
 * Change html by onChangeTable function.
 * @param {string} html - original html
 * @param {function} onChangeTable - function for changing html
 * @returns {string}
 * @private
 */
function changeHtml(html, onChangeTable) {
  const $tempDiv = $(`<div>${html}</div>`);
  const $tables = $tempDiv.find('table');

  if ($tables.length) {
    $tables.get().forEach((tableElement) => {
      const changedTableElement = onChangeTable(tableElement);

      $(tableElement).replaceWith(changedTableElement);
    });

    html = $tempDiv.html();
  }

  return html;
}

/**
 * Bind events.
 * @param {object} eventManager - eventManager instance
 * @private
 */
function bindEvents(eventManager) {
  eventManager.listen('convertorAfterMarkdownToHtmlConverted', html => changeHtml(html, createMergedTable));
}

/**
 * table extension
 * @param {Editor} editor - editor instance
 * @ignorex
 */
function tableExtension(editor) {
  const { eventManager } = editor;

  editor.toMarkOptions = editor.toMarkOptions || {};
  editor.toMarkOptions.renderer = toMarkRenderer;
  bindEvents(eventManager);
}

Editor.defineExtension('table', tableExtension);
