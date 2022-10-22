package io.choerodon.kb.infra.permission.Voter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

/**
 * 常用归票规则
 * @author gaokuo.dai@zknow.com 2022-10-19
 */
public final class TicketCollectionRules {

    /**
     * 归票规则--任一同意
     */
    public static final Collector<List<PermissionCheckVO>, List<PermissionCheckVO>, List<PermissionCheckVO>> ANY_AGREE = Collector.of(
            ArrayList::new,
            PermissionCheckVO::permissionPositiveAccumulator,
            PermissionCheckVO::permissionPositiveAccumulator,
            Function.identity(),
            Collector.Characteristics.IDENTITY_FINISH,
            Collector.Characteristics.UNORDERED,
            Collector.Characteristics.CONCURRENT
    );

    /**
     * 归票规则--一票否決
     */
    public static final Collector<List<PermissionCheckVO>, List<PermissionCheckVO>, List<PermissionCheckVO>> ONE_VETO = Collector.of(
            ArrayList::new,
            PermissionCheckVO::permissionNegativeAccumulator,
            PermissionCheckVO::permissionNegativeAccumulator,
            Function.identity(),
            Collector.Characteristics.IDENTITY_FINISH,
            Collector.Characteristics.UNORDERED,
            Collector.Characteristics.CONCURRENT
    );

}
