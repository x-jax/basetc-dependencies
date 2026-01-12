package com.basetc.base.dao.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类.
 *
 * <p>此类用于封装分页查询的结果数据,包含数据列表、总记录数、总页数等分页信息.
 * 配合MyBatis Plus的{@link com.baomidou.mybatisplus.core.metadata.IPage}使用,
 * 通过{@link com.basetc.base.dao.utils.PageUtils#coverTableData}方法转换得到.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 在Service中查询分页数据
 * public PageResult<User> listUsers(User query) {
 *     return pageList(query);
 * }
 *
 * // 在Controller中返回分页数据
 * @GetMapping("/users")
 * public R<PageResult<User>> listUsers(User query) {
 *     PageResult<User> pageResult = userService.listUsers(query);
 *     return R.success(pageResult);
 * }
 * }</pre>
 *
 * <h3>前端数据格式:</h3>
 * <pre>{@code
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": {
 *     "rows": [
 *       { "id": 1, "name": "张三" },
 *       { "id": 2, "name": "李四" }
 *     ],
 *     "total": 100,
 *     "totalPage": 10,
 *     "current": 1,
 *     "size": 10
 *   }
 * }
 * }</pre>
 *
 * @param <T> 数据记录类型
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.basetc.base.dao.utils.PageUtils#coverTableData(com.baomidou.mybatisplus.core.metadata.IPage)
 */
@Data
@NoArgsConstructor
@FieldNameConstants
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页数据列表.
     * <p>当前页的数据记录集合,可能为空列表但不会为null.
     */
    private List<T> rows;

    /**
     * 总记录数.
     * <p>符合条件的所有记录总数(不受分页影响).
     */
    private Long total;

    /**
     * 总页数.
     * <p>根据总记录数和每页大小计算得出的总页数.
     */
    private Long totalPage;

    /**
     * 当前页码.
     * <p>当前页的页码,从1开始.
     */
    private Long current;

    /**
     * 每页显示数量.
     * <p>每页最多显示的记录数.
     */
    private Long size;

    /**
     * 构造函数.
     *
     * @param rows      数据列表
     * @param total     总记录数
     * @param totalPage 总页数
     * @param current   当前页码
     * @param size      每页大小
     */
    public PageResult(List<T> rows, Long total, Long totalPage, Long current, Long size) {
        this.rows = rows;
        this.total = total;
        this.totalPage = totalPage;
        this.current = current;
        this.size = size;
    }

    /**
     * 判断是否有数据.
     *
     * @return true=有数据 false=无数据
     */
    public boolean hasData() {
        return rows != null && !rows.isEmpty();
    }

    /**
     * 判断是否是第一页.
     *
     * @return true=第一页 false=非第一页
     */
    public boolean isFirstPage() {
        return current != null && current == 1;
    }

    /**
     * 判断是否是最后一页.
     *
     * @return true=最后一页 false=非最后一页
     */
    public boolean isLastPage() {
        return current != null && totalPage != null && current.equals(totalPage);
    }

    /**
     * 获取下一页页码.
     *
     * @return 下一页页码,如果没有下一页则返回当前页码
     */
    public long getNextPage() {
        if (current == null || totalPage == null || current >= totalPage) {
            return current != null ? current : 1;
        }
        return current + 1;
    }

    /**
     * 获取上一页页码.
     *
     * @return 上一页页码,如果没有上一页则返回当前页码
     */
    public long getPrevPage() {
        if (current == null || current <= 1) {
            return 1;
        }
        return current - 1;
    }
}