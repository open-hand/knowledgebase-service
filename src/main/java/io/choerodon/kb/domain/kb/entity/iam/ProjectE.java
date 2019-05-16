//package io.choerodon.kb.domain.kb.entity.iam;
//
///**
// * Created by Zenger on 2019/5/14.
// */
//public class ProjectE {
//
//    private Long id;
//    private String name;
//    private String code;
//    private OrganizationE organization;
//    private Boolean enabled;
//
//    public ProjectE() {
//    }
//
//    public ProjectE(Long id) {
//        this.id = id;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//    public OrganizationE getOrganization() {
//        return organization;
//    }
//
//    public void setOrganization(OrganizationE organization) {
//        this.organization = organization;
//    }
//
//    public void initOrganizationE(Long id) {
//        organization = new OrganizationE(id);
//    }
//
//    public Boolean getEnabled() {
//        return enabled;
//    }
//
//    public void setEnabled(Boolean enabled) {
//        this.enabled = enabled;
//    }
//
//    @Override
//    public String toString() {
//        return "ProjectE{" +
//                "id=" + id +
//                ", name=" + name +
//                ", code=" + code +
//                ", enabled=" + enabled +
//                ", organization=" + organization.toString() +
//                '}';
//    }
//}
