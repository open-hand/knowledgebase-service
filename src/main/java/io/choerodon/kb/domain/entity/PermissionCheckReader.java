package io.choerodon.kb.domain.entity;

import java.util.Objects;

/**
 * @author superlee
 * @since 2022-10-13
 */
public class PermissionCheckReader {

    private String targetType;

    private Long targetValue;

    private Boolean approve;

    public static PermissionCheckReader of(String targetType,
                                           Long targetValue,
                                           Boolean approve) {
        PermissionCheckReader permissionCheckReader = new PermissionCheckReader();
        permissionCheckReader.setTargetType(targetType);
        permissionCheckReader.setTargetValue(targetValue);
        permissionCheckReader.setApprove(approve);
        return permissionCheckReader;
    }


    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
    }

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionCheckReader)) return false;
        PermissionCheckReader that = (PermissionCheckReader) o;
        return Objects.equals(getTargetType(), that.getTargetType()) &&
                Objects.equals(getTargetValue(), that.getTargetValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTargetType(), getTargetValue());
    }
}
