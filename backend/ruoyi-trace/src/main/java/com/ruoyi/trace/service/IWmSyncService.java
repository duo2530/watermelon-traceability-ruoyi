package com.ruoyi.trace.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.trace.domain.WmSyncItem;

/**
 * 离线同步Service
 *
 * @author ruoyi
 */
public interface IWmSyncService
{
    /**
     * 批量同步，按 clientId 幂等
     *
     * @return 每条结果 {clientId, result: accepted|duplicate|rejected, bizId, message}
     */
    public List<Map<String, Object>> syncBatch(List<WmSyncItem> items, String username);
}
