package io.choerodon.kb.infra.utils.arilerank;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/4/30.
 */
public class KbSystemHelper {
    private static final List<KbInteger> BASE_36_DIVIDERS;
    private static final String NUMERAL_SYSTEM_ERROR = "error.rank.unsupportedNumeralSystem";

    private KbSystemHelper() {
    }

    public static List<KbDecimal> getBaseDivisors(KbNumeralSystem lexoNumeralSystem, int fractionMagnitude) {
        int base = lexoNumeralSystem.getBase();
        if (base == KbNumeralSystem.BASE_36.getBase()) {
            return fractionMagnitude < 0 ? BASE_36_DIVIDERS.stream().map(lexoInteger ->
                    KbDecimal.make(lexoInteger.shiftLeft(fractionMagnitude * -1), 0)
            ).collect(Collectors.toList()) : BASE_36_DIVIDERS.stream().map(lexoInteger ->
                    KbDecimal.make(lexoInteger, fractionMagnitude)
            ).collect(Collectors.toList());
        } else {
            throw new CommonException(NUMERAL_SYSTEM_ERROR);
        }
    }

    static {
        List<Object> list =
                ImmutableList
                        .builder()
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{18}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{12}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{9}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{6}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{4}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{3}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{2}))
                        .add(KbInteger.make(KbNumeralSystem.BASE_36, 1, new int[]{1}))
                        .build();
        List<KbInteger> lexoIntegers = new ArrayList<>();
        list.forEach(object -> lexoIntegers.add((KbInteger) (object)));
        BASE_36_DIVIDERS = lexoIntegers;
    }
}
