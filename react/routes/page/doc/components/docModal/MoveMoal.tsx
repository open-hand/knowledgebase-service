import React, { useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Modal,
} from 'choerodon-ui/pro';
import { Table } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import { ModalProps } from 'choerodon-ui/pro/lib/modal/Modal';
import PageStore from '@/routes/page/stores/PageStore';
import './MoveModal.less';

interface IModalProps extends ModalProps {
  handleOk: (promise: () => Promise<boolean>) => Promise<void>,
  handleCancel: (promise: () => Promise<boolean>) => Promise<void>,
  close: (destroy?: boolean) => void,
  update: (modalProps: ModalProps) => void
}

interface Props {
  store: PageStore
  id: string
  refresh: (id?: string) => void
  modal: IModalProps
}

const Move: React.FC<Props> = ({
  store, id, refresh, modal,
}) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);

  const initData = useCallback(() => {
    store.queryMoveTree().then(() => {
      const { moveTree } = store;
      setLoading(false);
      // @ts-ignore
      setSelectedRowKeys([moveTree[0] && moveTree[0].id]);
    });
  }, [store]);

  useEffect(() => {
    initData();
  }, [initData]);

  const getColumn = useCallback(() => [
    {
      title: '文档位置',
      dataIndex: 'name',
      key: 'name',
    },
  ], []);

  const onSelectChange = useCallback((keys) => {
    setSelectedRowKeys(keys);
  }, []);

  const getCheckboxProps = useCallback((record) => ({
    disabled: record.route.split('.').indexOf(String(id)) !== -1,
    name: record.name,
  }), [id]);

  const moveDoc = useCallback(() => store.moveWorkSpace(selectedRowKeys[0], {
    id,
    before: true,
    targetId: 0,
  }).then(() => {
    modal?.close();
    refresh(id);
  }).catch(() => {
  }), [id, modal, refresh, selectedRowKeys, store]);

  useEffect(() => {
    modal?.handleOk(moveDoc);
  }, [modal, moveDoc]);

  const data = store.getMoveTree;
  const workSpace = store.getWorkSpace;
  // @ts-ignore
  const spaceData = workSpace[store.spaceCode].data;
  const rowSelection = {
    selectedRowKeys,
    onChange: onSelectChange,
    getCheckboxProps,
    hideDefaultSelections: true,
    type: 'radio',
  };

  return (
    <Table
      className="c7n-moveModal-table"
        // @ts-ignore
      rowSelection={rowSelection}
      dataSource={data}
      columns={getColumn()}
      rowKey={(record) => record.id}
      pagination={false}
      loading={loading}
      filterBar={false}
      defaultExpandedRowKeys={[spaceData.rootId]}
      scroll={{ y: 400 }}
    />
  );
};

// @ts-ignore
const ObserverMove = injectIntl(observer(Move));

const openMove = (props: Props) => {
  Modal.open({
    title: '移动',
    children: <ObserverMove {...props} />,
  });
};

export default openMove;
