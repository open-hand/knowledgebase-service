// 反馈按钮
import React, { useEffect } from 'react';
import { Upload } from 'choerodon-ui';
import { Button } from 'choerodon-ui/pro';
import { randomWord } from '../../../../../utils';
import './FileUpload.less';

const FileUpload = ({ fileList, onChange, beforeUpload }) => {
  const randomClassName = randomWord(false, 5);
  useEffect(() => {
    const selectEle = document.querySelector(`.${randomClassName} .c7n-upload-select`);
    const fileListEle = document.querySelector(`.${randomClassName} .c7n-upload-list`);
    if (selectEle && fileListEle) {
      fileListEle.appendChild(selectEle);
    }
  });

  function handleBeforeUpload(file) {
    if (beforeUpload) {
      return beforeUpload(file);
    }
    return false;
  }

  return (
    <div className="fileUpload">
      <Upload
        className={`upload-content ${randomClassName}`}
        fileList={fileList}
        onChange={onChange}
        multiple
        // eslint-disable-next-line react/jsx-no-bind
        beforeUpload={handleBeforeUpload}
      >
        <Button
          icon="file_upload_black-o"
        >
          <span>上传附件</span>
        </Button>
      </Upload>
    </div>
  );
};
export default FileUpload;
