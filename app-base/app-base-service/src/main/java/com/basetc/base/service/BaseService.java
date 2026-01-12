package com.basetc.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basetc.base.dao.domain.PageResult;
import com.basetc.base.dao.utils.PageUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Objects;

/**
 * 基础服务类,提供通用的CRUD操作和分页查询功能.
 *
 * <p>
 * 此类继承自MyBatis Plus的{@link ServiceImpl},提供了一套标准的业务服务层方法.
 * 所有的Service层可以继承此类,获得常用的数据库操作能力,减少重复代码.
 * </p>
 *
 * <h3>核心功能</h3>
 * <ul>
 * <li>分页查询: 支持实体对象和Wrapper两种方式的分页查询</li>
 * <li>条件查询: 子类可重写buildQueryWrapper方法自定义查询逻辑</li>
 * <li>异常操作: 提供带异常处理的查询、更新、删除方法</li>
 * <li>通用操作: 基于MyBatis Plus的丰富操作方法</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService extends BaseService<UserMapper, User> {
 *
 *         @Override
 *         protected Wrapper<User> buildQueryWrapper(User entity) {
 *             return Wrappers.lambdaQuery(entity)
 *                     .like(User::getName, entity.getName())
 *                     .eq(User::getStatus, entity.getStatus());
 *         }
 *
 *         public User getUserOrThrow(Long id) {
 *             return getByIdOrThrow(id, () -> {
 *                 throw new BasetcException("用户不存在");
 *             });
 *         }
 *     }
 * }
 * </pre>
 *
 * <h3>设计优势</h3>
 * <ul>
 * <li>代码复用: 提供通用CRUD操作,减少重复代码</li>
 * <li>灵活扩展: 支持自定义查询条件和业务逻辑</li>
 * <li>异常处理: 提供优雅的异常处理机制</li>
 * <li>分页支持: 内置分页查询功能,简化分页操作</li>
 * </ul>
 *
 * @param <M> Mapper接口类型,必须继承{@link BaseMapper}
 * @param <T> 实体类型
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see ServiceImpl
 * @see BaseMapper
 * @see PageResult
 */
