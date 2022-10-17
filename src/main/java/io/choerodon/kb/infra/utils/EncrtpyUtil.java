package io.choerodon.kb.infra.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import io.choerodon.kb.api.vo.WorkSpaceTreeNodeVO;
import io.choerodon.kb.api.vo.WorkSpaceVO;

import org.hzero.core.base.BaseConstants;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

/**
 * @author jiaxu.cui@hand-china.com 2020/6/19 下午4:13
 */
public class EncrtpyUtil {
    public static final String BLANK_KEY = "";
    public static <T> ImmutablePair<String, T> encryptMap(Map.Entry<Long, T> entry,
                                                          IEncryptionService encryptionService,
                                                          String key,
                                                          Function<Object, T> function){
        return new ImmutablePair<>(encryptionService.encrypt(entry.getKey().toString(), key),
                function.apply(entry.getValue()));
    }

    public static ImmutablePair<String, WorkSpaceTreeNodeVO> encryptWsMap(Map.Entry<Long, WorkSpaceTreeNodeVO> entry, IEncryptionService encryptionService){
        Function<Object, WorkSpaceTreeNodeVO> func = v -> {
            if (Objects.isNull(v)){
                return null;
            }
            if (StringUtils.isBlank(((WorkSpaceTreeNodeVO) v).getRoute())){
                return (WorkSpaceTreeNodeVO) v;
            }
            EncrtpyUtil.entryWsRoute((WorkSpaceTreeNodeVO) v, encryptionService);
            return (WorkSpaceTreeNodeVO) v;
        };
        return encryptMap(entry, encryptionService, BLANK_KEY, func);
    }

    /**
     * route加密
     * @param ws WorkSpaceVO
     * @param encryptionService encryptionService
     * @return String
     */
    public static String entryRoute(WorkSpaceVO ws, IEncryptionService encryptionService){
        return
                Optional.ofNullable(StringUtils.split(ws.getRoute(), BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, BLANK_KEY))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
    }

    /**
     * route加密
     * @param ws WorkSpaceInfoVO
     * @param encryptionService encryptionService
     * @return String
     */
    public static String entryRoute(String route, IEncryptionService encryptionService){
        return
                Optional.ofNullable(StringUtils.split(route, BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, BLANK_KEY))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
    }

    /**
     * route加密
     * @param ws WorkSpaceTreeVO
     * @param encryptionService encryptionService
     * @return WorkSpaceTreeVO
     */
    public static WorkSpaceTreeNodeVO entryWsRoute(WorkSpaceTreeNodeVO ws, IEncryptionService encryptionService){
        ws.setRoute(
                Optional.ofNullable(StringUtils.split(ws.getRoute(), BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, BLANK_KEY))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null));
        return ws;
    }
}
