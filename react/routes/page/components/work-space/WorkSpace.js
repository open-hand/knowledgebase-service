import React, { useContext, useState, useImperativeHandle } from 'react';
import { observer } from 'mobx-react-lite';
import { mutateTree } from '@atlaskit/tree';
import { Collapse, Icon } from 'choerodon-ui';
import WorkSpaceTree from '../../../../components/WorkSpaceTree';
import Store from '../../stores';
import Section from './Section';
import './WorkSpace.less';

const { Panel } = Collapse;

function WorkSpace(props) {
  const { pageStore, history } = useContext(Store);
  const { onClick, onSave, onDelete, onCreate, onCancel, readOnly, forwardedRef, onRecovery } = props;
  const [openKeys, setOpenKeys] = useState(['pro', 'org', 'recycle']);
  const selectId = pageStore.getSelectId;
  const { section } = pageStore;
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

  function handleClickAllNode() {
    pageStore.setSection('tree');
    const workSpace = pageStore.getWorkSpace;
    const spaceCode = pageStore.getSpaceCode;
    const currentSelectId = pageStore.getSelectId;
    const objectKeys = Object.keys(workSpace[spaceCode].data.items);
    const firstNode = workSpace[spaceCode].data.items[objectKeys[0]];
    if (currentSelectId) {
      onClick(currentSelectId);
    } else if (firstNode.id !== firstNode.parentId) {
      const newSpace = mutateTree(workSpace[spaceCode].data, firstNode.id, { isClick: true });
      pageStore.setWorkSpaceByCode(spaceCode, newSpace);
      pageStore.setSelectId(firstNode.id);
      onClick(firstNode.id);
    }
  }


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
                <Icon type="chrome_reader_mode" style={{ color: '#5266D4', marginLeft: 15, marginRight: 10 }} />
                <span role="none" onClick={handleClickAllNode}>所有文档</span>
                <Icon type={openKeys.includes(key) ? 'expand_less' : 'expand_more'} style={{ marginLeft: 'auto', marginRight: 5 }} />
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
            <Icon type="restore" style={{ color: '#5266D4', marginRight: 10 }} />
            最近更新
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
        ><Icon type="settings_applications" style={{ color: '#5266D4', marginRight: 10 }} />模板管理
        </Section>
      )}
    </div>
  );
}

export default observer(WorkSpace);
