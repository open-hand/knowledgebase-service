// 反馈按钮
import React, { useEffect } from 'react';
import { Upload, Button } from 'choerodon-ui';
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
    } else {
      return false;
    }
  }

  return (
    <div className="fileUpload">
      <Upload
        className={`upload-content ${randomClassName}`}
        fileList={fileList}
        onChange={onChange}
        multiple
        beforeUpload={handleBeforeUpload}
      >
        <Button
          type="primary"
          funcType="raised"
          className="upload-btn"
          shape="circle"
          icon="file_upload"
        />
      </Upload>
    </div>
  );
};
export default FileUpload;
