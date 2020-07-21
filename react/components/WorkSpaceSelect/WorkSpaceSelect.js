import React, { Component } from 'react';
import styled from 'styled-components';
import Button from '@atlaskit/button';
import { stores, Permission } from '@choerodon/boot';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import { Icon } from 'choerodon-ui';
import './WorkSpaceSelect.less';

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

const { AppState } = stores;

class WorkSpaceSelect extends Component {
  constructor(props) {
    super(props);
    this.state = {};
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
          <Icon
            className="c7n-workSpace-item-icon"
            type="baseline-arrow_drop_down"
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
          <Icon
            className="c7n-workSpace-item-icon"
            type="baseline-arrow_right"
            onClick={(e) => {
              e.stopPropagation();
              onExpand(item.id);
            }}
          />
        </Button>
      );
    }
    return <Dot>&bull;</Dot>;
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
      cursor: 'pointer',
      boxShadow,
      backgroundColor,
      ...draggableStyle,
    };
  };

  /* eslint-disable react/jsx-props-no-spreading */
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
      <span style={{ display: 'inline-block', verticalAlign: 'middle' }}>{this.getIcon(item, onExpand, onCollapse)}</span>
      <span style={{ whiteSpace: 'nowrap', width: '100%' }}>
        <span title={item.data.title} className="c7n-workSpace-title">{item.data.title}</span>
        {item.isClick
          ? (
            <Icon type="check" style={{ verticalAlign: 'top', marginLeft: 10 }} />
          ) : null}
      </span>
    </div>
  );

  handleClickItem = (item) => {
    const { data, onClick, selectId } = this.props;
    if (item.id !== 'create' && item.id !== selectId) {
      let newTree = mutateTree(data, item.id, { isClick: true });
      if (selectId && newTree.items[selectId]) {
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

  render() {
    const { data } = this.props;

    return (
      <div className="c7n-workSpaceSelect">
        <Tree
          tree={data}
          renderItem={this.renderItem}
          onExpand={this.onExpand}
          onCollapse={this.onCollapse}
          isDragEnabled={false}
          isNestingEnabled={false}
          offsetPerLevel={20}
        />
      </div>
    );
  }
}

export default WorkSpaceSelect;
