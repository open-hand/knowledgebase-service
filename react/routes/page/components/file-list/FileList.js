import React from 'react';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import { stores } from '@choerodon/boot';
import { Tooltip } from 'choerodon-ui/pro/lib';
import { getFileSuffix } from '../../../../utils';
import './FileList.less';

const { AppState } = stores;
const previewSuffix = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'gif', 'png'];

function FileList(props) {
  const { fileList, readOnly, deleteFile } = props;

  const handlePreviewClick = (service, name, fileUrl) => {
    const urlParams = AppState.currentMenuType;
    window.open(`/#/knowledge/preview?fileService=${service}&fileName=${name}&fileUrl=${fileUrl}`);
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
        <a className="c7n-agile-singleFileUpload-download" href={url}>
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
          ) : null
        }
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', marginTop: 5, flexWrap: 'wrap' }}>
      {fileList ? fileList.map(file => renderFile(file)) : null}
    </div>
  );
}

export default withRouter(FileList);
