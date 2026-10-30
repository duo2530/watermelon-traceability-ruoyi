package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmSyncLog;

/**
 * 同步日志Mapper接口
 *
 * @author ruoyi
 */
public interface WmSyncLogMapper
{
    public WmSyncLog selectWmSyncLogById(Long logId);

    public List<WmSyncLog> selectWmSyncLogList(WmSyncLog syncLog);

    public int insertWmSyncLog(WmSyncLog syncLog);

    public int updateWmSyncLog(WmSyncLog syncLog);

    public int deleteWmSyncLogById(Long logId);

    public int deleteWmSyncLogByIds(Long[] logIds);
}
