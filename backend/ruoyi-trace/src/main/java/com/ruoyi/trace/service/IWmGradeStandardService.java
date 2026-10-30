package com.ruoyi.trace.service;

import java.util.List;
import com.ruoyi.trace.domain.WmGradeStandard;

/**
 * 分级标准Service接口
 *
 * @author ruoyi
 */
public interface IWmGradeStandardService
{
    public WmGradeStandard selectWmGradeStandardById(Long standardId);

    public List<WmGradeStandard> selectWmGradeStandardList(WmGradeStandard gradeStandard);

    public int insertWmGradeStandard(WmGradeStandard gradeStandard);

    public int updateWmGradeStandard(WmGradeStandard gradeStandard);

    public int deleteWmGradeStandardByIds(Long[] standardIds);
}
