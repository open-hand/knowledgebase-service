package io.choerodon.kb.api.vo.permission;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.Assert;

import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.algorithm.tree.Child;
import org.hzero.core.algorithm.tree.TreeBuilder;
import org.hzero.core.base.BaseConstants;

public class PermissionTreeCheckVO extends Child<PermissionTreeCheckVO> {

    private PermissionTreeCheckVO(Long id, String targetBaseType, Long parentId, String parentTargetBaseType, List<PermissionCheckVO> permissionCheckInfo) {
        this.id = id == null ? PermissionConstants.EMPTY_ID_PLACEHOLDER : id;
        this.targetBaseType = targetBaseType;
        this.key = new MultiKey<>(this.id, this.targetBaseType);
        this.parentId = parentId == null ? PermissionConstants.EMPTY_ID_PLACEHOLDER : parentId;
        this.parentTargetBaseType = parentTargetBaseType;
        this.parentKey = new MultiKey<>(this.parentId, this.parentTargetBaseType);
        this.permissionCheckInfo = permissionCheckInfo == null ? Collections.emptyList() : permissionCheckInfo;
        this.checkedPermissionPool = new HashMap<>();
    }

    public static PermissionTreeCheckVO of(Long id, String targetBaseType, Long parentId, String parentTargetBaseType, List<PermissionCheckVO> permissionCheckInfo) {
        Assert.hasText(targetBaseType, BaseConstants.ErrorCode.NOT_NULL);
        return new PermissionTreeCheckVO(id, targetBaseType, parentId, parentTargetBaseType, permissionCheckInfo);
    }

    public static List<PermissionTreeCheckVO> treeToList(Collection<PermissionTreeCheckVO> tree) {
        if(CollectionUtils.isEmpty(tree)) {
            return Collections.emptyList();
        }
        List<PermissionTreeCheckVO> result = new ArrayList<>(tree);
        List<PermissionTreeCheckVO> currentSlot = tree.stream()
                .filter(node -> CollectionUtils.isNotEmpty(node.getChildren()))
                .flatMap(node -> node.getChildren().stream())
                .collect(Collectors.toList());
        List<PermissionTreeCheckVO> nextSlot = null;
        while (CollectionUtils.isNotEmpty(currentSlot)) {
            result.addAll(currentSlot);
            nextSlot = currentSlot.stream()
                    .filter(node -> CollectionUtils.isNotEmpty(node.getChildren()))
                    .flatMap(node -> node.getChildren().stream())
                    .collect(Collectors.toList());
            currentSlot = nextSlot;
        }
        return result;
    }

    public static List<PermissionTreeCheckVO> listToTree(Collection<PermissionTreeCheckVO> list, Long rootId, String rootTargetBaseType) {
        if(CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        if(rootId == null) {
            rootId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        Assert.hasText(rootTargetBaseType, BaseConstants.ErrorCode.NOT_NULL);
        // HZERO的TreeBuilder会过滤掉根节点, 所以需要人工补上
        final Long finalRootId = rootId;
        final PermissionTreeCheckVO rootNode = list.stream()
                .filter(node -> Objects.equals(node.getId(), finalRootId) && Objects.equals(node.getTargetBaseType(), rootTargetBaseType))
                .findAny()
                .orElse(null);
        final List<PermissionTreeCheckVO> generatedTree = TreeBuilder.buildTree(new ArrayList<>(list), new MultiKey<>(rootId, rootTargetBaseType), PermissionTreeCheckVO::getKey, PermissionTreeCheckVO::getParentKey);
        if(rootNode == null) {
            return generatedTree;
        } else {
            return Collections.singletonList(rootNode.resetChild(generatedTree));
        }
    }

    public PermissionTreeCheckVO inheritPermissionMap() {
        if(parent == null) {
            return this;
        }
        this.checkedPermissionPool.putAll(parent.checkedPermissionPool);
        return this;
    }

    public List<PermissionCheckVO> checkWithInnerCache() {
        if(CollectionUtils.isEmpty(this.permissionCheckInfo)) {
            return Collections.emptyList();
        }
        List<PermissionCheckVO> unCachedCheckInfo = new ArrayList<>();
        final Iterator<PermissionCheckVO> iterator = permissionCheckInfo.iterator();
        while (iterator.hasNext()) {
            PermissionCheckVO singleCheckInfo = iterator.next();
            final String permissionCode = singleCheckInfo.getPermissionCode();
            final PermissionCheckVO cachedResult = this.checkedPermissionPool.get(permissionCode);
            if(cachedResult != null && Boolean.TRUE.equals(cachedResult.getApprove())) {
                singleCheckInfo.setApprove(cachedResult.getApprove())
                        .setControllerType(cachedResult.getControllerType());
            } else {
                unCachedCheckInfo.add(singleCheckInfo);
                iterator.remove();
            }
        }
        return unCachedCheckInfo;
    }

    public PermissionTreeCheckVO mergePermissionCheckInfo(List<PermissionCheckVO> permissionCheckInfo) {
        if(permissionCheckInfo == null) {
            permissionCheckInfo = Collections.emptyList();
        }
        this.permissionCheckInfo.addAll(permissionCheckInfo);

        this.checkedPermissionPool.clear();
        this.checkedPermissionPool.putAll(Stream.of(new ArrayList<>(this.checkedPermissionPool.values()), permissionCheckInfo)
                .collect(PermissionCheckVO.permissionCombiner)
                .stream()
                .collect(Collectors.toMap(PermissionCheckVO::getPermissionCode, Function.identity()))
        );
        return this;
    }

    private final Long id;
    private final String targetBaseType;
    private final MultiKey<Object> key;
    private final Long parentId;
    private final String parentTargetBaseType;
    private final MultiKey<Object> parentKey;
    private final List<PermissionCheckVO> permissionCheckInfo;
    private PermissionTreeCheckVO parent;
    private final Map<String, PermissionCheckVO> checkedPermissionPool;

    public Long getId() {
        return id;
    }

    public String getTargetBaseType() {
        return targetBaseType;
    }

    public MultiKey<Object> getKey() {
        return key;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentTargetBaseType() {
        return parentTargetBaseType;
    }

    public MultiKey<Object> getParentKey() {
        return parentKey;
    }

    public List<PermissionCheckVO> getPermissionCheckInfo() {
        return permissionCheckInfo;
    }

    public PermissionTreeCheckVO getParent() {
        return parent;
    }

    public PermissionTreeCheckVO setParent(PermissionTreeCheckVO parent) {
        this.parent = parent;
        return this;
    }

    public Map<String, PermissionCheckVO> getCheckedPermissionPool() {
        return checkedPermissionPool;
    }

    public PermissionTreeCheckVO resetChild(List<PermissionTreeCheckVO> children) {
        if(CollectionUtils.isEmpty(children)) {
            children = Collections.emptyList();
        }
        final Field childrenField = FieldUtils.getDeclaredField(this.getClass(), "children", true);
        try{
            childrenField.set(this, null);
        } catch (Throwable thr) {
            // ignore
        }
        this.addChildren(children);
        return this;
    }
}
