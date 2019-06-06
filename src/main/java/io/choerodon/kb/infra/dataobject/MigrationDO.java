package io.choerodon.kb.infra.dataobject;

/**
 * Created by Zenger on 2019/6/6.
 */
public class MigrationDO {

    private String reference;
    private String type;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Migration{" +
                "reference=" + reference +
                ", type=" + type +
                "}";
    }
}
