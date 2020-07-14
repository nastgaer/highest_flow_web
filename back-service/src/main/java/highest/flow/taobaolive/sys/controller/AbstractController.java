/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.sys.entity.SysMember;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller公共组件
 *
 * @author Mark sunlightcs@gmail.com
 */
public abstract class AbstractController {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected SysMember getUser() {
		return (SysMember) SecurityUtils.getSubject().getPrincipal();
	}

	protected int getUserId() {
		return getUser().getId();
	}

	protected boolean isAdmin() {
		return getUser().getLevel() == 999;
	}
}
