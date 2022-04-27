package io.choerodon.kb.infra.utils.arilerank;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/4/30.
 */
public class KbRank implements Comparable<KbRank> {
    public static final KbNumeralSystem NUMERAL_SYSTEM;
    private static final KbDecimal ZERO_DECIMAL;
    private static final KbDecimal ONE_DECIMAL;
    private static final KbDecimal EIGHT_DECIMAL;
    private static final KbDecimal MIN_DECIMAL;
    private static final KbDecimal MAX_DECIMAL;
    public static final KbDecimal MID_DECIMAL;
    private static final KbDecimal INITIAL_MIN_DECIMAL;
    private static final KbDecimal INITIAL_MAX_DECIMAL;
    private final String value;
    private KbRankBucket bucket;
    private KbDecimal decimal;

    private static final String BUCKET_ERROR = "error.rank.bucketNotEqual";
    private static final String NUMERAL_SYSTEM_ERROR = "error.rank.numeralSysNotEqual";
    private static final String RANK_ERROR = "error.rank.notBetweenRank";
    private static final String DISTANCE_ERROR = "error.rank.notSuitableDistance";

    private KbRank(String value) {
        this.value = value;
    }

    private KbRank(KbRankBucket bucket, KbDecimal decimal) {
        this.value = bucket.format() + "|" + formatDecimal(decimal);
        this.bucket = bucket;
        this.decimal = decimal;
    }

    public static KbRank min() {
        return from(KbRankBucket.BUCKET_0, MIN_DECIMAL);
    }

    public static KbRank max() {
        return max(KbRankBucket.BUCKET_0);
    }

    public static KbRank max(KbRankBucket bucket) {
        return from(bucket, MAX_DECIMAL);
    }

    public static KbRank initial(KbRankBucket bucket) {
        return bucket == KbRankBucket.BUCKET_0?from(bucket, INITIAL_MIN_DECIMAL):from(bucket, INITIAL_MAX_DECIMAL);
    }

    public KbRank genPrev() {
        this.fillDecimal();
        if(this.isMax()) {
            return new KbRank(this.bucket, INITIAL_MAX_DECIMAL);
        } else {
            KbInteger floorInteger = this.decimal.floor();
            KbDecimal floorDecimal = KbDecimal.from(floorInteger);
            KbDecimal nextDecimal = floorDecimal.subtract(EIGHT_DECIMAL);
            if(nextDecimal.compareTo(MIN_DECIMAL) <= 0) {
                nextDecimal = between(MIN_DECIMAL, this.decimal);
            }

            return new KbRank(this.bucket, nextDecimal);
        }
    }

    public KbRank genNext() {
        this.fillDecimal();
        if(this.isMin()) {
            return new KbRank(this.bucket, INITIAL_MIN_DECIMAL);
        } else {
            KbInteger ceilInteger = this.decimal.ceil();
            KbDecimal ceilDecimal = KbDecimal.from(ceilInteger);
            KbDecimal nextDecimal = ceilDecimal.add(EIGHT_DECIMAL);
            if(nextDecimal.compareTo(MAX_DECIMAL) >= 0) {
                nextDecimal = between(this.decimal, MAX_DECIMAL);
            }

            return new KbRank(this.bucket, nextDecimal);
        }
    }

    public KbRank between(KbRank other) {
        return this.between(other, 0);
    }

    public KbRank between(KbRank other, int capacity) {
        this.fillDecimal();
        other.fillDecimal();
        if(!this.bucket.equals(other.bucket)) {
            throw new CommonException(BUCKET_ERROR);
        } else {
            int cmp = this.decimal.compareTo(other.decimal);
            if(cmp > 0) {
                return new KbRank(this.bucket, between(other.decimal, this.decimal, capacity));
            } else if(cmp == 0) {
                throw new CommonException(RANK_ERROR);
            } else {
                return new KbRank(this.bucket, between(this.decimal, other.decimal, capacity));
            }
        }
    }

