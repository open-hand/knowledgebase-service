import React, { Component } from 'react';
import styled from 'styled-components';
import Button from '@atlaskit/button';
import classnames from 'classnames';
import { stores, Permission } from '@choerodon/boot';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import {
  Input, Button as C7NButton, Dropdown, Menu, Icon,
} from 'choerodon-ui';
import { moveItemOnTree } from './utils';
import './WorkSpaceTree.less';

const Dot = styled.span`
  display: flex;
  width: 24px;
  height: 32px;
  justify-content: center;
  font-size: 12px;
  line-height: 32px;
`;

const { AppState } = stores;

class WorkSpaceTree extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isDragginng: false,
    };
  }

  componentDidUpdate() {
    const createDOM = document.getElementById('create-workSpaceTree');
    if (createDOM) {
      createDOM.focus();
    }
  }

  handleClickMenu = (e, item, isRealDelete = false) => {
    const { onDelete, onShare, onRecovery, code } = this.props;
    const { id, data: { title } } = item;
    // console.log('isRealDelete', isRealDelete)
    switch (e.key) {
      case 'delete':
        if (onDelete) {
          onDelete(id, title, 'normal', isRealDelete);
        }
        break;
      case 'adminDelete':
        if (onDelete) {
          onDelete(id, title, 'admin', isRealDelete);
        }
        break;
      case 'recovery':
        if (onRecovery) {
          onRecovery(id, title);
        }
        break;
      case 'share':
        if (onShare) {
          onShare(id);
        }
        break;
      default:
        break;
    }
  };

  getMenus = (item) => {
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    const { code, isRecycle } = this.props;
    if (code === 'recycle') {
      return (
        <Menu onClick={e => this.handleClickMenu(e, item, true)}>
          <Menu.Item key="recovery">
            恢复
          </Menu.Item>
          <Menu.Item key="adminDelete">
            删除
          </Menu.Item>
        </Menu>
      );
    }
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
              service={type === 'project' 
                ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
                : ['choerodon.code.organization.knowledge.ps.doc.delete']}
            >
              <Menu.Item key="adminDelete">
                删除
              </Menu.Item>
            </Permission>
          )}

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
          <Icon
            className="c7n-workSpaceTree-item-icon"
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
            className="c7n-workSpaceTree-item-icon"
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
      backgroundColor = 'white';
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

  /**
   * 渲染空间节点
   * @param item
   * @param onExpand
   * @param onCollapse
   * @param provided
   * @param snapshot
   */
  renderItem = ({ item, onExpand, onCollapse, provided, snapshot }) => {
    const { operate, readOnly, isRecycle } = this.props;
    const { type, id: projectId, organizationId: orgId } = AppState.currentMenuType;

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
        className="c7n-workSpaceTree-item"
        onClick={() => this.handleClickItem(item)}
      >
        <span style={{ marginLeft: 15 }}>{this.getIcon(item, onExpand, onCollapse)}</span>
        <span style={{ whiteSpace: 'nowrap', width: '100%', lineHeight: '18px' }}>
          {item.id === 'create'
            ? (
              <span>
                <Input
                  id="create-workSpaceTree"
                  onPressEnter={e => this.handlePressEnter(e, item)}
                  onBlur={() => this.handleCreateBlur(item)}
                />
              </span>
            )
            : (
              <div>
                <span title={item.data.title} className="c7n-workSpaceTree-title">{item.data.title}</span>
                <span onClick={(e) => { e.stopPropagation(); }}>                
                  {isRecycle && (
                  <Permission
                    key="adminDelete"
                    type={type}
                    projectId={projectId}
                    organizationId={orgId}
                    service={type === 'project' 
                      ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
                      : ['choerodon.code.organization.knowledge.ps.doc.delete']}
                  >
                    <Dropdown overlay={this.getMenus(item)} trigger={['click']}>
                      <C7NButton
                        onClick={e => e.stopPropagation()}
                        className="c7n-workSpaceTree-item-btn c7n-workSpaceTree-item-btnMargin"
                        shape="circle"
                        size="small"
                      >
                        <i className="icon icon-more_vert" />
                      </C7NButton>
                    </Dropdown>
                  </Permission>
                  )}
                  {!isRecycle && (!!operate || !readOnly)
                    ? (
                      <React.Fragment>
                        <C7NButton
                          className="c7n-workSpaceTree-item-btn c7n-workSpaceTree-item-btnMargin"
                          shape="circle"
                          size="small"
                          onClick={e => this.handleClickAdd(e, item)}
                        >
                          <i className="icon icon-add" />
                        </C7NButton>
                        <Dropdown overlay={this.getMenus(item)} trigger={['click']}>
                          <C7NButton
                            onClick={e => e.stopPropagation()}
                            className="c7n-workSpaceTree-item-btn"
                            shape="circle"
                            size="small"
                          >
                            <i className="icon icon-more_vert" />
                          </C7NButton>
                        </Dropdown>
                      </React.Fragment>
                    ) : null}
                </span>
              </div>
            )}
        </span>
      </div>
    );
  };

  handleCancel = (item) => {
    const { onCancel } = this.props;
    if (onCancel) {
      onCancel(item.id);
    }
  };

  handleCreateBlur = (item) => {
    const inputEle = document.getElementById('create-workSpaceTree');
    const { onSave, onCancel } = this.props;
    if (inputEle && inputEle.value && inputEle.value.trim()) {
      onSave(inputEle.value.trim(), item);
    } else {
      this.handleCancel(item);
    }
  };

  handlePressEnter = (e, item) => {
    const { onSave } = this.props;
    if (onSave) {
      onSave(e.target.value, item);
    }
  };

  handleSave = (e, item) => {
    const inputEle = document.getElementById('create-workSpaceTree');
    const { onSave } = this.props;
    if (onSave) {
      onSave(inputEle.value, item);
    }
  };

  handleClickItem = (item) => {
    const { data, onClick, selectId, code } = this.props;
    if (item.id !== 'create' && String(item.id) !== String(selectId)) {
      let newTree = mutateTree(data, item.id, { isClick: true });
      if (selectId && newTree.items[selectId]) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      if (onClick) {
        onClick(newTree, item.id, code, selectId);
      }
    }
  };

  onExpand = (itemId) => {
    const { data, onExpand, code } = this.props;
    const newTree = mutateTree(data, itemId, { isExpanded: true });
    if (onExpand) {
      onExpand(newTree, code);
    }
  };

  onCollapse = (itemId) => {
    const { data, onCollapse, code } = this.props;
    const newTree = mutateTree(data, itemId, { isExpanded: false });
    if (onCollapse) {
      onCollapse(newTree, code);
    }
  };

  onDragEnd = (source, destination) => {
    this.setState({
      isDragginng: false,
    });
    const { data, onDragEnd, code } = this.props;
    if (!destination) {
      return;
    }
    const newTree = moveItemOnTree(data, source, destination);
    if (onDragEnd) {
      onDragEnd(newTree, source, destination, code);
    }
  };

  onDragStart = () => {
    this.setState({
      isDragginng: true,
    });
  };

  render() {
    const { data, operate, readOnly, isRecycle } = this.props;
    const { isDragginng } = this.state;
    return (
      <div className={classnames('c7n-workSpaceTree', {
        'c7n-workSpaceTree-dragging': isDragginng,
      })}// 
      >
        <Tree
          tree={data}
          renderItem={this.renderItem}
          onExpand={this.onExpand}
          onCollapse={this.onCollapse}
          onDragStart={this.onDragStart}
          onDragEnd={this.onDragEnd}
          isDragEnabled={!isRecycle && (!!operate || !readOnly)}
          isNestingEnabled={!isRecycle && (!!operate || !readOnly)}
          offsetPerLevel={20}
        />
      </div>
    );
  }
}

export default WorkSpaceTree;
