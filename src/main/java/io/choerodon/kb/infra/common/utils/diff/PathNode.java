package io.choerodon.kb.infra.common.utils.diff;

/**
 * @author shinan.chen
 * @since 2019/5/5
 */
public abstract class PathNode {
    public final int i;
    public final int j;
    public final PathNode prev;

    public PathNode(int i, int j, PathNode prev) {
        this.i = i;
        this.j = j;
        this.prev = prev;
    }

    public abstract Boolean isSnake();

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        PathNode node = this;
        while (node != null) {
            buf.append("(");
            buf.append(Integer.toString(node.i));
            buf.append(",");
            buf.append(Integer.toString(node.j));
            buf.append(")");
            node = node.prev;
        }
        buf.append("]");
        return buf.toString();
    }
}
