import React from 'react';
import { Icon } from 'choerodon-ui';
import { Button } from 'choerodon-ui/pro';
import FileSaver from 'file-saver';
import FilePreview from './FilePreview';
import './index.less';

const prefixCls = 'c7n-agile-preview';
const Preview = ({
  url, fileName, modal,
}) => {
  const handleDownLoadFile = () => {
    FileSaver.saveAs(url, fileName);
  };

  const handleClose = () => {
    modal.close();
  };
  return (
    <div className={`${prefixCls}`}>
      <div className={`${prefixCls}-toolbar`}>
        <Button funcType="flat" icon="get_app" className={`${prefixCls}-header-downloadWrap`} onClick={handleDownLoadFile}>
          <span className={`${prefixCls}-header-downloadWrap-fileName`}>{decodeURIComponent(fileName)}</span>
        </Button>
        <Icon type="close" style={{ fontSize: 20, marginLeft: 20, cursor: 'pointer' }} onClick={handleClose} />
      </div>
      <div className={`${prefixCls}-content`}>
        <FilePreview url={url} />
      </div>
    </div>
  );
};

export default Preview;
