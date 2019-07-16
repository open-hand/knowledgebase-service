package io.choerodon.kb.infra.utils.diff;

/**
 * @author shinan.chen
 * @since 2019/5/5
 */
public final class DiffNode extends PathNode {
    public DiffNode(int i, int j, PathNode prev) {
        super(i, j, prev);
    }

    @Override
    public Boolean isSnake() {
        return false;
    }
}
