package com.ruoyi.trace.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.trace.service.IWmTraceService;

/**
 * 消费者扫码溯源（匿名访问）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/trace/public")
public class WmPublicController extends BaseController
{
    @Autowired
    private IWmTraceService traceService;

    /**
     * 按批次码查询种植全过程
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{batchCode}")
    public AjaxResult trace(@PathVariable("batchCode") String batchCode)
    {
        if (batchCode == null || !batchCode.matches("^[A-Za-z0-9]{4,32}$"))
        {
            return error("溯源码格式不正确");
        }
        Map<String, Object> data = traceService.publicTrace(batchCode.toUpperCase());
        if (data == null)
        {
            return error("未查询到该溯源码，请确认是否为正品");
        }
        return success(data);
    }
}
