import React, { Component } from 'react';
import styled from 'styled-components';
import ChevronDownIcon from '@atlaskit/icon/glyph/chevron-down';
import ChevronRightIcon from '@atlaskit/icon/glyph/chevron-right';
import Button from '@atlaskit/button';
import Tree, {
  mutateTree,
  moveItemOnTree,
} from '@atlaskit/tree';
import {
  Input,
} from 'choerodon-ui';
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

class DragDropWithNestingTree extends Component {
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

  getIcon = (item, onExpand, onCollapse) => {
    if (item.children && item.children.length > 0) {
      return item.isExpanded ? (
        <Button
          spacing="none"
          appearance="subtle-link"
          onClick={(e) => {
            e.stopPropagation();
            onCollapse(item.id);
          }}
        >
          <ChevronDownIcon
            label=""
            size="medium"
            onClick={(e) => {
              e.stopPropagation();
              onCollapse(item.id);
            }}
          />
        </Button>
      ) : (
        <Button
          spacing="none"
          appearance="subtle-link"
          onClick={(e) => {
            e.stopPropagation();
            onExpand(item.id);
          }}
        >
          <ChevronRightIcon
            label=""
            size="medium"
            onClick={(e) => {
              e.stopPropagation();
              onExpand(item.id);
            }}
          />
        </Button>
      );
    }
    return <Dot />;
  };

  getItemStyle = (isDragging, draggableStyle, item, current) => {
    let boxShadow = '';
    let backgroundColor = '';
    if (item.isClick) {
      backgroundColor = 'rgba(140,158,255,.16)';
    }
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

  renderItem = ({ item, onExpand, onCollapse, provided, snapshot }) => (
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
      onClick={() => this.handleClickItem(item)}
    >
      <span>{this.getIcon(item, onExpand, onCollapse)}</span>
      <span style={{ whiteSpace: 'nowrap' }}>
        {item.id === 'create'
          ? (
            <Input
              id="create-workSpace"
              placeholder="请输入文档名称"
              onPressEnter={e => this.handlePressEnter(e, item)}
              onBlur={() => this.handleCreateBlur(item)}
            />
          )
          : item.data.title
        }
      </span>
    </div>
  );

  handleCreateBlur = (item) => {
    const { onCreateBlur } = this.props;
    if (onCreateBlur) {
      onCreateBlur(item);
    }
  };

  handlePressEnter = (e, item) => {
    const { onPressEnter } = this.props;
    if (onPressEnter) {
      onPressEnter(e.target.value, item);
    }
  };

  handleClickItem = (item) => {
    const { data, onClick, selectId } = this.props;
    if (item.id !== 'create' && item.id !== selectId) {
      let newTree = mutateTree(data, item.id, { isClick: true });
      if (selectId) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      if (onClick) {
        onClick(newTree, item.id);
      }
    }
  };

  onExpand = (itemId) => {
    const { data, onExpand } = this.props;
    const newTree = mutateTree(data, itemId, { isExpanded: true });
    if (onExpand) {
      onExpand(newTree, itemId);
    }
  };

  onCollapse = (itemId) => {
    const { data, onCollapse } = this.props;
    const newTree = mutateTree(data, itemId, { isExpanded: false });
    if (onCollapse) {
      onCollapse(newTree);
    }
  };

  onDragEnd = (source, destination) => {
    const { data, onDragEnd } = this.props;
    if (!destination) {
      return;
    }
    const newTree = moveItemOnTree(data, source, destination);
    if (onDragEnd) {
      onDragEnd(newTree, source, destination);
    }
  };

  render() {
    const { data } = this.props;

    return (
      <div className="c7n-workSpace">
        <Tree
          tree={data}
          renderItem={this.renderItem}
          onExpand={this.onExpand}
          onCollapse={this.onCollapse}
          onDragEnd={this.onDragEnd}
          isDragEnabled
          isNestingEnabled
        />
      </div>
    );
  }
}

export default DragDropWithNestingTree;
