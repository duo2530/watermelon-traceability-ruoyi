package com.ruoyi.trace.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.trace.domain.WmSyncItem;
import com.ruoyi.trace.domain.WmSyncLog;
import com.ruoyi.trace.mapper.WmSyncLogMapper;
import com.ruoyi.trace.service.IWmSyncService;

/**
 * 小程序离线队列同步
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/sync")
public class WmSyncController extends BaseController
{
    @Autowired
    private IWmSyncService syncService;

    @Autowired
    private WmSyncLogMapper syncLogMapper;

    /**
     * 批量提交离线数据，按 clientId 幂等；返回每条的处理结果
     */
    @PreAuthorize("@ss.hasPermi('trace:sync:submit')")
    @PostMapping("/batch")
    public AjaxResult batch(@RequestBody List<WmSyncItem> items)
    {
        List<Map<String, Object>> results = syncService.syncBatch(items, getUsername());
        return success(results);
    }

    /**
     * 当前用户的同步日志
     */
    @PreAuthorize("@ss.hasPermi('trace:sync:submit')")
    @GetMapping("/log")
    public TableDataInfo log(WmSyncLog query)
    {
        query.setCreateBy(getUsername());
        startPage();
        return getDataTable(syncLogMapper.selectWmSyncLogList(query));
    }
}
