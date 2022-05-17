import React, { Component } from 'react';
import Button from '@atlaskit/button';
import classnames from 'classnames';
import { stores, Permission } from '@choerodon/boot';
import Tree, {
  mutateTree,
} from '@atlaskit/tree';
import {
  Button as C7NButton, Dropdown, Menu, Icon,
} from 'choerodon-ui';
import { throttle } from 'lodash';
import { TextField } from 'choerodon-ui/pro';
import {
  workSpaceApi,
} from '@choerodon/master';
import { moveItemOnTree } from './utils';
import './WorkSpaceTree.less';
import folderSvg from '@/assets/image/folder.svg';
import documentSvg from '@/assets/image/document.svg';
import importFileSvg from '@/assets/image/importFile.svg';
import uploadFileSvg from '@/assets/image/uploadFile.svg';
import wordSvg from '@/assets/image/word.svg';
import pptSvg from '@/assets/image/ppt.svg';
import pdfSvg from '@/assets/image/pdf.svg';
import txtSvg from '@/assets/image/txt.svg';
import xlsxSvg from '@/assets/image/xlsx.svg';
import mp4Svg from '@/assets/image/mp4.svg';

const { AppState } = stores;

class WorkSpaceTree extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isDragginng: false,
      prefix: 'c7n-workSpaceTree',
      isEditFocus: false,
    };
  }

  componentDidUpdate() {
    const createDOM = document.getElementById('create-workSpaceTree');
    const editDOM = document.getElementById('edit-workSpaceTree');
    if (createDOM) {
      createDOM.focus();
    }
    if (this.state.isEditFocus) {
      editDOM.focus();
    }
  }

  handleEditBlur=async (obj) => {
    const { store, data, code } = this.props;
    this.setState({
      isEditFocus: false,
    });
    const newTree = mutateTree(data, obj.id, { isEdit: false });
    const editDOM = document.getElementById('edit-workSpaceTree');
    await workSpaceApi.rename(obj.id, editDOM.value).then((res) => {
      const newSpace = mutateTree(newTree, obj.id, { data: { title: editDOM.value } });
      store.setWorkSpaceByCode(code, newSpace);
    });
  }

  handleReName = (obj) => {
    const { data, code, store } = this.props;
    const newSpace = mutateTree(data, obj.id, { isEdit: true });
    store.setWorkSpaceByCode(code, newSpace);
    this.setState({
      isEditFocus: true,
    });
  };

  handleClickMenu = (e, item, isRealDelete = false) => {
    const {
      onDelete, onShare, onRecovery, code, onCopy, onMove,
    } = this.props;
    const { id, data: { title } } = item;
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
      case 'copy':
        if (onCopy) {
          onCopy(item);
        }
        break;
      case 'move':
        if (onMove) {
          onMove(item);
        }
        break;
      case 'reName':
        this.handleReName(item);
        break;

      default:
        break;
    }
  };

  handleAddClickMenu=(e, item) => {
    const {
      onCreate, onCreateDoc, importOnline, upload,
    } = this.props;
    switch (e.key) {
      case 'createFolder':
        if (onCreate) {
          onCreate(item);
        }

        break;
      case 'createDocument':
        if (onCreateDoc) {
          onCreateDoc(item);
        }
        break;
      case 'import':
        if (importOnline) {
          importOnline();
        }
        break;
      case 'upload':
        if (upload) {
          upload(item.id);
        }
        break;
      default:
        break;
    }
  }

  getMenus = (item) => {
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    const { code, isRecycle } = this.props;
    if (code === 'recycle') {
      return (
        <Menu onClick={(e) => this.handleClickMenu(e, item, true)}>
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
      <Menu onClick={(e) => this.handleClickMenu(e, item)}>
        {item.type === 'folder' && (
        <Menu.Item key="reName">
          重命名
        </Menu.Item>
        )}
        {(['document', 'file'].includes(item.type)) && (
        <Menu.Item key="copy">
          复制
        </Menu.Item>
        )}
        <Menu.Item key="move">
          移动
        </Menu.Item>
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
  }

    getAddMenus= (item) => {
      if (item.type === 'folder') {
        return (
          <Menu onClick={(e) => this.handleAddClickMenu(e, item)}>
            <Menu.Item key="createDocument">
              <img src={documentSvg} alt="" className={`${this.state.prefix}-image`} />
              创建文档
            </Menu.Item>
            <Menu.Item key="upload">
              <img src={uploadFileSvg} alt="" style={{ marginRight: '6px' }} className={`${this.state.prefix}-image`} />
              上传本地文件
            </Menu.Item>
            <Menu.Item key="import">
              <img src={importFileSvg} alt="" style={{ marginRight: '6px' }} className={`${this.state.prefix}-image`} />
              导入为在线文档
            </Menu.Item>
            <Menu.Item key="createFolder">
              <img src={folderSvg} alt="" style={{ marginRight: '6px' }} className={`${this.state.prefix}-image`} />
              创建文件夹
            </Menu.Item>
          </Menu>
        );
      }
      return (
        <Menu onClick={(e) => this.handleAddClickMenu(e, item)}>
          <Menu.Item key="createDocument">
            <img src={documentSvg} alt="" style={{ marginRight: '6px' }} />
            创建子文档
          </Menu.Item>
          <Menu.Item key="import">
            <img src={importFileSvg} alt="" style={{ marginRight: '6px' }} />
            导入为在线文档
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
    return null;
  };

  getItemStyle = (isDragging, draggableStyle, item, current) => {
    let boxShadow = '';
    let backgroundColor = '';
    if (item.isClick) {
      backgroundColor = 'rgba(104, 135, 232, 0.08)';
    }
    if (isDragging) {
      boxShadow = 'rgba(9, 30, 66, 0.31) 0px 4px 8px -2px, rgba(9, 30, 66, 0.31) 0px 0px 1px';
      backgroundColor = 'rgba(15, 19, 88, 0.03)';
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
  renderItem = ({
    item, onExpand, onCollapse, provided, snapshot,
  }) => {
    const { operate, readOnly, isRecycle } = this.props;
    const { type, id: projectId, organizationId: orgId } = AppState.currentMenuType;
    const iconList = { folder: folderSvg, document: documentSvg };
    const fileImageList = {
      docx: wordSvg, doc: wordSvg, ppt: pptSvg, pps: pptSvg, ppsx: pptSvg, pptx: pptSvg, pdf: pdfSvg, txt: txtSvg, xlsx: xlsxSvg, xls: xlsxSvg, xlsm: xlsxSvg, csv: xlsxSvg, mp4: mp4Svg,
    };
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
        role="none"
        onClick={() => this.handleClickItem(item)}
      >
        <span style={{ marginLeft: 15 }}>{this.getIcon(item, onExpand, onCollapse)}</span>
        <span style={{ whiteSpace: 'nowrap', width: '100%', lineHeight: '18px' }}>
          {item.id === 'create'
            ? (
              <TextField id="create-workSpaceTree" onChange={(value) => { this.handleChange(value, item); }} onBlur={() => { this.handleCreateBlur(item); }} />
            )
            : (
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <img src={item.type === 'file' ? fileImageList[item.fileType] : iconList[item.type]} alt="" style={{ marginRight: '6px' }} />
                {item.isEdit ? <TextField id="edit-workSpaceTree" value={item.data.title} onClick={(e) => { e.stopPropagation(); }} onChange={() => {}} onBlur={() => { this.handleEditBlur(item); }} /> : (
                  <>
                    <span title={item.data.title} className="c7n-workSpaceTree-title">{item.data.title}</span>
                    <span role="none" onClick={(e) => { e.stopPropagation(); }}>
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
                            onClick={(e) => e.stopPropagation()}
                            className="c7n-workSpaceTree-item-btn c7n-workSpaceTree-item-btnMargin"
                            shape="circle"
                            size="small"
                            icon="icon icon-more_vert"
                          />
                        </Dropdown>
                      </Permission>
                      )}
                      {!isRecycle && (!!operate || !readOnly)
                        ? (
                          <>
                            {item.type !== 'file'
                        && (
                        <Dropdown overlay={this.getAddMenus(item)} placement="bottomLeft" trigger={['click']}>
                          <C7NButton
                            className="c7n-workSpaceTree-item-btn c7n-workSpaceTree-item-btnMargin"
                            shape="circle"
                            size="small"
                            icon="icon icon-add"
                          />
                        </Dropdown>
                        )}
                            <Dropdown overlay={this.getMenus(item)} placement="bottomLeft" trigger={['click']}>
                              <C7NButton
                                onClick={(e) => e.stopPropagation()}
                                className={item.type !== 'file' ? 'c7n-workSpaceTree-item-btn' : 'c7n-workSpaceTree-item-btn c7n-workSpaceTree-item-btnMargin'}
                                shape="circle"
                                size="small"
                                icon="icon icon-more_vert"
                              />
                            </Dropdown>
                          </>
                        ) : null}
                    </span>
                  </>
                )}
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

  throttleOnSave = throttle((value, item) => {
    const { onSave } = this.props;
    onSave && onSave(value, item);
  }, 600, { trailing: false })

  handleCreateBlur = (item) => {
    const inputEle = document.getElementById('create-workSpaceTree');
    const { onSave, onCancel } = this.props;
    if (inputEle && inputEle.value && inputEle.value.trim()) {
      this.throttleOnSave(inputEle.value.trim(), item);
    } else {
      this.handleCancel(item);
    }
  };

  handleChange = (value, item) => {
    const { onSave } = this.props;
    if (onSave && value) {
      this.throttleOnSave(value, item);
    }
  }

  handleSave = (e, item) => {
    const inputEle = document.getElementById('create-workSpaceTree');
    const { onSave } = this.props;
    if (onSave) {
      this.throttleOnSave(inputEle.value, item);
    }
  };

  handleClickItem = (item) => {
    const {
      data, onClick, selectId, code,
    } = this.props;
    if (item.id !== 'create' && String(item.id) !== String(selectId)) {
      let newTree = mutateTree(data, item.id, { isClick: true });
      if (selectId && newTree.items[selectId]) {
        newTree = mutateTree(newTree, selectId, { isClick: false });
      }
      if (onClick) {
        onClick(newTree, item, code, selectId);
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
    const {
      data, operate, readOnly, isRecycle,
    } = this.props;
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
