package io.choerodon.kb.api.dao;

import difflib.Delta;
import io.choerodon.kb.infra.common.utils.diff.MyersDiff;
import io.choerodon.kb.infra.common.utils.diff.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据TextDiffDTO来生成可以在页面展示的文本差异
 *
 * @author shinan.chen
 * @since 2019/5/28
 */
public class DiffHandleDTO {

    private Delta.TYPE type;
    private List<String> strs;
    private Integer skipLine;
    public static final String INSERT_TAG_BEGIN = "<font style='background:rgba(0,191,165,0.16);'>";
    public static final String INSERT_TAG_END = "</font>";
    public static final String DELETE_TAG_BEGIN = "<font style='background: rgba(226,59,59,0.16);'><s>";
    public static final String DELETE_TAG_END = "</s></font>";

    public DiffHandleDTO(Builder builder) {
        this.strs = builder.strs;
        this.type = builder.type;
        this.skipLine = builder.skipLine;
    }

    public static class Builder {
        private Delta.TYPE type;
        private List<String> strs;
        private Integer skipLine;

        /**
         * 处理增加行的标记
         * @param list
         * @return
         */
        public Builder insert(List<String> list) {
            type = Delta.TYPE.INSERT;
            skipLine = list.size();
            strs = new ArrayList<>(list.size());
            for (String str : list) {
                strs.add(INSERT_TAG_BEGIN + str + INSERT_TAG_END);
            }
            return this;
        }

        /**
         * 处理删除行的标记
         * @param list
         * @return
         */
        public Builder delete(List<String> list) {
            type = Delta.TYPE.DELETE;
            strs = new ArrayList<>(list.size());
            for (String str : list) {
                strs.add(DELETE_TAG_BEGIN + str + DELETE_TAG_END);
            }
            return this;
        }

        /**
         * 处理改变行的标记，只有当增加行与删除行数量相等时，才进行单行的比较
         * 单行比较差异采用myersDiff.buildPath
         * @param delete
         * @param insert
         * @return
         */
        public Builder change(List<String> delete, List<String> insert) {
            if (delete.size() == insert.size()) {
                strs = new ArrayList<>(delete.size());
                type = Delta.TYPE.CHANGE;
                skipLine = delete.size();
                try {
                    //处理改变
                    for (int i = 0; i < delete.size(); i++) {
                        List<String> ori = char2String(delete.get(i).toCharArray());
                        List<String> rev = char2String(insert.get(i).toCharArray());
                        MyersDiff myersDiff = new MyersDiff<String>();
                        PathNode pathNode = myersDiff.buildPath(ori, rev);
                        strs.add(myersDiff.buildDiff(pathNode, ori, rev));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                strs = new ArrayList<>(delete.size() + insert.size());
                type = Delta.TYPE.CHANGE;
                skipLine = insert.size();
                for (String str : insert) {
                    strs.add(INSERT_TAG_BEGIN + str + INSERT_TAG_END);
                }
                for (String str : delete) {
                    strs.add(DELETE_TAG_BEGIN + str + DELETE_TAG_END);
                }
            }
            return this;
        }

        public DiffHandleDTO build() {
            return new DiffHandleDTO(this);
        }

        private List<String> char2String(char[] chars) {
            List<String> strs = new ArrayList<>(chars.length);
            for (char c : chars) {
                strs.add(String.valueOf(c));
            }
            return strs;
        }
    }

    public Delta.TYPE getType() {
        return type;
    }

    public void setType(Delta.TYPE type) {
        this.type = type;
    }

    public List<String> getStrs() {
        return strs;
    }

    public void setStrs(List<String> strs) {
        this.strs = strs;
    }

    public Integer getSkipLine() {
        return skipLine;
    }

    public void setSkipLine(Integer skipLine) {
        this.skipLine = skipLine;
    }
}
