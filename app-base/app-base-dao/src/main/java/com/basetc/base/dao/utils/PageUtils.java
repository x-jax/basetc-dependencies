package com.basetc.base.dao.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basetc.base.dao.domain.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

/**
 * 分页工具类,提供分页相关的便捷操作.
 *
 * <p>此类提供从HTTP请求中获取分页参数、创建分页对象、转换分页结果等功能.
 * 使用Lombok的{@link UtilityClass}注解,所有方法均为静态方法,无需创建实例.</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>自动从HTTP请求中提取分页参数</li>
 *   <li>支持自定义分页参数名称</li>
 *   <li>提供参数默认值和异常处理</li>
 *   <li>支持MyBatis Plus分页对象转换</li>
 *   <li>内置安全参数校验和边界检查</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 方式1: 使用默认参数名
 * IPage<User> page = PageUtils.getPageRequest();
 *
 * // 方式2: 使用自定义参数名
 * IPage<User> page = PageUtils.getPageRequest("page", "limit");
 *
 * // 在Service中使用
 * public PageResult<User> listUsers(User query) {
 *     IPage<User> page = baseMapper.selectPage(PageUtils.getPageRequest(), queryWrapper);
 *     return PageUtils.coverTableData(page);
 * }
 *
 * // 前端请求示例:
 * // GET /users?current=1&size=10
 * // GET /users?page=1&limit=20 (自定义参数名)
 * }</pre>
 *
 * <h3>默认参数名</h3>
 * <ul>
 *   <li>页码: current (默认值: 1)</li>
 *   <li>页大小: size (默认值: 10)</li>
 * </ul>
 *
 * <h3>自定义参数名</h3>
 * <pre>{@code
 * // 全局设置(仅一次)
 * PageUtils.setRequestParams("page", "limit");
 *
 * // 后续使用自定义参数名
 * IPage<User> page = PageUtils.getPageRequest();
 * }</pre>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>在应用启动时统一配置参数名,避免全局状态变更</li>
 *   <li>优先使用getPageRequest(String, String)方法,避免依赖全局配置</li>
 *   <li>结合MyBatis Plus使用,提供完整的分页解决方案</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.baomidou.mybatisplus.core.metadata.IPage
 * @see com.baomidou.mybatisplus.extension.plugins.pagination.Page
 * @see PageResult
 */
@UtilityClass
public class PageUtils {

    /**
     * 默认分页页码参数名.
     */
    private static final String DEFAULT_PAGE_NUM = "current";

    /**
     * 默认分页大小参数名.
     */
    private static final String DEFAULT_PAGE_SIZE = "size";

    /**
     * 当前页码参数名.
     * <p>可通过{@link #setRequestParams(String, String)}方法修改.
     */
    private volatile String pageNumKey = DEFAULT_PAGE_NUM;

    /**
     * 页面大小参数名.
     * <p>可通过{@link #setRequestParams(String, String)}方法修改.
     */
    private volatile String pageSizeKey = DEFAULT_PAGE_SIZE;

    /**
     * 获取分页请求对象,使用默认的页码和页面大小参数名.
     *
     * <p>此方法会从当前HTTP请求中提取分页参数,并创建MyBatis Plus的Page对象.
     * 如果参数不存在或格式错误,使用默认值.
     *
     * <p>默认值:
     * <ul>
     *   <li>页码: 1</li>
     *   <li>页大小: 10</li>
     * </ul>
     *
     * @param <T> 泛型类型参数
     * @return 分页对象,包含页码和页面大小信息
     * @throws IllegalStateException 当无法获取请求上下文时抛出
     * @see #getPageRequest(String, String)
     */
    public <T> IPage<T> getPageRequest() {
        return getPageRequest(pageNumKey, pageSizeKey);
    }

    /**
     * 根据指定的参数名从HTTP请求中获取分页信息并创建分页对象.
     *
     * <p>此方法提供了更灵活的分页参数配置,允许自定义页码和页面大小的参数名.
     * 适用于前端使用不同参数名的场景.
     *
     * <p>参数解析规则:
     * <ul>
     *   <li>如果参数不存在或为空,使用默认值</li>
     *   <li>如果参数格式错误(非数字),使用默认值</li>
     *   <li>页码最小值为1</li>
     *   <li>页面大小最小值为1</li>
     * </ul>
     *
     * @param <T>         泛型类型参数
     * @param pageNumKey  页码参数名,不能为null
     * @param pageSizeKey 页面大小参数名,不能为null
     * @return 分页对象,包含从请求参数中解析出的页码和页面大小信息
     * @throws IllegalArgumentException 如果pageNumKey或pageSizeKey为null
     * @throws IllegalStateException 当无法获取请求上下文时抛出
     */
    public <T> IPage<T> getPageRequest(String pageNumKey, String pageSizeKey) {
        Objects.requireNonNull(pageNumKey, "pageNumKey cannot be null");
        Objects.requireNonNull(pageSizeKey, "pageSizeKey cannot be null");

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException(
                    "无法获取当前请求上下文，请确保在Web环境中调用此方法");
        }

