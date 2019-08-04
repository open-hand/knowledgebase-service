import React, { Component, useContext, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { mutateTree } from '@atlaskit/tree';
import { Collapse } from 'choerodon-ui';
import WorkSpaceTree from '../../../../components/WorkSpaceTree';
import Store from '../../stores';
import './WorkSpace.less';

const { Panel } = Collapse;

function WorkSpace(props) {
  const { pageStore } = useContext(Store);
  const { onClick, onSave, onDelete } = props;

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
    if (treeCode !== spaceCode) {
      const newSpace = mutateTree(workSpace[spaceCode].data, lastClickId, { isClick: false });
      pageStore.setWorkSpaceByCode(spaceCode, newSpace);
    }
    pageStore.setWorkSpaceByCode(treeCode, newTree);
    pageStore.setSpaceCode(treeCode);
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
    const spaceData = workSpace[code];
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
    });
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function updateWorkSpace(newTree, code) {
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function renderPanel() {
    const panels = [];
    const workSpace = pageStore.getWorkSpace;
    const selectId = pageStore.getSelectId;
    Object.keys(workSpace).forEach((key) => {
      const space = workSpace[key];
      const spaceData = space.data;
      if (spaceData.items && spaceData.items[0] && spaceData.items[0].children && spaceData.items[0].children.length) {
        panels.push(
          <Panel header={space.name} key={space.code}>
            <WorkSpaceTree
              selectId={selectId}
              code={space.code}
              data={space.data}
              operate={space.isOperate}
              onClick={handleSpaceClick}
              onExpand={updateWorkSpace}
              onCollapse={updateWorkSpace}
              onDragEnd={handleSpaceDragEnd}
              onSave={onSave}
              onDelete={onDelete}
            />
          </Panel>,
        );
      }
    });
    return panels;
  }

  return (
    <div className="c7n-workSpace">
      <Collapse bordered={false} defaultActiveKey={['pro', 'org']}>
        {renderPanel()}
      </Collapse>
    </div>
  );
}

export default observer(WorkSpace);
