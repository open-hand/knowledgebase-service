package io.choerodon.kb.infra.utils;

import java.lang.reflect.Field;

import io.choerodon.core.exception.CommonException;

/**
 * @author shinan.chen
 * @date 2018/10/24
 */
public class EnumUtil {

    private EnumUtil() {
    }

    /**
     * 枚举类通用校验
     *
     * @param cls
     * @param statusType
     * @return
     */
    public static Boolean contain(Class cls, String statusType) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            try {
                String type = String.valueOf(field.get(cls));
                if (type.equals(statusType)) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                throw new CommonException("error.enumUtil.contain", e);
            }
        }
        return false;
    }
}
