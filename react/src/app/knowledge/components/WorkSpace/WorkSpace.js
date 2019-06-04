import React, { Component } from 'react';
import styled from 'styled-components';
import ChevronDownIcon from '@atlaskit/icon/glyph/chevron-down';
import ChevronRightIcon from '@atlaskit/icon/glyph/chevron-right';
import Button from '@atlaskit/button';
import { stores, Permission } from '@choerodon/boot';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import {
  Input, Button as C7NButton, Dropdown, Menu, Icon,
} from 'choerodon-ui';
import { moveItemOnTree } from './utils';
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

const { AppState } = stores;

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

  handleClickMenu = (e, item) => {
    const { onDelete } = this.props;
    switch (e.key) {
      case 'delete':
        if (onDelete) {
          onDelete(item);
        }
        break;
      case 'adminDelete':
        if (onDelete) {
          onDelete(item, 'admin');
        }
        break;
      default:
        break;
    }
  };

  getMenus = (item) => {
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    return (
      <Menu onClick={e => this.handleClickMenu(e, item)}>
        {AppState.userInfo.id === item.createdBy
          ? (
            <Menu.Item key="delete">
              删除
            </Menu.Item>
          ) : (
            <Permission
              key="adminDelete"
              type={type}
              projectId={projectId}
              organizationId={orgId}
              service={[`knowledgebase-service.work-space-${type}.delete`]}
            >
              <Menu.Item key="adminDelete">
                删除
              </Menu.Item>
            </Permission>
          )
        }
        <Menu.Item key="share">
          分享
        </Menu.Item>
      </Menu>
    );
  };

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
      boxShadow,
      backgroundColor,
      ...draggableStyle,
    };
  };

  handleClickAdd = (e, item) => {
    e.stopPropagation();
    const { onCreate } = this.props;
    if (onCreate) {
      onCreate(item);
    }
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
      <span style={{ whiteSpace: 'nowrap', width: '100%' }}>
        {item.id === 'create'
          ? (
            <span style={{ position: 'relative' }}>
              <Input
                id="create-workSpace"
                placeholder="请输入文档名称"
                onPressEnter={e => this.handlePressEnter(e, item)}
              />
              <div style={{ textAlign: 'right', lineHeight: '20px', position: 'absolute', right: '0px' }}>
                <Icon type="done" className="c7n-workSpace-item-icon" onClick={e => this.handleSave(e, item)} />
                <Icon type="close" className="c7n-workSpace-item-icon" onClick={() => this.handleCancel(item)} />
              </div>
            </span>
          )
          : (
            <div>
              <span title={item.data.title}>{item.data.title}</span>
              {this.props.mode !== 'pro'
                ? (
                  <React.Fragment>
                    <C7NButton
                      className="c7n-workSpace-item-btn c7n-workSpace-item-btnMargin"
                      shape="circle"
                      size="small"
                      onClick={e => this.handleClickAdd(e, item)}
                    >
                      <i className="icon icon-add" />
                    </C7NButton>
                    <Dropdown overlay={this.getMenus(item)} trigger={['click']}>
                      <C7NButton
                        onClick={e => e.stopPropagation()}
                        className="c7n-workSpace-item-btn"
                        shape="circle"
                        size="small"
                      >
                        <i className="icon icon-more_vert" />
                      </C7NButton>
                    </Dropdown>
                  </React.Fragment>
                ) : null
              }
            </div>
          )
        }
      </span>
    </div>
  );

  handleCancel = (item) => {
    const { onCancel } = this.props;
    if (onCancel) {
      onCancel(item);
    }
  };

  handlePressEnter = (e, item) => {
    const { onSave } = this.props;
    if (onSave) {
      onSave(e.target.value, item);
    }
  };

  handleSave = (e, item) => {
    const inputEle = document.getElementById('create-workSpace');
    const { onSave } = this.props;
    if (onSave) {
      onSave(inputEle.value, item);
    }
  };

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
    const { data, mode } = this.props;

    return (
      <div className="c7n-workSpace">
        <Tree
          tree={data}
          renderItem={this.renderItem}
          onExpand={this.onExpand}
          onCollapse={this.onCollapse}
          onDragEnd={this.onDragEnd}
          isDragEnabled={mode !== 'pro'}
          isNestingEnabled={mode !== 'pro'}
          offsetPerLevel={20}
        />
      </div>
    );
  }
}

export default DragDropWithNestingTree;
