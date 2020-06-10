/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package highest.flow.taobaolive.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.xss.SQLFilter;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 查询参数
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Query<T> {

	/**
	 * 当前页码
	 */
	public static final String PAGE = "page";
	/**
	 * 每页显示记录数
	 */
	public static final String LIMIT = "limit";
	/**
	 * 排序字段
	 */
	public static final String ORDER_FIELD = "sidx";
	/**
	 * 排序方式
	 */
	public static final String ORDER = "order";
	/**
	 * 升序
	 */
	public static final String ASC = "asc";

	public IPage<T> getPage(Map<String, Object> params) {
		return this.getPage(params, null, false);
	}

	public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
		// 分页参数
		long curPage = 1;
		long limit = 10;

		if (params.get(PAGE) != null) {
			curPage = Long.parseLong((String) params.get(PAGE));
		}
		if (params.get(LIMIT) != null) {
			limit = Long.parseLong((String) params.get(LIMIT));
		}

		// 分页对象
		Page<T> page = new Page<>(curPage, limit);

		// 分页参数
		params.put(PAGE, page);

		// 排序字段
		// 防止SQL注入（因为sidx、order是通过拼接SQL实现排序的，会有SQL注入风险）
		String orderField = SQLFilter.sqlInject((String) params.get(ORDER_FIELD));
		String order = (String) params.get(ORDER);

		// 前端字段排序
		if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
			if (ASC.equalsIgnoreCase(order)) {
				return page.setAsc(orderField);
			} else {
				return page.setDesc(orderField);
			}
		}

		// 默认排序
		if (isAsc) {
			page.setAsc(defaultOrderField);
		} else {
			page.setDesc(defaultOrderField);
		}

		return page;
	}
}
