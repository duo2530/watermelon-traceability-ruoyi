package com.ruoyi.trace.service;

import java.util.List;
import com.ruoyi.trace.domain.WmFarmRecord;

/**
 * 农事记录Service接口
 *
 * @author ruoyi
 */
public interface IWmFarmRecordService
{
    public WmFarmRecord selectWmFarmRecordById(Long recordId);

    public List<WmFarmRecord> selectWmFarmRecordList(WmFarmRecord farmRecord);

    public int insertWmFarmRecord(WmFarmRecord farmRecord);

    public int updateWmFarmRecord(WmFarmRecord farmRecord);

    public int deleteWmFarmRecordByIds(Long[] recordIds);
}
