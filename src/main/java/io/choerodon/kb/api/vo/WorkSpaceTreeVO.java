package io.choerodon.kb.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库对象树VO
 * @author gaokuo.dai@zknow.com 2022-10-17
 */
@ApiModel(value = "知识库对象树VO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkSpaceTreeVO {

    @Encrypt(ignoreValue = "0")
    @ApiModelProperty(value = "根节点ID")
    private Long rootId;

    @ApiModelProperty(value = "文档树类型")
    private String treeTypeCode;

    @ApiModelProperty(value = "节点数据")
    private List<WorkSpaceTreeNodeVO> nodeList;

    @ApiModelProperty(value = "是否启用")
    private Boolean enabledFlag;

    @ApiModelProperty(value = "共享类型")
    private String shareType;

    /**
     * @return 根节点ID
     */
    public Long getRootId() {
        return rootId;
    }

    public WorkSpaceTreeVO setRootId(Long rootId) {
        this.rootId = rootId;
        return this;
    }

    /**
     * @return 文档树类型
     */
    public String getTreeTypeCode() {
        return treeTypeCode;
    }

    public WorkSpaceTreeVO setTreeTypeCode(String treeTypeCode) {
        this.treeTypeCode = treeTypeCode;
        return this;
    }

    /**
     * @return 节点数据
     */
    public List<WorkSpaceTreeNodeVO> getNodeList() {
        return nodeList;
    }

    public WorkSpaceTreeVO setNodeList(List<WorkSpaceTreeNodeVO> nodeList) {
        this.nodeList = nodeList;
        return this;
    }

    /**
     * @return 是否启用
     */
    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public WorkSpaceTreeVO setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
        return this;
    }

    /**
     * @return 共享类型
     */
    public String getShareType() {
        return shareType;
    }

    public WorkSpaceTreeVO setShareType(String shareType) {
        this.shareType = shareType;
        return this;
    }
}
