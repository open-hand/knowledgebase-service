import React, { Component } from 'react';
import styled from 'styled-components';
import ChevronDownIcon from '@atlaskit/icon/glyph/chevron-down';
import ChevronRightIcon from '@atlaskit/icon/glyph/chevron-right';
import Button from '@atlaskit/button';
import Tree, {
  mutateTree,
  moveItemOnTree,
  type RenderItemParams,
  type TreeItem,
  type TreeData,
  type ItemId,
  type TreeSourcePosition,
  type TreeDestinationPosition,
} from '@atlaskit/tree';
import { complexTree } from './mockData/complexTree';
import './WorkSpace.scss';

const Container = styled.div`
  display: flex;
`;

const Dot = styled.span`
  display: flex;
  width: 24px;
  height: 32px;
  justify-content: center;
  font-size: 12px;
  line-height: 32px;
`;

type State = {|
  tree: TreeData,
|};

export default class DragDropWithNestingTree extends Component<void, State> {
  state = {
    tree: complexTree,
  };

  static getIcon(
    item: TreeItem,
    onExpand: (itemId: ItemId) => void,
    onCollapse: (itemId: ItemId) => void,
  ) {
    if (item.children && item.children.length > 0) {
      return item.isExpanded ? (
        <Button
          spacing="none"
          appearance="subtle-link"
          onClick={() => onCollapse(item.id)}
        >
          <ChevronDownIcon
            label=""
            size="medium"
            onClick={() => onCollapse(item.id)}
          />
        </Button>
      ) : (
        <Button
          spacing="none"
          appearance="subtle-link"
          onClick={() => onExpand(item.id)}
        >
          <ChevronRightIcon
            label=""
            size="medium"
            onClick={() => onExpand(item.id)}
          />
        </Button>
      );
    }
    return <Dot />;
  }

  getItemStyle = (isDragging, draggableStyle, item) => {
    let boxShadow = '';
    let backgroundColor = '';
    if (isDragging) {
      boxShadow = 'rgba(9, 30, 66, 0.31) 0px 4px 8px -2px, rgba(9, 30, 66, 0.31) 0px 0px 1px';
      backgroundColor = 'rgb(235, 236, 240)';
    }
    return {
      boxShadow,
      backgroundColor,
      ...draggableStyle,
    };
  };

  renderItem = ({ item, onExpand, onCollapse, provided, snapshot }: RenderItemParams) => {
    return (
      <div
        ref={provided.innerRef}
        {...provided.draggableProps}
        {...provided.dragHandleProps}
        style={this.getItemStyle(
          snapshot.isDragging,
          provided.draggableProps.style,
          item,
        )}
        className="c7n-workSpace-item"
      >
        <span>{DragDropWithNestingTree.getIcon(item, onExpand, onCollapse)}</span>
        <span style={{ whiteSpace: 'nowrap' }}>{item.data ? item.data.title : ''}</span>
      </div>
    );
  };

  onExpand = (itemId: ItemId) => {
    const { tree }: State = this.state;
    this.setState({
      tree: mutateTree(tree, itemId, { isExpanded: true }),
    });
  };

  onCollapse = (itemId: ItemId) => {
    const { tree }: State = this.state;
    this.setState({
      tree: mutateTree(tree, itemId, { isExpanded: false }),
    });
  };

  onDragEnd = (
    source: TreeSourcePosition,
    destination: ?TreeDestinationPosition,
  ) => {
    const { tree } = this.state;

    if (!destination) {
      return;
    }

    const newTree = moveItemOnTree(tree, source, destination);
    this.setState({
      tree: newTree,
    });
  };

  render() {
    const { tree } = this.state;

    return (
      <div className="c7n-workSpace">
        <Tree
          tree={tree}
          renderItem={this.renderItem}
          onExpand={this.onExpand}
          onCollapse={this.onCollapse}
          onDragEnd={this.onDragEnd}
          isDragEnabled={false}
          isNestingEnabled={false}
        />
      </div>
    );
  }
}
