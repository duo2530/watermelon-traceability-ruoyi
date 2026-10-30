package com.ruoyi.trace.service;

import java.util.List;
import com.ruoyi.trace.domain.WmQcRecord;

/**
 * 品控记录Service接口
 *
 * @author ruoyi
 */
public interface IWmQcRecordService
{
    public WmQcRecord selectWmQcRecordById(Long qcId);

    public List<WmQcRecord> selectWmQcRecordList(WmQcRecord qcRecord);

    public int insertWmQcRecord(WmQcRecord qcRecord);

    public int updateWmQcRecord(WmQcRecord qcRecord);

    public int deleteWmQcRecordByIds(Long[] qcIds);
}
