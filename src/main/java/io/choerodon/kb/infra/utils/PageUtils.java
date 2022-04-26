package io.choerodon.kb.infra.utils;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 根据page, size参数获取数据库start的行
 */
public class PageUtils {

    private PageUtils() {
    }

    public static int getBegin(int page, int size) {
        page = page <= 1 ? 1 : page;
        return (page - 1) * size;
    }

    /**
     * 装配Page对象
     *
     * @param list         包含所有内容的列表
     * @param pageRequest 分页参数
     * @return Page
     */
    public static <T> Page<T> createPageFromList(List<T> list, PageRequest pageRequest) {
        int total = list.size();
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        PageInfo pageInfo = new PageInfo(page, size);
        boolean selectAll = (page == 0 || size == 0);
        List<T> result;
        if (selectAll) {
            result = list;
        } else {
            int start = PageUtils.getBegin(page, size);
            int end = page * size > total ? total : page * size;
            result = list.subList(start, end);
        }
        return new Page<>(result, pageInfo, total);
    }
}
