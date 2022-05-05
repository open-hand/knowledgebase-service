import React, {
  useContext, useState, useImperativeHandle, useCallback, useRef,
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
import { uploadFile, secretMultipart, createOrgBase } from '@/api/knowledgebaseApi';
import uploadImage from '@/utils';

const { Panel } = Collapse;

function WorkSpace(props) {
  const { pageStore, history, type: levelType } = useContext(Store);
  const {
    onClick, onCopy, onMove, onSave, onDelete, onCreate, onCancel, readOnly, forwardedRef, onRecovery, onCreateDoc, importOnline,
  } = props;
  const [openKeys, setOpenKeys] = useState(['pro', 'org', 'recycle']);
  const formatMessage = useFormatMessage('knowledge.document');
  const selectId = pageStore.getSelectId;
  const { section } = pageStore;
  const uploadInput = useRef(null);
  /**
   * 点击空间
   * @param newTree 变化后空间
   * @param clickId 本次点击项
   * @param treeCode 本次点击空间类别code
   * @param lastClickId 上次选中项
   */
  function handleSpaceClick(newTree, clickId, treeCode, lastClickId) {
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
    pageStore.setSelectId(clickId);
    if (onClick) {
      onClick(clickId);
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
    const keys = Object.keys(space.data.items);
    keys.forEach((item) => {
      const newTree = mutateTree(space.data, item, { isExpanded: false });
      pageStore.setWorkSpaceByCode(space.code, newTree);
    });
  };
  const createFolder = (space, e) => {
    e.stopPropagation();
    onCreate(space.data.items[space.data.rootId]);
  };
  const handleUpload = useCallback((e) => {
    // @ts-ignore
    e.stopPropagation();
    uploadInput.current?.click();
  }, []);
  const handleCreate = useCallback((e) => {
    // @ts-ignore
    e.stopPropagation();
    onCreateDoc();
  }, []);
  const handleImport = useCallback((e) => {
    // @ts-ignore
    e.stopPropagation();
    importOnline();
  }, []);
  const handleMenu = (space) => (
    <Menu mode="vertical">
      <Menu.Item><div onClick={(e) => createFolder(space, e)} role="none">创建文件夹</div></Menu.Item>
      <Menu.Item><div onClick={(e) => handleCreate(e)} role="none">创建文档</div></Menu.Item>
      <Menu.Item><div onClick={(e) => handleUpload(e)} role="none">上传本地文件</div></Menu.Item>
      <Menu.Item><div onClick={(e) => handleImport(e)} role="none">导入为在线文档</div></Menu.Item>
    </Menu>
  );
  const upload = useCallback((file) => {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[levelType === 'project' ? 'pro' : 'org']?.data;
    if (!file) {
      Choerodon.prompt('请选择文件');
      return;
    }
    if (file.size > 1024 * 1024 * 100) {
      Choerodon.prompt('文件不能超过100M');
      return;
    }
    const formData = new FormData();
    formData.append('file', file);
    secretMultipart(formData).then((res) => {
      if (!res && res.failed) {
        Choerodon.prompt('上传失败！');
      }
      const data = {
        fileKey: res.fileKey,
        baseId: pageStore.baseId,
        parentWorkspaceId: spaceData?.rootId,
        title: spaceData.items[spaceData?.rootId].title,
        type: 'file',
      };
      uploadFile(data).then((response) => {
        if (res && !res.failed) {
          Choerodon.prompt('上传成功！');
          pageStore.loadWorkSpaceAll();
        }
      });
    });
  }, []);
  const beforeUpload = useCallback((e) => {
    if (e.target.files[0]) {
      upload(e.target.files[0]);
    }
  }, [upload]);
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
                <Dropdown overlay={handleMenu(space)} trigger={['click']}>
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
                <input
                  ref={uploadInput}
                  type="file"
                  onChange={beforeUpload}
                  style={{ display: 'none' }}
                  // accept=".docx"
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
              upload={handleUpload}
              onCopy={onCopy}
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
