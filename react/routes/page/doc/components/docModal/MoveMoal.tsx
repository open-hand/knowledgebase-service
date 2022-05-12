import React, { useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Modal, Icon,
} from 'choerodon-ui/pro';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import { Table } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import { ModalProps } from 'choerodon-ui/pro/lib/modal/Modal';
import { NewTips } from '@choerodon/components';
import PageStore from '@/routes/page/stores/PageStore';
import './MoveModal.less';
import folderSvg from '@/assets/image/folder.svg';
import documentSvg from '@/assets/image/document.svg';
import wordSvg from '@/assets/image/word.svg';
import pptSvg from '@/assets/image/ppt.svg';
import pdfSvg from '@/assets/image/pdf.svg';
import txtSvg from '@/assets/image/txt.svg';
import xlsxSvg from '@/assets/image/xlsx.svg';
import mp4Svg from '@/assets/image/mp4.svg';

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
  title:string,
  flag:string,
}
const prefix = 'c7n-moveModal-table';
const Move: React.FC<Props> = ({
  store, id, refresh, modal, title, flag,
}) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);

  const initData = useCallback(() => {
    store.queryMoveTree(id).then(() => {
      const { moveTree } = store;
      setLoading(false);
      // @ts-ignore
      setSelectedRowKeys([moveTree[0] && moveTree[0].id]);
    });
  }, [store]);

  const renderItem = (value:any, rowData:any) => {
    const iconList:Record<string, string> = { folder: folderSvg, document: documentSvg };
    const fileImageList:Record<string, string> = {
      docx: wordSvg, pptx: pptSvg, pdf: pdfSvg, txt: txtSvg, xlsx: xlsxSvg, mp4: mp4Svg,
    };
    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <img src={rowData.type === 'file' ? fileImageList[rowData.fileType] : iconList[rowData.type]} alt="" style={{ marginRight: '6px' }} />
        <span title={value} className="c7n-workSpaceTree-title">{value}</span>
      </div>
    );
  };

  useEffect(() => {
    initData();
  }, [initData]);

  const getColumn = useCallback(() => [
    {
      title: '文档位置',
      dataIndex: 'name',
      key: 'name',
      render: renderItem,
    },
  ], []);

  const onSelectChange = useCallback((keys: any) => {
    setSelectedRowKeys(keys);
  }, []);

  const moveDoc = useCallback(() => (flag === 'copy' ? store.copyWorkSpace(id, selectedRowKeys[0]) : store.moveWorkSpace(selectedRowKeys[0], {
    id,
    before: true,
    targetId: 0,
  })).then(() => {
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
    hideDefaultSelections: true,
    type: 'radio',
  };
  return (
    <div>
      <div className={`${prefix}-moveTo`}>
        <Icon type="info_outline" style={{ color: '#4D90FE', marginRight: '8px' }} />
        <span className={`${prefix}-name`}>
          将
          {flag === 'copy' ? '复制' : '移动'}
          【
          {title}
          】至…
        </span>
      </div>
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
    </div>
  );
};

// @ts-ignore
const ObserverMove = injectIntl(observer(Move));
const renderHelpText = () => `注意：
  1.「文档」支持移动或复制到「文档」或「文件夹」中；
  2.「文件」仅支持移动或复制到「文件夹」中；
  3.「文件夹」仅支持移动到「文件夹」中。`;
const openMove = (props: Props) => {
  Modal.open({
    title:
  <div className={`${prefix}-container`}>
    <span className={`${prefix}-title`}>{props.flag === 'copy' ? '复制' : '移动'}</span>
    <NewTips className={`${prefix}-tip`} helpText={renderHelpText()} placement="top" />
  </div>,
    drawer: true,
    style: {
      width: 379.5,
    },
    children: <ObserverMove {...props} />,
  });
};

export default openMove;
