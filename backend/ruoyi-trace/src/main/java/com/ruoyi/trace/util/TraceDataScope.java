package com.ruoyi.trace.util;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 溯源数据范围: 管理员或拥有 trace:data:all 权限的用户可见全部数据，农户只能看到/修改自己的数据
 *
 * @author ruoyi
 */
public class TraceDataScope
{
    public static final String PERMI_ALL = "trace:data:all";

    /**
     * 当前用户需要限定的农户ID；返回 null 表示不限定
     */
    public static Long farmerScope()
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId) || SecurityUtils.hasPermi(PERMI_ALL))
        {
            return null;
        }
        return userId;
    }

    /**
     * 校验数据归属
     */
    public static void checkOwner(Long farmerId)
    {
        Long scope = farmerScope();
        if (scope != null && !scope.equals(farmerId))
        {
            throw new ServiceException("没有权限访问该数据");
        }
    }
}
