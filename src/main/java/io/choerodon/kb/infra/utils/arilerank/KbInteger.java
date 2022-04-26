package io.choerodon.kb.infra.utils.arilerank;

import java.util.Arrays;
import org.apache.commons.lang.builder.HashCodeBuilder;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/4/30.
 */
public class KbInteger implements Comparable<KbInteger> {
    private static final int[] ZERO_MAG = new int[]{0};
    private static final int[] ONE_MAG = new int[]{1};
    public static final int NEGATIVE_SIGN = -1;
    public static final int ZERO_SIGN = 0;
    public static final int POSITIVE_SIGN = 1;
    private final KbNumeralSystem sys;
    private final int sign;
    private final int[] mag;

    private static final String DIGIT_ERROR = "error.rank.leastOneDigit";
    private static final String NUMERAL_SYS_ERROR = "error.rank.numeralSysNotEqual";

    private KbInteger(KbNumeralSystem system, int sign, int[] mag) {
        this.sys = system;
        this.sign = sign;
        this.mag = mag;
    }

    public KbInteger add(KbInteger other) {
        this.checkSystem(other);
        if (this.isZero()) {
            return other;
        } else if (other.isZero()) {
            return this;
        } else if (this.sign != other.sign) {
            KbInteger pos;
            if (this.sign == -1) {
                pos = this.negate();
                KbInteger val = pos.subtract(other);
                return val.negate();
            } else {
                pos = other.negate();
                return this.subtract(pos);
            }
        } else {
            int[] result = add(this.sys, this.mag, other.mag);
            return make(this.sys, this.sign, result);
        }
    }

    private static int[] add(KbNumeralSystem sys, int[] l, int[] r) {
        int estimatedSize = Math.max(l.length, r.length);
        int[] result = new int[estimatedSize];
        int carry = 0;

        for (int i = 0; i < estimatedSize; ++i) {
            int lnum = i < l.length ? l[i] : 0;
            int rnum = i < r.length ? r[i] : 0;
            int sum = lnum + rnum + carry;

            for (carry = 0; sum >= sys.getBase(); sum -= sys.getBase()) {
                ++carry;
            }

            result[i] = sum;
        }

        return extendWithCarry(result, carry);
    }

    private static int[] extendWithCarry(int[] mag, int carry) {
        int[] result = mag;
        if (carry > 0) {
            int[] extendedMag = new int[mag.length + 1];
            System.arraycopy(mag, 0, extendedMag, 0, mag.length);
            extendedMag[extendedMag.length - 1] = carry;
            result = extendedMag;
        }

        return result;
    }

    public KbInteger subtract(KbInteger other) {
        this.checkSystem(other);
        if (this.isZero()) {
            return other.negate();
        } else if (other.isZero()) {
            return this;
        } else if (this.sign != other.sign) {
            return contrarySubtract(other);
        } else {
            return sameSubtract(other);
        }
    }

    private KbInteger contrarySubtract(KbInteger other) {
        KbInteger negate;
        if (this.sign == -1) {
            negate = this.negate();
            KbInteger sum = negate.add(other);
            return sum.negate();
        } else {
            negate = other.negate();
            return this.add(negate);
        }
    }

    private KbInteger sameSubtract(KbInteger other) {
        int cmp = compare(this.mag, other.mag);
        if (cmp == 0) {
            return zero(this.sys);
        } else if (cmp < 0) {
            return make(this.sys, this.sign == -1 ? 1 : -1, subtract(this.sys, other.mag, this.mag));
        } else {
            return make(this.sys, this.sign == -1 ? -1 : 1, subtract(this.sys, this.mag, other.mag));
        }
    }

    private static int[] subtract(KbNumeralSystem sys, int[] l, int[] r) {
        int[] rComplement = complement(sys, r, l.length);
        int[] rSum = add(sys, l, rComplement);
        rSum[rSum.length - 1] = 0;
        return add(sys, rSum, ONE_MAG);
    }

    public KbInteger multiply(KbInteger other) {
        this.checkSystem(other);
        if (this.isZero()) {
            return this;
        } else if (other.isZero()) {
            return other;
        } else if (this.isOneish()) {
            return this.sign == other.sign ? make(this.sys, 1, other.mag) : make(this.sys, -1, other.mag);
        } else if (other.isOneish()) {
            return this.sign == other.sign ? make(this.sys, 1, this.mag) : make(this.sys, -1, this.mag);
        } else {
            int[] newMag = multiply(this.sys, this.mag, other.mag);
            return this.sign == other.sign ? make(this.sys, 1, newMag) : make(this.sys, -1, newMag);
        }
    }

    private static int[] multiply(KbNumeralSystem sys, int[] l, int[] r) {
        int[] result = new int[l.length + r.length];

        for (int li = 0; li < l.length; ++li) {
            for (int ri = 0; ri < r.length; ++ri) {
                int resultIndex = li + ri;

                for (result[resultIndex] += l[li] * r[ri]; result[resultIndex] >= sys.getBase(); result[resultIndex] -= sys.getBase()) {
                    ++result[resultIndex + 1];
                }
            }
        }

        return result;
    }

    public KbInteger negate() {
        if (this.isZero()) {
            return this;
        } else {
            return make(this.sys, this.sign == 1 ? -1 : 1, this.mag);
        }
    }

    public KbInteger shiftLeft() {
        return this.shiftLeft(1);
    }

