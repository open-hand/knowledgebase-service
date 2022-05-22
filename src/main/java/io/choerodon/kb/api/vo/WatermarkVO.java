package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author superlee
 * @since 12/24/21
 */
public class WatermarkVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "组织id")
    private Long tenantId;

    @ApiModelProperty(value = "是否分享")
    private Boolean enable;

    @ApiModelProperty(value = "水印内容")
    private String waterMarkString;

    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;

    @ApiModelProperty(value = "是否开启水印")
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
