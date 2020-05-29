package io.choerodon.kb.infra.feign.vo;


/**
 * @author: 25499
 * @date: 2020/3/3 19:29
 * @description:
 */
public class OrganizationSimplifyDTO {

    private Long tenantId;

    private String tenantName;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}