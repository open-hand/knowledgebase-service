import React, {
  useContext, useState, useImperativeHandle, useCallback,
} from 'react';
import { observer } from 'mobx-react-lite';
import { mutateTree } from '@atlaskit/tree';
import {
  Collapse, Icon, Menu, Dropdown,
} from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';
import useFormatMessage from '@/hooks/useFormatMessage';
import WorkSpaceTree from '../../../../components/WorkSpaceTree';
import Store from '../../stores';
import Section from './Section';
import './WorkSpace.less';
import pickUp from '@/assets/image/pickUp.svg';
import add from '@/assets/image/add.svg';
import { createOrgBase } from '@/api/knowledgebaseApi';
import uploadImage from '@/utils';
import folderSvg from '@/assets/image/folder.svg';
import documentSvg from '@/assets/image/document.svg';
import importFileSvg from '@/assets/image/importFile.svg';
import uploadFileSvg from '@/assets/image/uploadFile.svg';

const { Panel } = Collapse;

function WorkSpace(props) {
  const {
    pageStore, history, type: levelType,
  } = useContext(Store);
  const {
    onClick, onCopy, onMove, onSave, onDelete, onCreate, onCancel, readOnly, forwardedRef, onRecovery, onCreateDoc, importOnline, onUpload, itemUpload,
  } = props;
  const [openKeys, setOpenKeys] = useState(['pro', 'org', 'recycle']);
  const formatMessage = useFormatMessage('knowledge.document');
  const selectId = pageStore.getSelectId;
  const { section } = pageStore;
  const prefix = 'c7n-workSpace';
  /**
   * 点击空间
   * @param newTree 变化后空间
   * @param clickId 本次点击项
   * @param treeCode 本次点击空间类别code
   * @param lastClickId 上次选中项
   */
  function handleSpaceClick(newTree, item, treeCode, lastClickId) {
    if (pageStore.getShareVisible) {
      pageStore.setShareVisible(false);
    }
    const spaceCode = pageStore.getSpaceCode;
    const workSpace = pageStore.getWorkSpace;
    if (spaceCode && treeCode !== spaceCode) {
      const newSpace = mutateTree(workSpace[spaceCode].data, lastClickId, { isClick: false });
      pageStore.setWorkSpaceByCode(spaceCode, newSpace);
    }
    pageStore.setWorkSpaceByCode(treeCode, newTree);
    //  pageStore.setSpaceCode(treeCode);
    pageStore.setSelectId(item.id);
    pageStore.setSelectItem(item);
    if (onClick) {
      onClick(item.id);
    }
  }

  /**
   * 空间拖拽回调
   * @param newTree 拖拽后的空间
   * @param source
   * @param destination
   * @param code
   */
  function handleSpaceDragEnd(newTree, source, destination, code) {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    // 被拖动
    const sourceId = spaceData.items[source.parentId].children[source.index];
    const destId = destination.parentId;
    const destItems = spaceData.items[destination.parentId].children;
    let before = true;
    let targetId = 0;
    // 计算拖动情况
    if (destination.index) {
      before = false;
      targetId = destItems[destination.index - 1];
    } else if (destination.index === 0 && destItems.length) {
      targetId = destItems[destination.index];
    } else if (destItems.length) {
      before = false;
      targetId = destItems[destItems.length - 1];
    }
    pageStore.moveWorkSpace(destId, {
      id: sourceId,
      before,
      targetId,
    }).then(() => {
      if (sourceId === pageStore.getSelectId) {
        pageStore.loadDoc(sourceId);
      }
    });
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function updateWorkSpace(newTree, code) {
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function handleClickAllNode() {
    pageStore.setSection('tree');
    const workSpace = pageStore.getWorkSpace;
    const spaceCode = pageStore.getSpaceCode;
    const currentSelectId = pageStore.getSelectId;
    const objectKeys = Object.keys(workSpace[spaceCode].data.items);
    const firstNode = workSpace[spaceCode].data.items[objectKeys[0]];
    const docData = pageStore.getDoc;
    if (currentSelectId) {
      onClick(currentSelectId);
    } else if (firstNode.id !== firstNode.parentId) {
      const newSpace = mutateTree(workSpace[spaceCode].data, firstNode.id, { isClick: true });
      pageStore.setWorkSpaceByCode(spaceCode, newSpace);
      pageStore.setSelectId(firstNode.id);
      onClick(firstNode.id);
    } else if (docData && docData.id && !objectKeys.includes(docData.id)) {
      pageStore.setDoc(false);
    }
  }
  const handlePickUpAll = (space) => { // 全部收起和展开  展开时不展开一级目录
    pageStore.loadWorkSpaceAll();
  };
  const createFolder = (space, e) => {
    e.domEvent.stopPropagation();
    onCreate(space.data.items[space.data.rootId]);
  };
  const handleCreate = useCallback((e) => {
    // @ts-ignore
    e.domEvent.stopPropagation();
    onCreateDoc();
  }, []);
  const handleImport = useCallback((e) => {
    // @ts-ignore
    e.domEvent.stopPropagation();
    importOnline();
  }, []);
  const handleAddClickMenu = (e, item) => {
    switch (e.key) {
      case 'createFolder':
        if (createFolder) {
          createFolder(item, e);
        }

        break;
      case 'createDocument':
        if (handleCreate) {
          handleCreate(e);
        }
        break;
      case 'import':
        if (handleImport) {
          handleImport(e);
        }
        break;
      case 'upload':
        if (onUpload) {
          onUpload(item.data.rootId, e);
        }
        break;
      default:
        break;
    }
  };
  const handleMenu = (space) => (
    <Menu onClick={(e) => handleAddClickMenu(e, space)}>
      <Menu.Item key="createDocument">
        <img src={documentSvg} alt="" className={`${prefix}-action-image`} />
        创建文档
      </Menu.Item>
      <Menu.Item key="upload">
        <img src={uploadFileSvg} alt="" style={{ marginRight: '6px' }} className={`${prefix}-action-image`} />
        上传本地文件
      </Menu.Item>
      <Menu.Item key="import">
        <img src={importFileSvg} alt="" style={{ marginRight: '6px' }} className={`${prefix}-action-image`} />
        导入为在线文档
      </Menu.Item>
      <Menu.Item key="createFolder">
        <img src={folderSvg} alt="" style={{ marginRight: '6px' }} className={`${prefix}-action-image`} />
        创建文件夹
      </Menu.Item>
    </Menu>
  );
  function renderPanel() {
    const panels = [];
    const workSpace = pageStore.getWorkSpace;
    const workSpaceKeys = Object.keys(workSpace);
    workSpaceKeys.forEach((key) => {
      const space = workSpace[key];
      const spaceData = space.data;
      if (spaceData.items && spaceData.items[spaceData.rootId] && spaceData.items[spaceData.rootId].children) {
        panels.push(
          <Panel
            header={(
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <Icon type="chrome_reader_mode" style={{ color: 'var(--primary-color)', marginLeft: 15, marginRight: 10 }} />
                <span role="none" onClick={() => handleClickAllNode()}>所有文档/文件</span>
                <Dropdown overlay={handleMenu(space)} placement="bottomLeft" trigger={['click']}>
                  <img
                    src={add}
                    style={{ marginRight: 6, marginLeft: 'auto' }}
                    onClick={(e) => e.stopPropagation()}
                    alt=""
                    role="none"
                  />
                </Dropdown>
                <img
                  style={{ marginRight: 16 }}
                  src={pickUp}
                  onClick={() => handlePickUpAll(space)}
                  alt=""
                  role="none"
                />

              </div>

            )}
            showArrow={false}
            key={space.code}
          >
            <WorkSpaceTree
              readOnly={key === 'share' ? true : readOnly} // 项目层，组织数据默认不可修改
              selectId={selectId}
              code={space.code}
              data={space.data}
              operate={key === 'pro' && !readOnly} // 项目层数据默认可修改
              isRecycle={key === 'recycle'} // 只有管理员 并 在回收站的可彻底删除，还原
              onClick={handleSpaceClick}
              onExpand={updateWorkSpace}
              onCollapse={updateWorkSpace}
              onDragEnd={handleSpaceDragEnd}
              onMove={onMove}
              onCreateDoc={onCreateDoc}
              onSave={onSave}
              onDelete={onDelete}
              onCreate={onCreate}
              onCancel={onCancel}
              onRecovery={onRecovery}
              importOnline={importOnline}
              upload={itemUpload}
              onCopy={onCopy}
              store={pageStore}
            />
          </Panel>,
        );
      }
    });

    return panels;
  }

  function handlePanelChange(keys) {
    setOpenKeys(keys);
  }

  function handleRecentClick() {
    if (pageStore.selection !== 'recent' && onClick) {
      onClick();
    }
    pageStore.setSection('recent');
  }
  useImperativeHandle(forwardedRef, () => ({
    handlePanelChange,
    openKeys,
  }));

  return (
    <div className="c7n-workSpace">
      {
        history.location.pathname.indexOf('version') === -1 && (
          <Section selected={section === 'recent'} onClick={handleRecentClick}>
            <Icon type="fiber_new" style={{ color: 'var(--primary-color)', marginRight: 10 }} />
            {formatMessage({ id: 'recent_updates' })}
          </Section>
        )
      }
      <Collapse
        bordered={false}
        activeKey={openKeys}
        onChange={handlePanelChange}
        show
      >
        {renderPanel()}
      </Collapse>
      {!readOnly && (
        <Section
          selected={section === 'template'}
          onClick={() => {
            pageStore.setSection('template');
          }}
        >
          <Icon type="settings_applications" style={{ color: 'var(--primary-color)', marginRight: 10 }} />
          {formatMessage({ id: 'template_manage' })}
        </Section>
      )}
    </div>
  );
}

export default observer(WorkSpace);