        final HttpServletRequest request = attributes.getRequest();
        final String numStr = request.getParameter(pageNumKey);
        final String sizeStr = request.getParameter(pageSizeKey);

        int pageNum = parsePageNum(numStr);
        int pageSize = parsePageSize(sizeStr);

        return new Page<>(pageNum, pageSize);
    }

    /**
     * 解析页码参数.
     *
     * @param numStr 页码字符串
     * @return 页码,最小值为1
     */
    private int parsePageNum(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return 1;
        }

        try {
            int pageNum = Integer.parseInt(numStr.trim());
            return Math.max(pageNum, 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * 解析页面大小参数.
     *
     * @param sizeStr 页面大小字符串
     * @return 页面大小,最小值为1
     */
    private int parsePageSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 10;
        }

        try {
            int pageSize = Integer.parseInt(sizeStr.trim());
            return Math.max(pageSize, 1);
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    /**
     * 将MyBatis Plus的分页对象转换为分页结果对象.
     *
     * <p>此方法将MyBatis Plus的{@link IPage}对象转换为项目统一的
     * {@link PageResult}对象,便于前端使用和统一处理.
     *
     * <p>转换映射:
     * <ul>
     *   <li>IPage.records → PageResult.rows (数据列表)</li>
     *   <li>IPage.total → PageResult.total (总记录数)</li>
     *   <li>IPage.pages → PageResult.totalPage (总页数)</li>
     *   <li>IPage.current → PageResult.current (当前页码)</li>
     *   <li>IPage.size → PageResult.size (每页大小)</li>
     * </ul>
     *
     * @param <T>  泛型类型参数
     * @param page MyBatis Plus的分页对象,不能为null
     * @return 分页结果对象,包含分页数据、总记录数、总页数等信息
     * @throws IllegalArgumentException 如果page为null
     */
    public <T> PageResult<T> coverTableData(IPage<T> page) {
        Objects.requireNonNull(page, "page cannot be null");

        final List<T> records = page.getRecords();
        final PageResult<T> tableData = new PageResult<>();
        tableData.setRows(records);
        tableData.setTotal(page.getTotal());
        tableData.setTotalPage(page.getPages());
        tableData.setCurrent(page.getCurrent());
        tableData.setSize(page.getSize());
        return tableData;
    }

    /**
     * 设置全局分页参数名称.
     *
     * <p>此方法用于全局修改分页参数的名称,设置后所有通过
     * {@link #getPageRequest()}方法调用都会使用新的参数名.
     *
     * <p><b>注意:</b> 此方法只会执行一次,后续调用会被忽略.
     * 建议在应用启动时调用,例如在Spring Boot的配置类中.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * @Configuration
     * public class PageConfig {
     *     @PostConstruct
     *     public void init() {
     *         // 统一使用page和limit作为参数名
     *         PageUtils.setRequestParams("page", "limit");
     *     }
     * }
     * }</pre>
     *
     * @param pageNoName   页码参数名,不能为null或空
     * @param pageSizeName 页面大小参数名,不能为null或空
     * @throws IllegalArgumentException 如果参数为null或空
     * @deprecated 建议直接使用{@link #getPageRequest(String, String)}方法,
     *             避免使用全局可变状态
     */
    @Deprecated
    public void setRequestParams(String pageNoName, String pageSizeName) {
        if (pageNoName == null || pageNoName.trim().isEmpty()) {
            throw new IllegalArgumentException("pageNoName cannot be null or empty");
        }
        if (pageSizeName == null || pageSizeName.trim().isEmpty()) {
            throw new IllegalArgumentException("pageSizeName cannot be null or empty");
        }

        // 仅设置一次,避免被覆盖
        if (DEFAULT_PAGE_NUM.equals(pageNumKey) && DEFAULT_PAGE_SIZE.equals(pageSizeKey)) {
            pageNumKey = pageNoName;
            pageSizeKey = pageSizeName;
        }
    }

    /**
     * 重置分页参数名称为默认值.
     *
     * <p>将页码参数名重置为"current",页面大小参数名重置为"size".
     */
    public void resetRequestParams() {
        pageNumKey = DEFAULT_PAGE_NUM;
        pageSizeKey = DEFAULT_PAGE_SIZE;
    }

    /**
     * 获取当前页码参数名.
     *
     * @return 页码参数名
     */
    public String getPageNumKey() {
        return pageNumKey;
    }

    /**
     * 获取当前页面大小参数名.
     *
     * @return 页面大小参数名
     */
    public String getPageSizeKey() {
        return pageSizeKey;
    }
}

