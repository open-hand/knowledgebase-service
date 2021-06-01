import React, {
  useCallback, useState, useRef, useEffect, useImperativeHandle,
} from 'react';
import { observer } from 'mobx-react-lite';
import {
  Button, Modal,
} from 'choerodon-ui/pro';
// @ts-ignore
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { ModalProps } from 'choerodon-ui/pro/lib/modal/Modal';
import PageStore from '@/routes/page/stores/PageStore';
import WorkSpaceSelect from '@/components/WorkSpaceSelect';

interface IModalProps extends ModalProps {
  handleOk: (promise: () => Promise<boolean>) => Promise<void>,
  handleCancel: (promise: () => Promise<boolean>) => Promise<void>,
  close: (destroy?: boolean) => void,
  update: (modalProps: ModalProps) => void
}
interface Props {
  pageStore: PageStore
  originSelectId: string | number | boolean
  originData: any
  pathModalRef: React.MutableRefObject<{ selectId: number }>
  modal: IModalProps
  selectId: number | string,
  setSelectId: (id: number | string) => void,
  setOriginSelectId: (id: number | string | boolean) => void,
  currentSelectId: number | string
  setCurrentSelectId: (id: number | string) => void
}

const Path: React.FC<Props> = ({
  pageStore, originSelectId, originData, modal, pathModalRef, selectId, setSelectId, setOriginSelectId, currentSelectId, setCurrentSelectId,
}) => {
  const {
    getImportWorkSpace: spaceData,
    getImportDoc: importDoc,
    getImportTitle: importTitle,
  } = pageStore;

  const handleSpaceClick = useCallback((data, id) => {
    pageStore.setImportWorkSpace(data);
    setCurrentSelectId(id || 0);
  }, [pageStore, setCurrentSelectId]);

  const handleSpaceChange = useCallback((data) => {
    pageStore.setImportWorkSpace(data);
  }, [pageStore]);

  const handlePathChange = useCallback(() => {
    setSelectId((currentSelectId || 0) as number);
    modal?.close();
    return Promise.resolve(true);
  }, [currentSelectId, modal, setSelectId]);

  const handlePathCancel = useCallback(() => {
    pageStore.setImportWorkSpace(originData);
    // @ts-ignore
    setSelectId(originSelectId);
    setCurrentSelectId(0);
    modal?.close();
  }, [modal, originData, originSelectId, pageStore, setCurrentSelectId, setSelectId]);

  useEffect(() => {
    // @ts-ignore
    modal.handleOk(handlePathChange);
  }, [handlePathChange, modal]);

  useEffect(() => {
    // @ts-ignore
    modal.handleCancel(handlePathCancel);
  }, [handlePathCancel, modal]);

  console.log(currentSelectId ?? selectId);
  return (
    <>
      {
        // @ts-ignore
      spaceData && spaceData.items[spaceData.rootId].children.length
        ? (
          <WorkSpaceSelect
            data={spaceData}
            selectId={currentSelectId ?? selectId}
            onClick={handleSpaceClick}
            onExpand={handleSpaceChange}
            onCollapse={handleSpaceChange}
          />
        ) : (
          <span>当前无父级文档可选，默认创建在根节点。</span>
        )
}
    </>
  );
};

// @ts-ignore
const ObserverPath = injectIntl(observer(Path));

const openPath = (props: Props) => {
  Modal.open({
    title: '文档创建位置',
    children: <ObserverPath {...props} />,
  });
};

export default openPath;
