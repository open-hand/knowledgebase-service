import React, { Component, useContext, useEffect, useState, useImperativeHandle } from 'react';
import { observer } from 'mobx-react-lite';
import { mutateTree } from '@atlaskit/tree';
import classnames from 'classnames';
import { Collapse, Icon } from 'choerodon-ui';
import WorkSpaceTree from '../../../../components/WorkSpaceTree';
import Store from '../../stores';
import './WorkSpace.less';

const { Panel } = Collapse;

function WorkSpace(props) {
  const { pageStore } = useContext(Store);
  const { onClick, onSave, onDelete, onCreate, onCancel, readOnly, forwardedRef, onRecovery } = props;
  const [openKeys, setOpenKeys] = useState(['pro', 'org', 'recycle']);

  /**
   * 点击空间
   * @param newTree 变化后空间
   * @param clickId 本次点击项
   * @param treeCode 本次点击空间类别code
   * @param lastClickId 上次选中项
   */
  function handleSpaceClick(newTree, clickId, treeCode, lastClickId) {
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


  function renderPanel() {
    const panels = [];
    const workSpace = pageStore.getWorkSpace;
    const recycleDate = pageStore.getRecycleWorkSpace;
    const selectId = pageStore.getSelectId;
    const workSpaceKeys = Object.keys(workSpace);
    // console.log('renderPanel', workSpaceKeys, workSpace);
    workSpaceKeys.forEach((key) => {
      const space = workSpace[key];
      const spaceData = space.data;
      if (spaceData.items && spaceData.items[0] && spaceData.items[0].children && spaceData.items[0].children.length) {
        panels.push(
          <Panel header={space.name} key={space.code}>
            <WorkSpaceTree
              readOnly={workSpaceKeys.length > 1 && key === 'org' ? true : readOnly} // 项目层，组织数据默认不可修改
              selectId={selectId}
              code={space.code}
              data={space.data}
              operate={key === 'pro'} // 项目层数据默认可修改
              isRecycle={key === 'recycle'} // 只有管理员 并 在回收站的可彻底删除，还原
              onClick={handleSpaceClick}
              onExpand={updateWorkSpace}
              onCollapse={updateWorkSpace}
              onDragEnd={handleSpaceDragEnd}
              onSave={onSave}
              onDelete={onDelete}
              onCreate={onCreate}
              onCancel={onCancel}
              onRecovery={onRecovery}
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
    const lastClickId = pageStore.getSelectId;
    const spaceCode = pageStore.getSpaceCode;
    const workSpace = pageStore.getWorkSpace;
    if (lastClickId && spaceCode) {
      const newSpace = mutateTree(workSpace[spaceCode].data, lastClickId, { isClick: false });
      pageStore.setWorkSpaceByCode(spaceCode, newSpace);
    }
    if (lastClickId && onClick) {
      onClick();
    }
  }

  function renderHomeBtn() {
    const selectId = pageStore.getSelectId;
    return (
      <div
        className={classnames('c7n-workSpace-rencent', {
          'c7n-workSpace-rencent-clicked': !selectId,
        })}
        onClick={handleRecentClick}
      >
        <span className="c7n-workSpace-rencentText">最近更新</span>
      </div>
    );
  }

  useImperativeHandle(forwardedRef, () => ({
    handlePanelChange,
    openKeys,
  }));

  return (
    <div className="c7n-workSpace">
      {renderHomeBtn()}
      <Collapse
        bordered={false}
        activeKey={openKeys}
        onChange={handlePanelChange}
      >
        {renderPanel()}

      </Collapse>
    </div>
  );
}

export default observer(WorkSpace);
