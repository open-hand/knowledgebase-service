import React from 'react';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { stores } from '@choerodon/boot';
import { Tooltip } from 'choerodon-ui/pro/lib';
import Preview from '@choerodon/agile/lib/components/Preview';
import FileSaver from 'file-saver';
import { getFileSuffix } from '../../utils';
import './FileList.less';

const previewSuffix = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'gif', 'png'];
const modalKey = Modal.key();
function FileList(props) {
  const { fileList, readOnly, deleteFile } = props;

  const handleDownLoadFile = (url, fileName) => {
    alert(url, fileName);
    FileSaver.saveAs(url, fileName);
  };

  const handlePreviewClick = (service, name, fileUrl) => {
    Modal.open({
      key: modalKey,
      title: '预览',
      // style: {
      //   width: '80%',
      // },
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
    return (
      <div className="c7n-agile-singleFileUpload" key={id}>
        <span className="c7n-agile-singleFileUpload-icon">
          {url && previewSuffix.includes(getFileSuffix(url)) && (
            <Tooltip title="预览">
              <Icon
                type="zoom_in"
                style={{ cursor: 'pointer' }}
                onClick={handlePreviewClick.bind(this, '', name, url)}
              />
            </Tooltip>
          )}
        </span>
        <a className="c7n-agile-singleFileUpload-download" role="none" onClick={handleDownLoadFile.bind(this, url, name)}>
          <span className="c7n-agile-singleFileUpload-icon">
            <Tooltip title="下载">
              <Icon type="get_app" style={{ color: '#000' }} />
            </Tooltip>
          </span>
          <span className="c7n-agile-singleFileUpload-fileName">{name}</span>
        </a>
        {!readOnly
          ? (
            <Tooltip title="删除">
              <Icon
                onClick={handleDeleteClick.bind(this, id)}
                type="close"
              />
            </Tooltip>
          ) : null}
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
