package io.choerodon.kb.api.vo.permission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 * 查询已有协作者vo
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollaboratorSearchVO {

    /**
     * 前端组织层和项目层使用同一组件，所以后端根据此字段转换为 {@link io.choerodon.kb.api.vo.permission.CollaboratorSearchVO#targetType}
     */
    @NotBlank
    private String baseTargetType;

    /**
     * 实际使用字段
     */
    private String targetType;

    @NotNull
    @Encrypt
    private Long targetValue;

    public String getBaseTargetType() {
        return baseTargetType;
    }

    public void setBaseTargetType(String baseTargetType) {
        this.baseTargetType = baseTargetType;
    }

    public Long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