    public static KbDecimal between(KbDecimal oLeft, KbDecimal oRight) {
        return between(oLeft, oRight, 0);
    }

    public static KbDecimal between(KbDecimal left, KbDecimal right, int spaceToRemain) {
        KbNumeralSystem system = left.getSystem();
        if(system != right.getSystem()) {
            throw new CommonException(NUMERAL_SYSTEM_ERROR);
        } else {
            //将right与left相减
            KbDecimal space = right.subtract(left);
            int capacity = spaceToRemain + 2;
            KbDecimal spacing = findSpacing(space, capacity);
            KbDecimal floor = floorToSpacingDivisor(left, spacing);
            return roundToSpacing(left, floor, spacing);
        }
    }

    private static KbDecimal findSpacing(KbDecimal space, int capacity) {
        //capacity自然对数除space.getSystem().getBase()计算容量
        int capacityMagnitude = (int)Math.floor(Math.log((double)capacity) / Math.log((double)space.getSystem().getBase()));
        //int数组长度-”:“后字符长度 -1 - capacityMagnitude
        int spacingMagnitude = space.getOrderOfMagnitude() - capacityMagnitude;
        //
        KbDecimal lexoCapacity = KbDecimal.fromInt(capacity, space.getSystem());
        Iterator var5 = getSystemBaseDivisors(space.getSystem(), spacingMagnitude).iterator();

        KbDecimal spacingCandidate;
        do {
            if(!var5.hasNext()) {
                throw new CommonException(DISTANCE_ERROR);
            }

            spacingCandidate = (KbDecimal)var5.next();
        } while(space.compareTo(spacingCandidate.multiply(lexoCapacity)) < 0);

        return spacingCandidate;
    }

    private static List<KbDecimal> getSystemBaseDivisors(KbNumeralSystem lexoNumeralSystem, int magnitude) {
        int fractionMagnitude = magnitude * -1;
        int adjacentFractionMagnitude = fractionMagnitude + 1;
        List<Object> list = ImmutableList.builder().addAll(KbSystemHelper.getBaseDivisors(lexoNumeralSystem, fractionMagnitude)).addAll(KbSystemHelper.getBaseDivisors(lexoNumeralSystem, adjacentFractionMagnitude)).build();
        List<KbDecimal> lexoDecimals = new ArrayList<>();
        list.forEach(object ->lexoDecimals.add((KbDecimal)(object)));
        return lexoDecimals;
    }

    private static KbDecimal floorToSpacingDivisor(KbDecimal number, KbDecimal spacing) {
        KbDecimal zero = KbDecimal.from(KbInteger.zero(number.getSystem()));
        if(zero.equals(number)) {
            return spacing;
        } else {
            KbInteger spacingsMag = spacing.getMag();
            int scaleDifference = number.getScale() + spacing.getOrderOfMagnitude();
            int spacingsMostSignificantDigit = spacingsMag.getMagSize() - 1;

            KbInteger floor;
            for(floor = number.getMag().shiftRight(scaleDifference).add(KbInteger.one(number.getSystem())); floor.getMag(0) % spacingsMag.getMag(spacingsMostSignificantDigit) != 0;) {
                floor = floor.add(KbInteger.one(number.getSystem()));
            }
            return number.getScale() - scaleDifference > 0? KbDecimal.make(floor, number.getScale() - scaleDifference): KbDecimal.make(floor.shiftLeft(scaleDifference), number.getScale());
        }
    }

    private static KbDecimal roundToSpacing(KbDecimal number, KbDecimal floor, KbDecimal spacing) {
        KbDecimal halfSpacing = spacing.multiply(KbDecimal.half(spacing.getSystem()));
        KbDecimal difference = floor.subtract(number);
        return difference.compareTo(halfSpacing) >= 0?floor:floor.add(spacing);
    }

