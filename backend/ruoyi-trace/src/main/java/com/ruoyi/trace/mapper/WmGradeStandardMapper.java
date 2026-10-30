package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmGradeStandard;

/**
 * 分级标准Mapper接口
 *
 * @author ruoyi
 */
public interface WmGradeStandardMapper
{
    public WmGradeStandard selectWmGradeStandardById(Long standardId);

    public List<WmGradeStandard> selectWmGradeStandardList(WmGradeStandard gradeStandard);

    public int insertWmGradeStandard(WmGradeStandard gradeStandard);

    public int updateWmGradeStandard(WmGradeStandard gradeStandard);

    public int deleteWmGradeStandardById(Long standardId);

    public int deleteWmGradeStandardByIds(Long[] standardIds);
}
