package io.choerodon.kb.api.vo;

import java.util.Objects;

import io.choerodon.kb.infra.constants.EncryptConstants;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

import javax.validation.constraints.NotNull;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public class PageCreateWithoutContentVO {
    @NotNull
    @ApiModelProperty(value = "父级工作空间ID，顶级目录则传0L")
    private Object parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;

    private String description;

    @Encrypt(EncryptConstants.TN_KB_KNOWLEDGE_BASE)
    private Long baseId;

    public Long getParentWorkspaceId() {
        return Long.valueOf((String)parentWorkspaceId);
    }

    public void setParentWorkspaceId(Long parentWorkspaceId) {
        this.parentWorkspaceId = parentWorkspaceId;
    }

    public void dencrypt(IEncryptionService encryptionService) {
        if (!Objects.equals(this.parentWorkspaceId.toString(), "0")){
            this.parentWorkspaceId = Long.valueOf(encryptionService
                    .decrypt(String.valueOf(this.parentWorkspaceId), EncryptConstants.TN_KB_WORKSPACE));
        }
    }

    public void setParentWorkspaceId(String parentWorkspaceId) {
        this.parentWorkspaceId = parentWorkspaceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
