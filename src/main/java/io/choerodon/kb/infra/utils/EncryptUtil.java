package io.choerodon.kb.infra.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.internal.util.Assert;

import org.hzero.core.base.BaseConstants;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

/**
 * @author jiaxu.cui@hand-china.com 2020/6/19 下午4:13
 */
public class EncryptUtil {
    public static final String BLANK_KEY = "";

    /**
     * route加密
     * @param plaintextRoute    明文Route
     * @param encryptionService 加密Service
     * @return                  密文Route
     */
    public static String entryRoute(String plaintextRoute, IEncryptionService encryptionService){
        Assert.notNull(encryptionService, BaseConstants.ErrorCode.NOT_NULL);
        if(StringUtils.isBlank(plaintextRoute)) {
            return plaintextRoute;
        }
        return Arrays.stream(StringUtils.split(plaintextRoute, BaseConstants.Symbol.POINT))
                .map(plaintextId -> encryptionService.encrypt(plaintextId, BLANK_KEY))
                .collect(Collectors.joining(BaseConstants.Symbol.POINT));
    }

}
