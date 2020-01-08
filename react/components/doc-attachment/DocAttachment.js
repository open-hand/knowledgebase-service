import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import FileList from '../FileList';
import './DocAttachment.less';

function DocAttachment(props) {
  const { store, readOnly } = props;
  const [visible, setVisivble] = useState(false);
  const fileList = store.getFileList;

  function handleClick() {
    setVisivble(!visible);
  }

  function handleDeleteFile(id) {
    store.deleteFile(id);
  }

  return (
    <div className="doc-attachment">
      <Icon
        className="doc-attachment-expend"
        onClick={handleClick}
        type={visible ? 'expand_less' : 'expand_more'}
      />
      {`附件 (${fileList.length})`}
      {visible
        ? <FileList fileList={fileList} deleteFile={handleDeleteFile} readOnly={readOnly} />
        : null}
    </div>
  );
}

export default observer(DocAttachment);
