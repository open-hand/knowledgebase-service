import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Choerodon,
  Action,
  workSpaceApi,
} from '@choerodon/master';
import {
  Table, Tooltip, Modal, TextField, Form,
} from 'choerodon-ui/pro';
import TimeAgo from 'timeago-react';
import folder from '@/assets/image/folder.svg';
import document from '@/assets/image/document.svg';
import xlsx from '@/assets/image/xlsx.svg';
import doc from '@/assets/image/word.svg';
import other from '@/assets/image/mp4.svg';
import pdf from '@/assets/image/pdf.svg';
import ppt from '@/assets/image/ppt.svg';
import txt from '@/assets/image/txt.svg';

import {
  TREE_FOLDER,
  TREE_DOC,
  TREE_FILE,
} from '../../CONSTANTS';
import { mapping } from './stores/tableDataSet';
import { useStore } from './stores';

import './index.less';

const prefix = 'c7ncd-treeFolder';

const Index = observer(() => {
  const {
    TableDataSet,
    data,
    onDelete,
  } = useStore();

  const {
    data: {
      title,
    },
  } = data;

  const getImage = (type: any, name?: any) => {
    switch (type) {
      case TREE_FOLDER: {
        return folder;
        break;
      }
      case TREE_DOC: {
        return document;
        break;
      }
      case TREE_FILE: {
        if (name.includes('xlsx')) {
          return xlsx;
        } if (name.includes('doc')) {
          return doc;
        } if (name.includes('pdf')) {
          return pdf;
        } if (name.includes('ppt')) {
          return ppt;
        } if (name.includes('txt')) {
          return txt;
        }
        return other;
        break;
      }
      default: {
        return other;
        break;
      }
    }
  };

  const renderName = ({ record, text }: any) => (
    <Tooltip title={record?.get('name')}>
      <div className={`${prefix}-name`}>
        <img src={getImage(record.get('type'), record?.get('name'))} alt="" />
        <span>{record?.get('name')}</span>
      </div>
    </Tooltip>
  );

  const renderAttribute = ({ record }: any) => {
    const type = record?.get('type');
    const subFiles = record?.get('subFiles');
    const subDocuments = record?.get('subDocuments');
    const subFolders = record?.get('subFolders');
    const fileSize = record?.get('fileSize');
    switch (type) {
      case TREE_FOLDER: {
        if (subFiles || subDocuments || subFolders) {
          return (
            <>
              <span>{subFolders ? `含${subFolders}个子文件夹` : ''}</span>
              <span>{subFiles ? `含${subFiles}个文件` : ''}</span>
              <span>{subDocuments ? `含${subDocuments}个文档` : ''}</span>
            </>
          );
        }
        return '-';
        break;
      }
      case TREE_FILE: {
        // @ts-ignore
        const size = parseInt(fileSize / 1024 / 1024, 10);
        return (
          <span>{`文件大小: ${size}M`}</span>
        );
        break;
      }
      case TREE_DOC: {
        return (
          <span>{!subFiles ? '-' : `含${subFiles}个子文档`}</span>
        );
        break;
      }
      default: {
        return '';
        break;
      }
    }
  };

  const renderCreator = ({ record }: any) => {
    const createUser = record?.get('createUser');
    const {
      imageUrl,
      realName,
    } = createUser;
    return (
      <div className={`${prefix}-creator`}>
        <img
          style={{
            width: 18,
            borderRadius: '50%',
          }}
          src={imageUrl}
          alt=""
        />
        <span>{realName}</span>
      </div>

    );
  };

  const renderOperation = ({ record }: any) => {
    const lastUpdatedUser = record?.get('lastUpdatedUser');
    const lastUpdateDate = record?.get('lastUpdateDate');
    const {
      imageUrl,
      realName,
    } = lastUpdatedUser;
    return (
      <Tooltip title={(
        <span>
          {realName}
          更新于
          <TimeAgo
            datetime={lastUpdateDate}
            locale={Choerodon.getMessage('zh_CN', 'en')}
          />
        </span>
      )}
      >
        <div className={`${prefix}-operation`}>
          <img
            style={{
              width: 18,
              borderRadius: '50%',
            }}
            src={imageUrl}
            alt=""
          />
          <span>
            {realName}
          </span>
          <span>
            更新于
          </span>
          <TimeAgo
            datetime={lastUpdateDate}
            locale={Choerodon.getMessage('zh_CN', 'en')}
          />
        </div>
      </Tooltip>
    );
  };

  const refresh = () => {
    TableDataSet?.query();
  };

  const renderAction = ({ record }: any) => (
    <Action
      data={[{
        text: '重命名',
        action: () => {
          Modal.open({
            title: '重命名',
            children: (
              <ReNameCom
                data={record.toData()}
                refresh={refresh}
              />
            ),
          });
        },
      }, {
        text: '删除',
        action: () => {
          onDelete(record?.get('id'), record?.get('name'), 'admin', refresh);
        },
      }]}
    />
  );

  return (
    <div className={prefix}>
      <p className={`${prefix}-title`}>{title}</p>
      <Table queryBar={'none' as any} dataSet={TableDataSet}>
        <Table.Column
          name={mapping.name.name}
          renderer={renderName}
        />
        <Table.Column
          renderer={renderAction}
        />
        <Table.Column
          name={mapping.attribute.name}
          renderer={renderAttribute}
        />
        <Table.Column
          name={mapping.creator.name}
          renderer={renderCreator}
        />
        <Table.Column
          name={mapping.operation.name}
          renderer={renderOperation}

        />
      </Table>
    </div>
  );
});

const ReNameCom = observer(({ data, modal, refresh }: any) => {
  const [title, setTitle] = useState('');
  const [suffix, setSuffix] = useState('');

  useEffect(() => {
    const list = data?.name?.split('.');
    if (list.length > 1) {
      setSuffix(list[list.length - 1]);
      list.pop();
      setTitle(list.join('.'));
    } else {
      setTitle(list[0]);
    }
  }, []);

  const handleOk = async () => {
    if ([TREE_FOLDER, TREE_DOC].includes(data?.type)) {
      try {
        await workSpaceApi.rename(data?.id, title);
        refresh && refresh();
      } catch (e) {
        console.log(e);
      }
    }
  };

  modal.handleOk(handleOk);

  return (
    <div>
      {/* @ts-ignore */}
      <TextField
        style={{
          width: '100%',
        }}
        autoFocus
        value={title}
        onChange={(value) => {
          setTitle(value);
        }}
      />
    </div>
  );
});

export default Index;
