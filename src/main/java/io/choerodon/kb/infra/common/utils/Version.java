package io.choerodon.kb.infra.common.utils;

import java.util.Arrays;
import java.util.StringTokenizer;

import io.choerodon.core.exception.CommonException;

/**
 * Created by Zenger on 2019/4/30.
 */
public class Version extends ToString implements Cloneable, Comparable {
    private int[] numbers = new int[0];

    public Version(int major) {
        this.numbers = new int[]{major};
    }

    public Version(int major, int minor) {
        this.numbers = new int[]{major, minor};
    }

    public Version(Integer[] num) {
        this.numbers = new int[num.length];

        for (int i = 0; i < num.length; ++i) {
            this.numbers[i] = num[i].intValue();
        }

    }

    public Version(int[] num) {
        this.numbers = (int[]) ((int[]) num.clone());
    }

    public Version(String v) throws CommonException {
        if (v.endsWith(".")) {
            v = v + "0";
        }

        StringTokenizer t = new StringTokenizer(v, ".");
        int count = t.countTokens();
        if (this.even(count) && v.endsWith(".0")) {
            --count;
        }

        this.numbers = new int[count];

        for (int i = 0; i < count; ++i) {
            try {
                this.numbers[i] = Integer.parseInt(t.nextToken());
            } catch (NumberFormatException var6) {
                throw new CommonException(v);
            }
        }

    }

    public Version(Version v) {
        this.numbers = (int[]) ((int[]) v.numbers.clone());
        if (!Arrays.equals(this.numbers, v.numbers)) {
            throw new IllegalStateException(this.numbers.toString());
        }
    }

    public Version() {
    }

    public Object clone() {
        return new Version(this);
    }

    public int[] getNumbers() {
        return (int[]) ((int[]) this.numbers.clone());
    }

    public int compareVersions(Version ver) {
        int[] nthis = this.numbers;
        int[] nthat = ver.numbers;

        int i;
        for (i = 0; i < nthis.length; ++i) {
            if (i >= nthat.length || nthis[i] > nthat[i]) {
                return 1;
            }

            if (nthis[i] < nthat[i]) {
                return -1;
            }
        }

        if (nthat.length > i) {
            return -1;
        } else {
            return 0;
        }
    }

    public int compareTo(Object other) {
        if (other == this) {
            return 0;
        } else if (!(other instanceof Version)) {
            throw new IllegalArgumentException(other.toString());
        } else {
            Version otherVer = (Version) other;
            return this.size() != otherVer.size() ? this.size() - otherVer.size() : -this.compareVersions(otherVer);
        }
    }

    public boolean isGreaterThan(Version ver) {
        return this.compareVersions(ver) > 0;
    }

    public boolean isGreaterOrEqualThan(Version ver) {
        return this.compareVersions(ver) >= 0;
    }

    public boolean isLessThan(Version ver) {
        return this.compareVersions(ver) < 0;
    }

    public boolean isLessOrEqualThan(Version ver) {
        return this.compareVersions(ver) <= 0;
    }

    public boolean equals(Object o) {
        return this == o ? true : (!(o instanceof Version) ? false : (this.hashCode() != o.hashCode() ? false : this.compareTo((Version) o) == 0));
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public int at(int pos) {
        return this.numbers[pos];
    }

    public int last() {
        return this.at(this.size() - 1);
    }

    public Version getBase(int positions) {
        positions = positions > this.numbers.length ? this.numbers.length : positions;
        int[] result = new int[positions];
        System.arraycopy(this.numbers, 0, result, 0, positions);
        return new Version(result);
    }

    public Version getBranchPoint() {
        return this.getBase(this.size() - 1);
    }

    public Version next() {
        Version result = new Version(this);
        result.numbers[this.numbers.length - 1] = this.last() + 1;
        return result;
    }

    public void __addBranch(Integer branch) {
        this.__addBranch(branch.intValue());
    }

    public void __addBranch(int branch) {
        int[] newnum = new int[this.numbers.length + 1];
        System.arraycopy(this.numbers, 0, newnum, 0, this.numbers.length);
        newnum[this.numbers.length] = branch;
        this.numbers = newnum;
    }

    public Version newBranch(int branch) {
        int[] newnum = new int[this.numbers.length + 1];
        System.arraycopy(this.numbers, 0, newnum, 0, this.numbers.length);
        newnum[this.numbers.length] = branch;
        Version result = new Version();
        result.numbers = newnum;
        return result;
    }

    public int size() {
        return this.numbers.length;
    }

    public boolean isTrunk() {
        return this.size() >= 1 && this.size() <= 2;
    }

    public boolean isBranch() {
        return this.size() > 2;
    }

    public boolean isRevision() {
        return this.even();
    }

    public boolean isGhost() {
        for (int i = 0; i < this.size(); ++i) {
            if (this.numbers[i] <= 0) {
                return true;
            }
        }

        return false;
    }

    public boolean even(int n) {
        return n % 2 == 0;
    }

    public boolean even() {
        return this.even(this.size());
    }

    public boolean odd(int n) {
        return !this.even(n);
    }

    public boolean odd() {
        return !this.even();
    }

    public void toString(StringBuffer s) {
        if (this.size() > 0) {
            s.append(Integer.toString(this.numbers[0]));

            for (int i = 1; i < this.numbers.length; ++i) {
                s.append(".");
                s.append(Integer.toString(this.numbers[i]));
            }
        }

    }
}
