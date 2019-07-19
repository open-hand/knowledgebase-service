package io.choerodon.kb.api.vo;

import difflib.Delta;
import io.choerodon.kb.infra.utils.diff.MyersDiff;
import io.choerodon.kb.infra.utils.diff.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据TextDiffVO来生成可以在页面展示的文本差异
 *
 * @author shinan.chen
 * @since 2019/5/28
 */
public class DiffHandleVO {

    private Delta.TYPE type;
    private List<String> strs;
    private Integer skipLine;
    public static final String INSERT_TAG_BEGIN = "<span style='background:rgba(0,191,165,0.16);display:inline-block'>";
    public static final String INSERT_TAG_END = "</span>";
    public static final String DELETE_TAG_BEGIN = "<span style='background:rgba(226,59,59,0.16);display:inline-block'>";
    public static final String DELETE_TAG_END = "</span>";
    public static final String IMG_BEGIN = "<img src='";
    public static final String IMG_END = "alt='img'>";
    public static final String IMG_INSERT_TAG_BEGIN = "<span style='border: 6px solid rgba(0,191,165,0.16);display:inline-block;vertical-align:top'>";
    public static final String IMG_DELETE_TAG_BEGIN = "<span style='border: 6px solid rgba(226,59,59,0.16);display:inline-block;vertical-align:top'>";
    public static final String IMG_TAG_END = "</span>";
    public static final String HEAD_TAG_BEGIN_1 = "<span style='font-weight:bold;font-size:36px'>";
    public static final String HEAD_TAG_BEGIN_2 = "<span style='font-weight:bold;font-size:24px'>";
    public static final String HEAD_TAG_BEGIN_3 = "<span style='font-weight:bold;font-size:21px'>";
    public static final String HEAD_TAG_BEGIN_4 = "<span style='font-weight:bold;font-size:18px'>";
    public static final String HEAD_TAG_BEGIN_5 = "<span style='font-weight:bold;font-size:16px'>";
    public static final String HEAD_TAG_BEGIN_6 = "<span style='font-weight:bold;font-size:14px'>";
    public static final String HEAD_TAG_END = "</span>";

    public DiffHandleVO(Builder builder) {
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
         *
         * @param list
         * @return
         */
        public Builder insert(List<String> list) {
            type = Delta.TYPE.INSERT;
            skipLine = list.size();
            strs = new ArrayList<>(list.size());
            for (String str : list) {
                strs.add(tagInsertLine(str));
            }
            return this;
        }

        /**
         * 处理删除行的标记
         *
         * @param list
         * @return
         */
        public Builder delete(List<String> list) {
            type = Delta.TYPE.DELETE;
            strs = new ArrayList<>(list.size());
            for (String str : list) {
                strs.add(tagDeleteLine(str));
            }
            return this;
        }

        /**
         * 处理改变行的标记，只有当增加行与删除行数量相等时，才进行单行的比较
         * 单行比较差异采用myersDiff.buildPath
         *
         * @param delete
         * @param insert
         * @return
         */
        public Builder change(List<String> delete, List<String> insert) {
            if (delete.size() == insert.size() && insert.size() == 1) {
                strs = new ArrayList<>(delete.size());
                type = Delta.TYPE.CHANGE;
                skipLine = delete.size();
                try {
                    //处理改变
                    for (int i = 0; i < delete.size(); i++) {
                        if (delete.get(i).contains(IMG_BEGIN) || insert.get(i).contains(IMG_BEGIN)) {
                            strs.add(tagDeleteLine(delete.get(i)));
                            strs.add(tagInsertLine(insert.get(i)));
                        } else {
                            List<String> ori = char2String(delete.get(i).toCharArray());
                            List<String> rev = char2String(insert.get(i).toCharArray());
                            MyersDiff myersDiff = new MyersDiff<String>();
                            PathNode pathNode = myersDiff.buildPath(ori, rev);
                            strs.add(myersDiff.buildDiff(pathNode, ori, rev));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                strs = new ArrayList<>(delete.size() + insert.size());
                type = Delta.TYPE.CHANGE;
                skipLine = insert.size();
                for (String str : delete) {
                    strs.add(tagDeleteLine(str));
                }
                for (String str : insert) {
                    strs.add(tagInsertLine(str));
                }
            }
            return this;
        }

        public DiffHandleVO build() {
            return new DiffHandleVO(this);
        }

        private List<String> char2String(char[] chars) {
            List<String> strs = new ArrayList<>(chars.length);
            for (char c : chars) {
                strs.add(String.valueOf(c));
            }
            return strs;
        }

        private String tagInsertLine(String line) {
            StringBuilder insert = new StringBuilder();
            insert.append(INSERT_TAG_BEGIN);
            line = line.replaceAll(IMG_BEGIN, IMG_INSERT_TAG_BEGIN + IMG_BEGIN);
            line = line.replaceAll(IMG_END, IMG_END + IMG_TAG_END);
            insert.append(line);
            insert.append(INSERT_TAG_END);
            return insert.toString();
        }

        private String tagDeleteLine(String line) {
            line = line.replaceAll(IMG_BEGIN, IMG_DELETE_TAG_BEGIN + IMG_BEGIN);
            line = line.replaceAll(IMG_END, IMG_END + IMG_TAG_END);
            StringBuilder delete = new StringBuilder();
            delete.append(DELETE_TAG_BEGIN);
            delete.append(line);
            delete.append(DELETE_TAG_END);
            return delete.toString();
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
