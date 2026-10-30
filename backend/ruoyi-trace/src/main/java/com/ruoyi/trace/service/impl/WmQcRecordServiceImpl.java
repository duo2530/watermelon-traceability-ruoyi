package com.ruoyi.trace.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.domain.WmQcRecord;
import com.ruoyi.trace.mapper.WmBatchMapper;
import com.ruoyi.trace.mapper.WmQcRecordMapper;
import com.ruoyi.trace.service.IWmGradeService;
import com.ruoyi.trace.service.IWmQcRecordService;

/**
 * 品控记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WmQcRecordServiceImpl implements IWmQcRecordService
{
    @Autowired
    private WmQcRecordMapper qcRecordMapper;

    @Autowired
    private WmBatchMapper batchMapper;

    @Autowired
    private IWmGradeService gradeService;

    @Override
    public WmQcRecord selectWmQcRecordById(Long qcId)
    {
        return qcRecordMapper.selectWmQcRecordById(qcId);
    }

    @Override
    public List<WmQcRecord> selectWmQcRecordList(WmQcRecord qcRecord)
    {
        return qcRecordMapper.selectWmQcRecordList(qcRecord);
    }

    @Override
    public int insertWmQcRecord(WmQcRecord qcRecord)
    {
        if (qcRecord.getCheckTime() == null)
        {
            qcRecord.setCheckTime(DateUtils.getNowDate());
        }
        applyGrade(qcRecord);
        return qcRecordMapper.insertWmQcRecord(qcRecord);
    }

    @Override
    public int updateWmQcRecord(WmQcRecord qcRecord)
    {
        applyGrade(qcRecord);
        return qcRecordMapper.updateWmQcRecord(qcRecord);
    }

    @Override
    public int deleteWmQcRecordByIds(Long[] qcIds)
    {
        return qcRecordMapper.deleteWmQcRecordByIds(qcIds);
    }

    /**
     * 按分级标准自动判定等级（覆盖客户端传入的等级）
     */
    private void applyGrade(WmQcRecord qc)
    {
        if (qc.getSugarCenter() == null)
        {
            return;
        }
        String variety = qc.getVariety();
        if (variety == null && qc.getBatchId() != null)
        {
            WmBatch batch = batchMapper.selectWmBatchById(qc.getBatchId());
            variety = batch != null ? batch.getVariety() : null;
        }
        WmGradeStandard grade = gradeService.evaluate(variety, qc.getSugarCenter(), qc.getSugarEdge(), qc.getFruitShape(), qc.getWeightKg());
        qc.setGradeCode(grade != null ? grade.getGradeCode() : "NONE");
        qc.setGradeName(grade != null ? grade.getGradeName() : "等外");
    }
}
