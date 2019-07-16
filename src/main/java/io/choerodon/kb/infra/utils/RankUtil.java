package io.choerodon.kb.infra.utils;

import io.choerodon.kb.infra.utils.arilerank.KbRank;

/**
 * Created by Zenger on 2019/4/30.
 */
public class RankUtil {

    private RankUtil() {
    }

    public static String mid() {
        KbRank minRank = KbRank.min();
        KbRank maxRank = KbRank.max();
        return minRank.between(maxRank).format();
    }

    public static String genNext(String rank) {
        return KbRank.parse(rank).genNext().format();
    }

    public static String genPre(String minRank) {
        return KbRank.parse(minRank).genPrev().format();
    }

    public static String between(String leftRank, String rightRank) {
        KbRank left = KbRank.parse(leftRank);
        KbRank right = KbRank.parse(rightRank);
        return left.between(right).format();
    }
}
