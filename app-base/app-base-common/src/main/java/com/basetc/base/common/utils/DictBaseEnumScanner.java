package com.basetc.base.common.utils;

import com.basetc.base.common.annotation.DictType;
import com.basetc.base.common.domain.BaseDict;
import com.basetc.base.common.enums.BaseEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 字典枚举扫描器工具类.
 *
 * <p>用于扫描指定包路径下带有 {@link DictType} 注解的枚举类,
 * 并将其转换为 {@link BaseDict} 对象列表,便于构建字典数据结构.
 *
 * <p>该工具类通过类路径扫描机制查找所有实现 {@link BaseEnum} 接口
 * 且标记了 DictType 注解的枚举,然后提取其字典信息,包括字典键值、描述和选项列表.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 1. 定义字典枚举
 * @DictType(value = "user_status", description = "用户状态")
 * public enum UserStatusEnum implements BaseEnum<Integer> {
 *     ACTIVE(1, "正常"),
 *     INACTIVE(0, "禁用");
 *
 *     private final Integer value;
 *     private final String description;
 *
 *     UserStatusEnum(Integer value, String description) {
 *         this.value = value;
 *         this.description = description;
 *     }
 *
 *     @Override
 *     public Integer getValue() {
 *         return value;
 *     }
 *
 *     @Override
 *     public String getDescription() {
 *         return description;
 *     }
 * }
 *
 * // 2. 在应用启动时扫描字典
 * @PostConstruct
 * public void initDictCache() {
 *     List<BaseDict> dicts = DictBaseEnumScanner.scan("com.example.enums");
 *     // 将字典数据缓存到 Redis 或内存中
 *     redisTemplate.opsForValue().set("dict:all", dicts);
 * }
 *
 * // 3. 提供接口给前端查询
 * @GetMapping("/api/dict/{type}")
 * public R<List<BaseDict.DictData>> getDict(@PathVariable String type) {
 *     List<BaseDict> dicts = dictCache.get();
 *     BaseDict dict = dicts.stream()
 *         .filter(d -> d.getKey().equals(type))
 *         .findFirst()
 *         .orElse(null);
 *     return R.success(dict != null ? dict.getData() : List.of());
 * }
 * }</pre>
 *
 * <h3>性能优化建议:</h3>
 * <ul>
 *   <li>在应用启动时执行扫描,避免每次请求都扫描</li>
 *   <li>将扫描结果缓存到 Redis 或内存中</li>
 *   <li>如果包路径较大,建议分批扫描</li>
 *   <li>使用 @PostConstruct 确保扫描在应用启动后立即执行</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Slf4j
@UtilityClass
public class DictBaseEnumScanner {

    /**
     * 扫描指定包路径下带有 @DictType 注解的枚举类,并转换为 BaseDict 列表.
     *
     * <p>此方法会扫描指定包路径及其子包下的所有类,
     * 找出实现了 {@link BaseEnum} 接口且标记了 {@link DictType} 注解的枚举类,
     * 然后将其转换为字典数据结构.
     *
     * <p><b>性能提示:</b> 此方法会进行类路径扫描,相对耗时.
     * 建议在应用启动时执行一次,然后将结果缓存起来.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 扫描单个包
     * List<BaseDict> dicts = DictBaseEnumScanner.scan("com.example.enums");
     *
     * // 扫描多个包
     * List<BaseDict> dicts1 = DictBaseEnumScanner.scan("com.example.enums.system");
     * List<BaseDict> dicts2 = DictBaseEnumScanner.scan("com.example.enums.business");
     * List<BaseDict> allDicts = new ArrayList<>();
     * allDicts.addAll(dicts1);
     * allDicts.addAll(dicts2);
     *
     * // 在应用启动时扫描并缓存
     * @PostConstruct
     * public void initDictCache() {
     *     List<BaseDict> dicts = DictBaseEnumScanner.scan("com.example.enums");
     *     redisTemplate.opsForValue().set("dict:all", dicts, Duration.ofHours(1));
     * }
     * }</pre>
     *
     * @param basePackage 要扫描的基础包路径,如 "com.example.enums"
     * @return 包含字典信息的 BaseDict 对象列表,如果没有找到则返回空列表
     * @throws IllegalArgumentException 当 basePackage 为 null 时抛出
     */
    public static List<BaseDict> scan(@NonNull String basePackage) {
        // 1. 创建类路径扫描器，参数为false表示不使用默认的Bean定义扫描规则（枚举不是Bean，无需过滤注解）
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // 2. 添加过滤规则：只扫描实现了BaseEnum接口的类
        scanner.addIncludeFilter(new AssignableTypeFilter(BaseEnum.class));
        // 3. 扫描指定包下的所有候选组件（即实现BaseEnum的所有类）
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);

        List<BaseDict> enumDictList = new ArrayList<>();
        for (BeanDefinition beanDefinition : candidateComponents) {
            try {
                // 获取类的全限定名，反射加载类
                String className = beanDefinition.getBeanClassName();
                Class<?> clazz = Class.forName(className);

                // 校验：是否为枚举类型
                if (!clazz.isEnum()) {
                    log.debug("跳过非枚举类型: {}", className);
                    continue;
                }

                // 校验：是否包含 DictType 注解
                if (!clazz.isAnnotationPresent(DictType.class)) {
                    log.warn("枚举类未添加DictType注解：{}", className);
                    continue;
                }

                // 构建字典对象
                BaseDict dict = new BaseDict();
                DictType annotation = clazz.getAnnotation(DictType.class);
                dict.setKey(annotation.value());
                dict.setDescription(annotation.description());

                // 转换枚举为选项列表
                @SuppressWarnings("unchecked")
                List<BaseDict.DictData> options = (List<BaseDict.DictData>) BaseEnum
                        .convertOptions((Class<? extends BaseEnum<?>>) clazz, BaseDict.DictData.class);
                dict.setData(options);

                enumDictList.add(dict);
            } catch (ClassNotFoundException e) {
                log.warn("枚举类不存在：{}", beanDefinition.getBeanClassName(), e);
            } catch (ClassCastException e) {
                log.error("类型转换异常：{}", beanDefinition.getBeanClassName(), e);
            } catch (Exception e) {
                log.error("处理枚举类时发生未知错误：{}", beanDefinition.getBeanClassName(), e);
            }
        }
        return enumDictList;
    }

}
