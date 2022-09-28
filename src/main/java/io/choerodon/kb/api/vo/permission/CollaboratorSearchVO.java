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

    @NotBlank
    private String targetType;

    @NotNull
    @Encrypt
    private Long targetValue;

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
}
