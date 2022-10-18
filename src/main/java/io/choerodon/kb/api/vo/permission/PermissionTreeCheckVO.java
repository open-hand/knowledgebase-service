package io.choerodon.kb.api.vo.permission;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.algorithm.tree.Child;
import org.hzero.core.algorithm.tree.TreeBuilder;
import org.hzero.core.base.BaseConstants;

/**
 * 知识库对象树鉴权专用VO
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public class PermissionTreeCheckVO extends Child<PermissionTreeCheckVO> {

    /**
     * 私有化构造方法
     */
    private PermissionTreeCheckVO(
            Long id, String targetBaseType, Long parentId, String parentTargetBaseType, List<PermissionCheckVO> permissionCheckInfo
    ) {
        this.id = id == null ? PermissionConstants.EMPTY_ID_PLACEHOLDER : id;
        this.targetBaseType = targetBaseType;
        this.key = new MultiKey<>(this.id, this.targetBaseType);
        this.parentId = parentId == null ? PermissionConstants.EMPTY_ID_PLACEHOLDER : parentId;
        this.parentTargetBaseType = parentTargetBaseType;
        this.parentKey = new MultiKey<>(this.parentId, this.parentTargetBaseType);
        this.permissionCheckInfo = permissionCheckInfo == null ? Collections.emptyList() : permissionCheckInfo;
        this.checkedPermissionPool = new HashMap<>();
    }

    /**
     * 快速创建
     * @param id                    对象ID
     * @param targetBaseType        对象控制基础类型
     * @param parentId              父级对象ID, 可空
     * @param parentTargetBaseType  父级对象基础类型, 可空
     * @param permissionCheckInfo   待鉴定权限
     * @return 快速创建
     */
    public static PermissionTreeCheckVO of(
            @NonNull Long id,
            @NonNull String targetBaseType,
            Long parentId,
            String parentTargetBaseType,
            List<PermissionCheckVO> permissionCheckInfo
    ) {
        Assert.hasText(targetBaseType, BaseConstants.ErrorCode.NOT_NULL);
        return new PermissionTreeCheckVO(id, targetBaseType, parentId, parentTargetBaseType, permissionCheckInfo);
    }

    /**
     * 树结构转化为列表结构
     * @param tree 树结构的集合
     * @return  列表结构的集合
     */
    public static List<PermissionTreeCheckVO> treeToList(Collection<PermissionTreeCheckVO> tree) {
        if(CollectionUtils.isEmpty(tree)) {
            return Collections.emptyList();
        }
        List<PermissionTreeCheckVO> result = new ArrayList<>(tree);
        List<PermissionTreeCheckVO> currentSlot = tree.stream()
                .filter(node -> CollectionUtils.isNotEmpty(node.getChildren()))
                .flatMap(node -> node.getChildren().stream())
                .collect(Collectors.toList());
        List<PermissionTreeCheckVO> nextSlot;
        // 通过两个工作空间交替使用的方法实现BFS遍历
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

    /**
     * 列表结构转化为树结构
     * @param list                  列表结构的集合
     * @param rootId                根节点ID
     * @param rootTargetBaseType    根节点对象控制基础类型
     * @return                      树结构
     */
    public static List<PermissionTreeCheckVO> listToTree(
            Collection<PermissionTreeCheckVO> list,
            Long rootId,
            @NonNull String rootTargetBaseType
    ) {
        if(CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 先过滤出根节点
        final Long finalRootId = rootId;
        final PermissionTreeCheckVO rootNode = list.stream()
                .filter(node -> Objects.equals(node.getId(), finalRootId) && Objects.equals(node.getTargetBaseType(), rootTargetBaseType))
                .findAny()
                .orElse(null);
        // 使用TreeBuilder构建树
        final List<PermissionTreeCheckVO> generatedTree = TreeBuilder.buildTree(new ArrayList<>(list), new MultiKey<>(rootId, rootTargetBaseType), PermissionTreeCheckVO::getKey, PermissionTreeCheckVO::getParentKey);
        if(rootNode == null) {
            return generatedTree;
        } else {
            // HZERO的TreeBuilder会过滤掉根节点, 所以需要人工补上
            return Collections.singletonList(rootNode.resetChild(generatedTree));
        }
    }

    /**
     * 继承上级已有权限
     * @return this
     */
    public PermissionTreeCheckVO inheritPermissionMap() {
        if(parent == null) {
            return this;
        }
        this.checkedPermissionPool.clear();
        final Set<String> securityConfigActionCodes = Arrays.stream(PermissionConstants.SecurityConfigAction.values())
                .map(Object::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        parent.checkedPermissionPool.forEach((key, value) -> {
            // 安全设置里的操作权限不能继承
            if(securityConfigActionCodes.stream().noneMatch(key::endsWith)) {
                this.checkedPermissionPool.put(key, value);
            }
        });
        return this;
    }

    /**
     * 通过内部继承的权限进行快速鉴权
     * @return 没有匹配上内部继承权限的权限, 需要单独处理进行鉴权
     */
    public List<PermissionCheckVO> checkWithInnerCache() {
        // 如果自身没有任何待鉴定权限, 则返回空集合
        if(CollectionUtils.isEmpty(this.permissionCheckInfo)) {
            return Collections.emptyList();
        }
        List<PermissionCheckVO> unCachedCheckInfo = new ArrayList<>();
        final Iterator<PermissionCheckVO> iterator = permissionCheckInfo.iterator();
        // 遍历自身的待鉴定权限
        while (iterator.hasNext()) {
            // 尝试匹配继承来的权限
            PermissionCheckVO singleCheckInfo = iterator.next();
            final String permissionCode = singleCheckInfo.getPermissionCode();
            final PermissionCheckVO cachedResult = this.checkedPermissionPool.get(permissionCode);
            if(cachedResult != null && Boolean.TRUE.equals(cachedResult.getApprove())) {
                // 如果匹配上了, 且是有权限的, 则直接给当前权限赋值
                singleCheckInfo.setApprove(cachedResult.getApprove())
                        .setControllerType(cachedResult.getControllerType());
            } else {
                // 否则, 记为待鉴权, 且从当前节点的权限信息中移除
                unCachedCheckInfo.add(singleCheckInfo);
                iterator.remove();
            }
        }
        return unCachedCheckInfo;
    }

    /**
     * 合并部分鉴权结果到this
     * @param permissionCheckInfo   部分鉴权结果
     * @return                      this
     */
    public PermissionTreeCheckVO mergePermissionCheckInfo(List<PermissionCheckVO> permissionCheckInfo) {
        if(permissionCheckInfo == null) {
            permissionCheckInfo = Collections.emptyList();
        }
        // 合并当前对象的鉴权结果
        final List<PermissionCheckVO> newPermissionCheckInfo = Stream.of(this.permissionCheckInfo, permissionCheckInfo).collect(PermissionCheckVO.permissionCombiner);
        this.permissionCheckInfo.clear();
        this.permissionCheckInfo.addAll(newPermissionCheckInfo);
        // 合并继承权限池, 为下级内部鉴权做铺垫
        this.checkedPermissionPool.clear();
        this.checkedPermissionPool.putAll(Stream.of(new ArrayList<>(this.checkedPermissionPool.values()), this.permissionCheckInfo)
                .collect(PermissionCheckVO.permissionCombiner)
                .stream()
                .collect(Collectors.toMap(PermissionCheckVO::getPermissionCode, Function.identity()))
        );
        return this;
    }

    /**
     * 对象ID
     */
    private final Long id;
    /**
     * 对象控制基础类型
     */
    private final String targetBaseType;
    /**
     * 唯一键, Pair&lt;id, targetBaseType&gt;
     */
    private final MultiKey<Object> key;
    /**
     * 级对象ID
     */
    private final Long parentId;
    /**
     * 父级对象基础类型
     */
    private final String parentTargetBaseType;
    /**
     * 父级唯一键, Pair&lt;parentId, parentTargetBaseType&gt;
     */
    private final MultiKey<Object> parentKey;
    /**
     * 待鉴定权限
     */
    private final List<PermissionCheckVO> permissionCheckInfo;
    /**
     * 上级对象节点
     */
    private PermissionTreeCheckVO parent;
    /**
     * 继承权限池
     */
    private final Map<String, PermissionCheckVO> checkedPermissionPool;

    /**
     * @return 对象ID
     */
    public Long getId() {
        return id;
    }
    /**
     * @return 对象控制基础类型
     */
    public String getTargetBaseType() {
        return targetBaseType;
    }
    /**
     * @return 唯一键, Pair&lt;id, targetBaseType&gt;
     */
    public MultiKey<Object> getKey() {
        return key;
    }
    /**
     * @return 级对象ID
     */
    public Long getParentId() {
        return parentId;
    }
    /**
     * @return 父级对象基础类型
     */
    public String getParentTargetBaseType() {
        return parentTargetBaseType;
    }
    /**
     * @return 父级唯一键, Pair&lt;parentId, parentTargetBaseType&gt;
     */
    public MultiKey<Object> getParentKey() {
        return parentKey;
    }
    /**
     * @return 待鉴定权限
     */
    public List<PermissionCheckVO> getPermissionCheckInfo() {
        return permissionCheckInfo;
    }
    /**
     * @return 上级对象节点
     */
    public PermissionTreeCheckVO getParent() {
        return parent;
    }

    public PermissionTreeCheckVO setParent(PermissionTreeCheckVO parent) {
        this.parent = parent;
        return this;
    }
    /**
     * @return 继承权限池
     */
    public Map<String, PermissionCheckVO> getCheckedPermissionPool() {
        return checkedPermissionPool;
    }

    /**
     * 重置children, 由于HZERO没有开放children属性, 这里是用反射强制处理的, 谨慎使用
     * @param children children
     * @return this
     */
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
