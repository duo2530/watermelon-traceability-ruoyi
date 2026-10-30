package com.ruoyi.trace.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trace.domain.WmFarmRecord;
import com.ruoyi.trace.mapper.WmFarmRecordMapper;
import com.ruoyi.trace.service.IWmFarmRecordService;

/**
 * 农事记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WmFarmRecordServiceImpl implements IWmFarmRecordService
{
    @Autowired
    private WmFarmRecordMapper farmRecordMapper;

    @Override
    public WmFarmRecord selectWmFarmRecordById(Long recordId)
    {
        return farmRecordMapper.selectWmFarmRecordById(recordId);
    }

    @Override
    public List<WmFarmRecord> selectWmFarmRecordList(WmFarmRecord farmRecord)
    {
        return farmRecordMapper.selectWmFarmRecordList(farmRecord);
    }

    @Override
    public int insertWmFarmRecord(WmFarmRecord farmRecord)
    {
        if (farmRecord.getRecordTime() == null)
        {
            farmRecord.setRecordTime(DateUtils.getNowDate());
        }
        return farmRecordMapper.insertWmFarmRecord(farmRecord);
    }

    @Override
    public int updateWmFarmRecord(WmFarmRecord farmRecord)
    {
        return farmRecordMapper.updateWmFarmRecord(farmRecord);
    }

    @Override
    public int deleteWmFarmRecordByIds(Long[] recordIds)
    {
        return farmRecordMapper.deleteWmFarmRecordByIds(recordIds);
    }
}