public class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 根据实体进行分页查询(使用实体中的非空属性作为条件).
     *
     * <p>
     * 此方法会调用{@link #buildQueryWrapper(Object)}方法构建查询条件,
     * 子类可以重写该方法来自定义查询逻辑.
     *
     * <p>
     * 分页参数从HTTP请求中获取,默认参数名为:
     * <ul>
     * <li>页码: current (默认值: 1)</li>
     * <li>页大小: size (默认值: 10)</li>
     * </ul>
     *
     * @param entity 查询条件实体,非null属性会作为查询条件
     * @return 分页结果,包含数据列表和分页信息
     * @see #buildQueryWrapper(Object)
     * @see PageUtils#getPageRequest()
     */
    public PageResult<T> pageList(T entity) {
        // 使用Wrapper构建查询条件
        Wrapper<T> queryWrapper = buildQueryWrapper(entity);
        IPage<T> tiPage = baseMapper.selectPage(PageUtils.getPageRequest(), queryWrapper);
        return PageUtils.coverTableData(tiPage);
    }

    /**
     * 根据查询条件进行分页查询.
     *
     * <p>
     * 此方法允许直接传入自定义的Wrapper查询条件,
     * 适用于复杂的查询场景.
     *
     * @param queryWrapper 查询条件包装器,不能为null
     * @return 分页结果,包含数据列表和分页信息
     * @throws IllegalArgumentException 如果queryWrapper为null
     */
    public PageResult<T> pageList(Wrapper<T> queryWrapper) {
        if (queryWrapper == null) {
            throw new IllegalArgumentException("queryWrapper cannot be null");
        }
        IPage<T> tiPage = baseMapper.selectPage(PageUtils.getPageRequest(), queryWrapper);
        return PageUtils.coverTableData(tiPage);
    }

    /**
     * 构建查询条件包装器(根据实体非空属性).
     *
     * <p>
     * 默认实现返回null，表示查询所有数据。子类可以重写此方法来自定义查询逻辑，
     * 建议使用MyBatis Plus的{@link Wrappers#lambdaQuery(Object)}方法构建条件。
     *
     * <p>
     * 示例：
     * <ul>
     * <li>如果实体的name属性不为null，则会生成 `name = #{name}` 条件</li>
     * <li>如果实体的age属性不为null，则会生成 `age = #{age}` 条件</li>
     * <li>多个非空属性将生成 `AND` 连接的条件</li>
     * </ul>
     *
     * <p>
     * 重写示例：
     * 
     * <pre>{@code
     * @Override
     * protected Wrapper<User> buildQueryWrapper(User entity) {
     *     // 调用父类方法获取基础条件
     *     Wrapper<User> baseWrapper = super.buildQueryWrapper(entity);
     * 
     *     // 添加自定义条件
     *     return ((LambdaQueryWrapper<User>) baseWrapper)
     *             .like(StringUtils.isNotBlank(entity.getName()), User::getName, entity.getName())
     *             .ge(Objects.nonNull(entity.getAge()), User::getAge, entity.getAge());
     * }
     * }</pre>
     *
     * @param entity 查询条件实体
     * @return 查询条件包装器，使用实体非空属性作为条件
     * @see com.baomidou.mybatisplus.core.toolkit.Wrappers
     */
    protected Wrapper<T> buildQueryWrapper(T entity) {
        // 默认实现：使用实体的非空属性作为查询条件
        return Wrappers.lambdaQuery(entity);
    }

    /**
     * 根据ID查询,如不存在则执行异常处理.
     *
     * <p>
     * 此方法提供了便捷的数据存在性检查和异常处理机制,
     * 避免了繁琐的null判断和异常抛出代码.
     *
     * <h3>使用示例:</h3>
     * 
     * <pre>{@code
     * // 方式1: 使用Lambda表达式
     * User user = getByIdOrThrow(userId, () -> {
     *     throw new BasetcException(404, "用户不存在");
     * });
     *
     * // 方式2: 使用方法引用
     * User user = getByIdOrThrow(userId, this::throwUserNotFoundException);
     *
     * private void throwUserNotFoundException() {
     *     throw new BasetcException(404, "用户不存在");
     * }
     * }</pre>
     *
     * @param id 主键ID,不能为null
     * @param ex 不存在时的异常处理函数,不能为null
     * @return 实体对象
     * @throws IllegalArgumentException 如果id或ex为null
     * @see ExThrow
     */
    public T getByIdOrThrow(Serializable id, ExThrow ex) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(ex, "ex cannot be null");

        T entity = super.getById(id);
        if (entity == null) {
            ex.throwException();
        }
        return entity;
    }

    /**
     * 根据ID更新,如不存在则执行异常处理.
     *
     * <p>
     * 此方法提供了便捷的更新结果检查和异常处理机制.
     * 更新失败(记录不存在)时会执行传入的异常处理函数.
     *
     * <p>
     * 方法标注了{@link Transactional}注解,确保操作的原子性.
     *
     * @param entity 实体对象,必须包含主键ID,不能为null
     * @param ex     不存在时的异常处理函数,不能为null
     * @throws IllegalArgumentException 如果entity或ex为null
     * @see ExThrow
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateByIdOrThrow(T entity, ExThrow ex) {
        Objects.requireNonNull(entity, "entity cannot be null");
        Objects.requireNonNull(ex, "ex cannot be null");

        if (!super.updateById(entity)) {
            ex.throwException();
        }
    }

    /**
     * 根据ID删除,如不存在则执行异常处理.
     *
     * <p>
     * 此方法提供了便捷的删除结果检查和异常处理机制.
     * 删除失败(记录不存在)时会执行传入的异常处理函数.
     *
     * <p>
     * 方法标注了{@link Transactional}注解,确保操作的原子性.
     *
     * @param id 主键ID,不能为null
     * @param ex 不存在时的异常处理函数,不能为null
     * @throws IllegalArgumentException 如果id或ex为null
     * @see ExThrow
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdOrThrow(Serializable id, ExThrow ex) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(ex, "ex cannot be null");

        if (!super.removeById(id)) {
            ex.throwException();
        }
    }

    /**
     * 异常抛出函数式接口.
     *
     * <p>
     * 此接口用于定义"数据不存在时"的异常处理逻辑,
     * 配合{@code OrThrow}系列方法使用,提供灵活的异常处理机制.
     *
     * <h3>实现示例:</h3>
     * 
     * <pre>{@code
     * // Lambda表达式实现
     * ExThrow exThrow = () -> {
     *     throw new BasetcException("数据不存在");
     * };
     *
     * // 方法引用实现
     * ExThrow exThrow = MyService::throwNotFoundException;
     * }</pre>
     *
     * @author Liu,Dongdong
     * @since 1.0.0
     * @see #getByIdOrThrow(Serializable, ExThrow)
     * @see #updateByIdOrThrow(Object, ExThrow)
     * @see #removeByIdOrThrow(Serializable, ExThrow)
     */
    @FunctionalInterface
    public interface ExThrow {
        /**
         * 抛出异常.
         *
         * <p>
         * 当数据不存在或其他异常情况时,实现此方法来抛出业务异常.
         *
         * @throws RuntimeException 业务异常
         */
        void throwException();
    }
}