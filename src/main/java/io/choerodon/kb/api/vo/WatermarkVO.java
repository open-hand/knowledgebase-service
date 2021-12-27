package io.choerodon.kb.api.vo;

/**
 * @author superlee
 * @since 12/24/21
 */
public class WatermarkVO {

    private Long id;

    private Long tenantId;

    private Boolean enable;

    private String waterMarkString;

    private Long objectVersionNumber;

    private Boolean doWaterMark;

    public Boolean getDoWaterMark() {
        return doWaterMark;
    }

    public void setDoWaterMark(Boolean doWaterMark) {
        this.doWaterMark = doWaterMark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getWaterMarkString() {
        return waterMarkString;
    }

    public void setWaterMarkString(String waterMarkString) {
        this.waterMarkString = waterMarkString;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
