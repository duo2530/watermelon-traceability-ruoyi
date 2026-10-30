package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmFarmRecord;

/**
 * 农事记录Mapper接口
 *
 * @author ruoyi
 */
public interface WmFarmRecordMapper
{
    public WmFarmRecord selectWmFarmRecordById(Long recordId);

    public List<WmFarmRecord> selectWmFarmRecordList(WmFarmRecord farmRecord);

    public int insertWmFarmRecord(WmFarmRecord farmRecord);

    public int updateWmFarmRecord(WmFarmRecord farmRecord);

    public int deleteWmFarmRecordById(Long recordId);

    public int deleteWmFarmRecordByIds(Long[] recordIds);

    public WmFarmRecord selectWmFarmRecordByClientId(String clientId);

    public int countWmFarmRecordByBatchIds(Long[] batchIds);
}
