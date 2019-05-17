import { mutateTree } from '@atlaskit/tree';

export default {};

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

export const removeItemFromTree = (tree, item) => {
  const destinationParent = tree.items[item.parentId];
  // 删除节点
  delete tree.items[item.id];
  // 更新父级
  const newDestinationChildren = destinationParent.children.filter(id => id !== item.id);
  return mutateTree(tree, item.parentId, {
    children: newDestinationChildren,
    hasChildren: !!newDestinationChildren.length,
    isExpanded: !!newDestinationChildren.length,
  });
};
