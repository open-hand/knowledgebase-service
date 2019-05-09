package io.choerodon.kb.infra.common.utils.arilerank;

import java.util.Objects;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/4/30.
 */
public enum KbRankBucket {
    BUCKET_0("0"),
    BUCKET_1("1"),
    BUCKET_2("2");

    private static final String BUCKET_ERROR = "error.rank.unknownBucket";
    private static final String RANK_ERROR = "error.rank.illegalRankValue";

    private final KbInteger value;

    private KbRankBucket(String val) {
        this.value = KbInteger.parse(val, KbRank.NUMERAL_SYSTEM);
    }

    public static KbRankBucket resolve(int bucketId) {
        KbRankBucket[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            KbRankBucket bucket = var1[var3];
            if (Objects.equals(bucket.value, String.valueOf(bucketId))) {
                return bucket;
            }
        }

        throw new CommonException(BUCKET_ERROR);
    }

    public String format() {
        return this.value.format();
    }

    public KbRankBucket next() {
        switch (this.ordinal()) {
            case 1:
                return BUCKET_1;
            case 2:
                return BUCKET_2;
            case 3:
                return BUCKET_0;
            default:
                throw new CommonException(BUCKET_ERROR);
        }
    }

    public KbRankBucket prev() {
        switch (this.ordinal()) {
            case 1:
                return BUCKET_2;
            case 2:
                return BUCKET_0;
            case 3:
                return BUCKET_1;
            default:
                throw new CommonException(BUCKET_ERROR);
        }
    }

    public static KbRankBucket fromRank(String rank) {
        String bucket = rank.substring(0, rank.indexOf('|'));
        return from(bucket);
    }

    public static KbRankBucket from(String str) {
        KbInteger val = KbInteger.parse(str, KbRank.NUMERAL_SYSTEM);
        KbRankBucket[] var2 = values();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            KbRankBucket bucket = var2[var4];
            if (bucket.value.equals(val)) {
                return bucket;
            }
        }

        throw new CommonException(BUCKET_ERROR);
    }

    public static KbRankBucket max() {
        KbRankBucket[] values = values();
        return values[values.length - 1];
    }

    public Integer intValue() {
        switch (this.ordinal()) {
            case 1:
                return Integer.valueOf(0);
            case 2:
                return Integer.valueOf(1);
            case 3:
                return Integer.valueOf(2);
            default:
                throw new CommonException(RANK_ERROR);
        }
    }
}
