package com.ruoyi.trace.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.mapper.WmGradeStandardMapper;
import com.ruoyi.trace.service.IWmGradeStandardService;

/**
 * 分级标准Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WmGradeStandardServiceImpl implements IWmGradeStandardService
{
    @Autowired
    private WmGradeStandardMapper gradeStandardMapper;

    @Override
    public WmGradeStandard selectWmGradeStandardById(Long standardId)
    {
        return gradeStandardMapper.selectWmGradeStandardById(standardId);
    }

    @Override
    public List<WmGradeStandard> selectWmGradeStandardList(WmGradeStandard gradeStandard)
    {
        return gradeStandardMapper.selectWmGradeStandardList(gradeStandard);
    }

    @Override
    public int insertWmGradeStandard(WmGradeStandard gradeStandard)
    {
        return gradeStandardMapper.insertWmGradeStandard(gradeStandard);
    }

    @Override
    public int updateWmGradeStandard(WmGradeStandard gradeStandard)
    {
        return gradeStandardMapper.updateWmGradeStandard(gradeStandard);
    }

    @Override
    public int deleteWmGradeStandardByIds(Long[] standardIds)
    {
        return gradeStandardMapper.deleteWmGradeStandardByIds(standardIds);
    }
}