    public KbInteger shiftLeft(int times) {
        if (times == 0) {
            return this;
        } else if (times < 0) {
            return this.shiftRight(Math.abs(times));
        } else {
            int[] nmag = new int[this.mag.length + times];
            System.arraycopy(this.mag, 0, nmag, times, this.mag.length);
            return make(this.sys, this.sign, nmag);
        }
    }

    public KbInteger shiftRight() {
        return this.shiftRight(1);
    }

    public KbInteger shiftRight(int times) {
        if (times == 0) {
            return this;
        } else if (times < 0) {
            return this.shiftLeft(Math.abs(times));
        } else if (this.mag.length - times <= 0) {
            return zero(this.sys);
        } else {
            int[] nmag = new int[this.mag.length - times];
            System.arraycopy(this.mag, times, nmag, 0, nmag.length);
            return make(this.sys, this.sign, nmag);
        }
    }

    public KbInteger complement() {
        return this.complement(this.mag.length);
    }

    public KbInteger complement(int digits) {
        return make(this.sys, this.sign, complement(this.sys, this.mag, digits));
    }

    private static int[] complement(KbNumeralSystem sys, int[] mag, int digits) {
        if (digits <= 0) {
            throw new CommonException(DIGIT_ERROR);
        } else {
            int[] nmag = new int[digits];
            Arrays.fill(nmag, sys.getBase() - 1);

            for (int i = 0; i < mag.length; ++i) {
                nmag[i] = sys.getBase() - 1 - mag[i];
            }

            return nmag;
        }
    }

    public boolean isZero() {
        return this.sign == 0 && this.mag.length == 1 && this.mag[0] == 0;
    }

    private boolean isOneish() {
        return this.mag.length == 1 && this.mag[0] == 1;
    }

    public boolean isOne() {
        return this.sign == 1 && this.mag.length == 1 && this.mag[0] == 1;
    }

    int getMag(int index) {
        return this.mag[index];
    }

    int getMagSize() {
        return this.mag.length;
    }

    @Override
    public int compareTo(KbInteger o) {
        if (this.sign == -1) {
            return compare(o);
        } else if (this.sign == 1) {
            return o.sign == 1 ? compare(this.mag, o.mag) : 1;
        } else {
            if (o.sign == -1) {
                return 1;
            } else {
                return o.sign == 1 ? -1 : 0;
            }
        }
    }

    private int compare(KbInteger o) {
        if (o.sign == -1) {
            int cmp = compare(this.mag, o.mag);
            if (cmp == -1) {
                return 1;
            } else {
                return cmp == 1 ? -1 : 0;
            }
        } else {
            return -1;
        }
    }

    private static int compare(int[] l, int[] r) {
        if (l.length < r.length) {
            return -1;
        } else if (l.length > r.length) {
            return 1;
        } else {
            for (int i = l.length - 1; i >= 0; --i) {
                if (l[i] < r[i]) {
                    return -1;
                }

                if (l[i] > r[i]) {
                    return 1;
                }
            }

            return 0;
        }
    }

    public KbNumeralSystem getSystem() {
        return this.sys;
    }

    private void checkSystem(KbInteger other) {
        if (this.sys != other.sys) {
            throw new CommonException(NUMERAL_SYS_ERROR);
        }
    }

    public String format() {
        if (this.isZero()) {
            return "" + this.sys.toChar(0);
        } else {
            StringBuilder sb = new StringBuilder();
            int[] var2 = this.mag;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                int digit = var2[var4];
                sb.insert(0, this.sys.toChar(digit));
            }

            if (this.sign == -1) {
                sb.insert(0, this.sys.getNegativeChar());
            }

            return sb.toString();
        }
    }

    public static KbInteger parse(String strFull, KbNumeralSystem system) {
        String str = strFull;
        int sign = 1;
        if (strFull.indexOf(system.getPositiveChar()) == 0) {
            str = strFull.substring(1);
        } else if (strFull.indexOf(system.getNegativeChar()) == 0) {
            str = strFull.substring(1);
            sign = -1;
        }

        int[] mag = new int[str.length()];
        int strIndex = mag.length - 1;

        for (int magIndex = 0; strIndex >= 0; --strIndex, ++magIndex) {
            mag[magIndex] = system.toDigit(str.charAt(strIndex));
        }

        return make(system, sign, mag);
    }

    protected static KbInteger zero(KbNumeralSystem sys) {
        return new KbInteger(sys, 0, ZERO_MAG);
    }

    protected static KbInteger one(KbNumeralSystem sys) {
        return make(sys, 1, ONE_MAG);
    }

    /**
     * 从后往前查看连着为0的数，并截取掉返回构建的新对象
     *
     * @param sys  sys
     * @param sign sign
     * @param mag  mag
     * @return KbInteger
     */
    public static KbInteger make(KbNumeralSystem sys, int sign, int[] mag) {
        int actualLength;
        for (actualLength = mag.length; actualLength > 0; --actualLength) {
            if (mag[actualLength - 1] != 0) {
                break;
            }
        }

        if (actualLength == 0) {
            return zero(sys);
        } else if (actualLength == mag.length) {
            return new KbInteger(sys, sign, mag);
        } else {
            int[] nmag = new int[actualLength];
            System.arraycopy(mag, 0, nmag, 0, actualLength);
            return new KbInteger(sys, sign, nmag);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KbInteger)) {
            return false;
        } else {
            KbInteger o = (KbInteger) obj;
            return this.sys == o.sys && this.compareTo(o) == 0;
        }
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public String toString() {
        return this.format();
    }
}

