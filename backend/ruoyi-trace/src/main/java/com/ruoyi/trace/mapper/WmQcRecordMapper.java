package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmQcRecord;

/**
 * 品控记录Mapper接口
 *
 * @author ruoyi
 */
public interface WmQcRecordMapper
{
    public WmQcRecord selectWmQcRecordById(Long qcId);

    public List<WmQcRecord> selectWmQcRecordList(WmQcRecord qcRecord);

    public int insertWmQcRecord(WmQcRecord qcRecord);

    public int updateWmQcRecord(WmQcRecord qcRecord);

    public int deleteWmQcRecordById(Long qcId);

    public int deleteWmQcRecordByIds(Long[] qcIds);

    public WmQcRecord selectWmQcRecordByClientId(String clientId);

    public int countWmQcRecordByBatchIds(Long[] batchIds);
}
