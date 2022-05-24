package io.choerodon.kb.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by wangxiang on 2022/4/9
 */
@Table(name = "kb_wps_file_version")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileVersionDTO extends AuditDomain {
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @ApiModelProperty("文件的fileId")
    private String fileId;
    @ApiModelProperty("文件的名字")
    private String name;
    @ApiModelProperty("文件袋额版本")
    private Integer version;
    @ApiModelProperty("文件的大小")
    private Long fileSize;
    @ApiModelProperty("文件的fileKey")
    private String fileKey;
    @ApiModelProperty("文件的MD5码")
    private String md5;
    @ApiModelProperty("文件的url")
    private String fileUrl;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