    private static KbDecimal mid(KbDecimal left, KbDecimal right) {
        KbDecimal sum = left.add(right);
        KbDecimal mid = sum.multiply(KbDecimal.half(left.getSystem()));
        int scale = Math.max(left.getScale(), right.getScale());
        if(mid.getScale() > scale) {
            KbDecimal roundDown = mid.setScale(scale, false);
            if(roundDown.compareTo(left) > 0) {
                return roundDown;
            }

            KbDecimal roundUp = mid.setScale(scale, true);
            if(roundUp.compareTo(right) < 0) {
                return roundUp;
            }
        }

        return mid;
    }

    private void fillDecimal() {
        if(this.decimal == null) {
            String[] parts = this.value.split("\\|");
            this.bucket = KbRankBucket.from(parts[0]);
            this.decimal = KbDecimal.parse(parts[1], NUMERAL_SYSTEM);
        }

    }

    public KbRankBucket getBucket() {
        this.fillDecimal();
        return this.bucket;
    }

    public KbDecimal getDecimal() {
        this.fillDecimal();
        return this.decimal;
    }

    public KbRank inNextBucket() {
        this.fillDecimal();
        return from(this.bucket.next(), this.decimal);
    }

    public KbRank inPrevBucket() {
        this.fillDecimal();
        return from(this.bucket.prev(), this.decimal);
    }

    public boolean isMin() {
        this.fillDecimal();
        return this.decimal.equals(MIN_DECIMAL);
    }

    public boolean isMax() {
        this.fillDecimal();
        return this.decimal.equals(MAX_DECIMAL);
    }

    public String format() {
        return this.value;
    }

    public static String formatDecimal(KbDecimal decimal) {
        String formatVal = decimal.format();
        StringBuilder val = new StringBuilder(formatVal);
        int partialIndex = formatVal.indexOf(NUMERAL_SYSTEM.getRadixPointChar());
        char zero = NUMERAL_SYSTEM.toChar(0);
        if(partialIndex < 0) {
            partialIndex = formatVal.length();
            val.append(NUMERAL_SYSTEM.getRadixPointChar());
        }

        while(partialIndex < 6) {
            val.insert(0, zero);
            ++partialIndex;
        }

        while(val.charAt(val.length() - 1) == zero) {
            val.setLength(val.length() - 1);
        }

        return val.toString();
    }

    public static KbRank parse(String str) {
        return new KbRank(str);
    }

    public static KbRank from(KbRankBucket bucket, KbDecimal decimal) {
        if(decimal.getSystem() != NUMERAL_SYSTEM) {
            throw new CommonException(NUMERAL_SYSTEM_ERROR);
        } else {
            return new KbRank(bucket, decimal);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof KbRank && (this == o || this.value.equals(((KbRank) o).value));
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int compareTo(KbRank o) {
        return this.value.compareTo(o.value);
    }

    static {
        NUMERAL_SYSTEM = KbNumeralSystem.BASE_36;
        ZERO_DECIMAL = KbDecimal.parse("0", NUMERAL_SYSTEM);
        ONE_DECIMAL = KbDecimal.parse("1", NUMERAL_SYSTEM);
        EIGHT_DECIMAL = KbDecimal.parse("8", NUMERAL_SYSTEM);
        MIN_DECIMAL = ZERO_DECIMAL;
        MAX_DECIMAL = KbDecimal.parse("1000000", NUMERAL_SYSTEM).subtract(ONE_DECIMAL);
        MID_DECIMAL = mid(MIN_DECIMAL, MAX_DECIMAL);
        INITIAL_MIN_DECIMAL = KbDecimal.parse("100000", NUMERAL_SYSTEM);
        INITIAL_MAX_DECIMAL = KbDecimal.parse(NUMERAL_SYSTEM.toChar(NUMERAL_SYSTEM.getBase() - 2) + "00000", NUMERAL_SYSTEM);
    }
}

