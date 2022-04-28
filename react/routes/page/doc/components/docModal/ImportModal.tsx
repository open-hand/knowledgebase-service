import React, { useCallback, useState, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Button, Modal,
} from 'choerodon-ui/pro';
// @ts-ignore
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { ModalProps } from 'choerodon-ui/pro/lib/modal/Modal';
import PageStore from '@/routes/page/stores/PageStore';

interface IModalProps extends ModalProps {
  handleOk: (promise: () => Promise<boolean>) => Promise<void>,
  handleCancel: (promise: () => Promise<boolean>) => Promise<void>,
  close: (destroy?: boolean) => void,
  update: (modalProps: ModalProps) => void
}
interface Props {
  store: PageStore
  modal: IModalProps
}

const Import: React.FC<Props> = ({ store, modal }) => {
  const [uploading, setUploading] = useState<boolean>(false);
  const uploadInput = useRef(null);

  const importWord = useCallback(() => {
    // @ts-ignore
    uploadInput.current?.click();
  }, []);

  const upload = useCallback((file: any) => {
    if (!file) {
      Choerodon.prompt('请选择文件');
      return;
    }
    if (file.size > 1024 * 1024 * 10) {
      Choerodon.prompt('文件不能超过10M');
      return;
    }
    const formData = new FormData();
    formData.append('file', file);
    setUploading(true);
    store.importWord(formData).then((res: any) => {
      if (res.failed && res.code === 'Error reading from the stream (no bytes available)') {
        store.setImportDoc('');
      } else {
        store.setImportDoc(res.toString());
      }
      if (file.name) {
        const nameList = file.name.split('.');
        nameList.pop();
        store.setImportTitle(nameList.join());
      }
      setUploading(false);
      modal?.close();
      store.setImportMode(true);
    }).catch((error: any) => {
      setUploading(false);
      console.log(error);
    });
  }, [modal, store]);

  const beforeUpload = useCallback((e: any) => {
    if (e.target.files[0]) {
      upload(e.target.files[0]);
    }
  }, [upload]);

  return (
    <>
      <FormattedMessage id="doc.import.tip" />
      <div style={{ marginTop: 10 }}>
        <Button
          loading={uploading}
          onClick={() => importWord()}
          style={{ marginBottom: 2 }}
          icon="archive-o"
        >
          <span>导入文档</span>
        </Button>
        <input
          ref={uploadInput}
          type="file"
          onChange={beforeUpload}
          style={{ display: 'none' }}
          accept=".docx"
        />
      </div>
    </>
  );
};

// @ts-ignore
const ObserverImport = injectIntl(observer(Import));

const openImport = (props: Props) => {
  Modal.open({
    title: 'Word文档导入',
    children: <ObserverImport {...props} />,
    footer: (okBtn: any, cancelBtn: any) => cancelBtn,
  });
};

export default openImport;
