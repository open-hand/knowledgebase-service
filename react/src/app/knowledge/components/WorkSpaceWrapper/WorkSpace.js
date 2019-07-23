import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { mutateTree } from '@atlaskit/tree';
import {
  Input, Button, Dropdown, Menu, Icon, Collapse,
} from 'choerodon-ui';
import WorkSpaceItem from '../WorkSpace';
import './WorkSpace.scss';

const { Panel } = Collapse;

@observer
class WorkSpace extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidUpdate() {
    const createDOM = document.getElementById('create-workSpace');
    if (createDOM) {
      createDOM.focus();
    }
  }

  /**
   * 更新空间Map
   * @param tree
   * @param code
   */
  updateWorkSpaceMap = (tree, code) => {
    const { store } = this.props;
    store.setWorkSpaceMap(code, tree);
  };

  /**
   * 点击空间
   * @param newTree 变化后的空间
   * @param clickId 被点击空间id
   * @param treeCode pro or org
   * @param lastClickId
   */
  handleSpaceClick = (newTree, clickId, treeCode, lastClickId) => {
    const { onClick, store } = this.props;
    const spaceCode = store.getSpaceCode;
    const workSpaceMap = store.getWorkSpaceMap;
    if (treeCode !== spaceCode) {
      const newSpace = mutateTree(workSpaceMap[spaceCode], lastClickId, { isClick: false });
      this.updateWorkSpaceMap(newSpace, spaceCode);
    }
    this.updateWorkSpaceMap(newTree, treeCode);
    if (onClick && clickId) {
      onClick(clickId);
    }
  };

  /**
   * 空间拖拽回调
   * @param newTree 拖拽后的空间
   * @param source
   * @param destination
   * @param code
   */
  handleSpaceDragEnd = (newTree, source, destination, code) => {
    const { store } = this.props;
    const spaceMap = store.getWorkSpaceMap;
    const spaceData = spaceMap[code];
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
    store.moveWorkSpace(destId, {
      id: sourceId,
      before,
      targetId,
    });
    this.updateWorkSpaceMap(newTree, code);
  };

  renderPanel = () => {
    const panels = [];
    const { data, store } = this.props;
    const workSpaceMap = store.getWorkSpaceMap;
    data.forEach((panel) => {
      if (panel.data.items && panel.data.items[0] && panel.data.items[0].children && panel.data.items[0].children.length) {
        panels.push(
          <Panel header={panel.name} key={panel.code}>
            <WorkSpaceItem
              {...this.props}
              code={panel.code}
              data={workSpaceMap[panel.code]}
              operate={panel.isOperate}
              onClick={this.handleSpaceClick}
              onExpand={this.updateWorkSpaceMap}
              onCollapse={this.updateWorkSpaceMap}
              onDragEnd={this.handleSpaceDragEnd}
            />
          </Panel>,
        );
      }
    });
    return panels;
  };

  render() {
    return (
      <div className="c7n-workSpace">
        <Collapse bordered={false} defaultActiveKey={['pro', 'org']}>
          {this.renderPanel()}
        </Collapse>
      </div>
    );
  }
}

export default WorkSpace;
