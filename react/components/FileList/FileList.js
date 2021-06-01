import React from 'react';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { Tooltip } from 'choerodon-ui/pro/lib';
import Preview from '@choerodon/agile/lib/components/Preview';
import FileSaver from 'file-saver';
import { getFileSuffix } from '../../utils';
import './FileList.less';

import doc from './image/doc.svg';
import html from './image/html.svg';
import jpg from './image/jpg.svg';
import obj from './image/obj.svg';
import pdf from './image/pdf.svg';
import png from './image/png.svg';
import rar from './image/rar.svg';
import txt from './image/txt.svg';
import xls from './image/xls.svg';
import zip from './image/zip.svg';

const previewSuffix = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'gif', 'png'];
const suffixImgMap = new Map([
  ['doc', doc],
  ['docx', doc],
  ['html', html],
  ['jpg', jpg],
  ['jpeg', jpg],
  ['pdf', pdf],
  ['png', png],
  ['rar', rar],
  ['txt', txt],
  ['xls', xls],
  ['xlsx', xls],
  ['zip', zip],
]);
const modalKey = Modal.key();
function FileList(props) {
  const { fileList, readOnly, deleteFile } = props;

  const handleDownLoadFile = (url, fileName) => {
    FileSaver.saveAs(url, fileName);
  };

  const handlePreviewClick = (service, name, fileUrl) => {
    Modal.open({
      key: modalKey,
      title: '预览',
      footer: () => null,
      className: 'c7n-agile-preview-Modal',
      cancelText: '关闭',
      fullScreen: true,
      children: <Preview service={service} fileName={name} fileUrl={fileUrl} handleDownLoadFile={() => handleDownLoadFile(fileUrl, name)} />,
    });
  };

  function handleDeleteClick(id) {
    if (deleteFile) {
      deleteFile(id);
    }
  }

  function renderFile(file) {
    const { url, name, id } = file;
    const suffix = getFileSuffix(url);
    return (
      <div className="c7n-agile-singleFileUpload" key={id}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <img src={suffixImgMap.get(suffix) || obj} alt="doc" className="c7n-agile-singleFileUpload-img" />
          <span
            className="c7n-agile-singleFileUpload-fileName"
          >
            {name}
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {
          url && previewSuffix.includes(suffix) && (
          <span
            role="none"
            onClick={handlePreviewClick.bind(this, '', name, url)}
            style={{
              cursor: 'pointer',
            }}
          >
            <Tooltip title="预览">
              <Icon
                type="zoom_in"
                className="c7n-agile-singleFileUpload-icon"
                style={{ cursor: 'pointer', marginTop: -2 }}
              />
            </Tooltip>
          </span>
          )
        }
          {
          url && (
            <Tooltip title="下载">
              <Icon
                type="cloud_download-o"
                onClick={() => { handleDownLoadFile(url, name); }}
              />
            </Tooltip>
          )
        }
          {!readOnly && (
          <Tooltip title="删除">
            <Icon
              type="delete_sweep-o"
              onClick={() => { handleDeleteClick(id); }}
            />
          </Tooltip>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="doc-fileList">
      {fileList ? fileList.map((file) => renderFile(file)) : null}
    </div>
  );
}

export default withRouter(FileList);
