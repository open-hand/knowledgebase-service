import { mutateTree } from '@atlaskit/tree';

export default {};

const hasLoadedChildren = item => !!item.hasChildren && item.children.length > 0;

const isLeafItem = item => !item.hasChildren;

export const addItemToTree = (tree, item, mode) => {
  const destinationParent = tree.items[item.parentId];
  // 增加新节点
  tree.items[item.id] = {
    ...item,
    isExpanded: false,
    hasChildren: false,
  };
  // 最新children
  let newDestinationChildren = [];
  if (mode === 'create') {
    // 如果是新增，删除创建节点
    delete tree.items.create;
    newDestinationChildren = [
      ...destinationParent.children.filter(id => id !== 'create'),
      item.id,
    ];
  } else {
    newDestinationChildren = [
      ...destinationParent.children,
      item.id,
    ];
  }
  // 更新父级
  return mutateTree(tree, item.parentId, {
    children: newDestinationChildren,
    hasChildren: true,
    isExpanded: true,
  });
};

export const removeItemFromTree = (tree, item, isCancel) => {
  const destinationParent = tree.items[item.parentId];
  // 删除节点
  delete tree.items[item.id];
  // 更新父级
  const newDestinationChildren = destinationParent.children.filter(id => id !== item.id);
  const parent = {
    children: newDestinationChildren,
    hasChildren: !!newDestinationChildren.length,
    isExpanded: !!newDestinationChildren.length,
  };
  if (!isCancel) {
    parent.isClick = !!item.parentId;
  }
  return mutateTree(tree, item.parentId, parent);
};

/**
 * 将元素插入指定位置
 * @param tree
 * @param position
 * @param item
 * @returns {TreeData}
 */
const moveItem = (tree, position, item) => {
  const destinationParent = tree.items[position.parentId];
  const newDestinationChildren = [...destinationParent.children];
  if (typeof position.index === 'undefined') {
    if (hasLoadedChildren(destinationParent) || isLeafItem(destinationParent)) {
      newDestinationChildren.push(item);
    }
  } else {
    newDestinationChildren.splice(position.index, 0, item);
  }
  return mutateTree(tree, position.parentId, {
    children: newDestinationChildren,
    hasChildren: true,
    isExpanded: true,
  });
};

/**
 * 从指定位置删除元素
 * @param tree
 * @param position
 * @returns {{tree: TreeData, itemRemoved: *}}
 */
const removeItem = (tree, position) => {
  const sourceParent = tree.items[position.parentId];
  const newSourceChildren = [...sourceParent.children];
  const itemRemoved = newSourceChildren.splice(position.index, 1)[0];
  const newTree = mutateTree(tree, position.parentId, {
    children: newSourceChildren,
    hasChildren: newSourceChildren.length > 0,
    isExpanded: newSourceChildren.length > 0 && sourceParent.isExpanded,
  });
  return {
    tree: newTree,
    itemRemoved,
  };
};

/**
 * 移动指定元素到指定位置（由于需求拖动后展开，重写这部分函数）
 * @param tree
 * @param from
 * @param to
 * @returns {TreeData}
 */
export const moveItemOnTree = (tree, from, to) => {
  const { tree: treeWithoutSource, itemRemoved } = removeItem(
    tree,
    from,
  );
  return moveItem(treeWithoutSource, to, itemRemoved);
};
