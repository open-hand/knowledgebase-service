package io.choerodon.kb.infra.utils.diff;

/**
 * @author shinan.chen
 * @since 2019/5/5
 */
public final class Snake extends PathNode {
    public Snake(int i, int j, PathNode prev) {
        super(i, j, prev);
    }

    @Override
    public Boolean isSnake() {
        return true;
    }
}
