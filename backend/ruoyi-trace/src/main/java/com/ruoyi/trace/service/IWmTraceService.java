package com.ruoyi.trace.service;

import java.util.Map;

/**
 * 消费者溯源查询 / 品控统计
 *
 * @author ruoyi
 */
public interface IWmTraceService
{
    /**
     * 按批次码组装公开溯源信息：批次、品牌、地块、农事时间线、品控汇总
     *
     * @return null 表示批次不存在
     */
    public Map<String, Object> publicTrace(String batchCode);

    /**
     * 品控统计
     *
     * @param farmerId 限定农户，null 不限
     */
    public Map<String, Object> stats(Long farmerId, String variety, String beginTime, String endTime);
}
