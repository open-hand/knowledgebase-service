import React, { useContext, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import FileList from '../../routes/page/components/file-list';

function DocAttachment(props) {
  const { store } = props;
  const [visible, setVisivble] = useState(true);
  const fileList = store.getFileList;

  function handleClick() {
    setVisivble(!visible);
  }

  function handleDeleteFile(id) {
    store.deleteFile(id);
  }

  return (
    <div style={{ padding: '10px 0px', borderBottom: '1px solid #d8d8d8' }}>
      <Icon
        style={{ marginLeft: 10, verticalAlign: 'top', marginRight: 5, cursor: 'pointer' }}
        onClick={handleClick}
        type={visible ? 'expand_less' : 'expand_more'}
      />
      {`附件 (${fileList.length})`}
      {visible
        ? <FileList fileList={fileList} deleteFile={handleDeleteFile} />
        : null
      }
    </div>
  );
}

export default observer(DocAttachment);
