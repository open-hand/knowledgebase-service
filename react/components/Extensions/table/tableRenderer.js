/**
* @fileoverview Implements tableRenderer
* @author NHN FE Development Lab <dl_javascript@nhn.com>
*/

/**
 * Create cell html.
 * @param {object} cell - cell data of table base data
 * @returns {string}
 * @private
 */
function createCellHtml(cell) {
  let attrs = cell.colspan > 1 ? ` colspan="${cell.colspan}"` : '';
  attrs += cell.rowspan > 1 ? ` rowspan="${cell.rowspan}"` : '';
  attrs += cell.align ? ` align="${cell.align}"` : '';

  return `<${cell.nodeName}${attrs}>${cell.content}</${cell.nodeName}>`;
}

/**
 * Create html for thead or tbody.
 * @param {Array.<Array.<object>>} trs - tr list
 * @param {string} wrapperNodeName - wrapper node name like THEAD, TBODY
 * @returns {string}
 * @private
 */
function createTheadOrTbodyHtml(trs, wrapperNodeName) {
  let html = '';

  if (trs.length) {
    html = trs.map((tr) => {
      const tdHtml = tr.map(createCellHtml).join('');

      return `<tr>${tdHtml}</tr>`;
    }).join('');
    html = `<${wrapperNodeName}>${html}</${wrapperNodeName}>`;
  }

  return html;
}

/**
 * Create table html.
 * @param {Array.<Array.<object>>} renderData - table data for render
 * @returns {string}
 * @private
 */
function createTableHtml(renderData) {
  const thead = [renderData[0]];
  const tbody = renderData.slice(1);
  const theadHtml = createTheadOrTbodyHtml(thead, 'THEAD');
  const tbodyHtml = createTheadOrTbodyHtml(tbody, 'TBODY');
  const className = renderData.className ? ` class="${renderData.className}"` : '';

  return `<table${className}>${theadHtml + tbodyHtml}</renderData>`;
}

export default {
  createTableHtml,
};
