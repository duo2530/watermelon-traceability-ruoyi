package com.ruoyi.trace.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.trace.domain.WmQcRecord;

/**
 * 品控统计Mapper
 *
 * @author ruoyi
 */
public interface WmStatsMapper
{
    /** 总览: 批次数/农事数/品控数/平均糖度/扫码数 */
    public Map<String, Object> selectOverview(WmQcRecord query);

    /** 等级分布 */
    public List<Map<String, Object>> selectGradeDistribution(WmQcRecord query);

    /** 中心糖度分布（按整数区间） */
    public List<Map<String, Object>> selectSugarDistribution(WmQcRecord query);

    /** 按月糖度趋势 */
    public List<Map<String, Object>> selectMonthlyTrend(WmQcRecord query);

    /** 按品种汇总 */
    public List<Map<String, Object>> selectVarietySummary(WmQcRecord query);

    /** 按批次汇总 */
    public List<Map<String, Object>> selectBatchSummary(WmQcRecord query);
}
