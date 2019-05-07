import { TreeData } from './types';
import TreeBuilder from './TreeBuilder';

export const complexTree = new TreeBuilder(1)
  .withLeaf(0) // 0
  .withLeaf(1) // 1
  .withSubTree(
    new TreeBuilder(2) // 2
      .withLeaf(0) // 3
      .withLeaf(1) // 4
      .withLeaf(2) // 5
      .withLeaf(3), // 6
  )
  .withLeaf(3) // 7
  .withLeaf(4) // 8
  .withLeaf(5) // 9
  .withSubTree(
    new TreeBuilder(6) // 10
      .withLeaf(0) // 11
      .withLeaf(1) // 12
      .withSubTree(
        new TreeBuilder(2) // 13
          .withLeaf(0) // 14
          .withLeaf(1) // 15
          .withLeaf(2), // 16
      )
      .withLeaf(3) // 17
      .withLeaf(4), // 18
  )
  .withLeaf(7) // 19
  .withLeaf(8) // 20
  .withLeaf(9) // 20
  .withLeaf(10) // 20
  .withLeaf(11) // 20
  .withLeaf(12) // 20
  .build();
