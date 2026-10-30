package com.ruoyi.trace.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.mapper.WmGradeStandardMapper;
import com.ruoyi.trace.service.IWmGradeService;

/**
 * 分级判定实现
 *
 * @author ruoyi
 */
@Service
public class WmGradeServiceImpl implements IWmGradeService
{
    @Autowired
    private WmGradeStandardMapper gradeStandardMapper;

    @Override
    public WmGradeStandard evaluate(String variety, BigDecimal sugarCenter, BigDecimal sugarEdge, String fruitShape, BigDecimal weightKg)
    {
        WmGradeStandard query = new WmGradeStandard();
        query.setStatus("0");
        List<WmGradeStandard> standards = gradeStandardMapper.selectWmGradeStandardList(query);
        return match(standards, variety, sugarCenter, sugarEdge, fruitShape, weightKg);
    }

    /**
     * 纯函数，便于单元测试
     */
    public static WmGradeStandard match(List<WmGradeStandard> standards, String variety, BigDecimal sugarCenter,
            BigDecimal sugarEdge, String fruitShape, BigDecimal weightKg)
    {
        if (sugarCenter == null)
        {
            return null;
        }
        // 先匹配品种专用标准，再匹配通用标准
        for (int pass = 0; pass < 2; pass++)
        {
            for (WmGradeStandard s : standards)
            {
                boolean specific = StringUtils.isNotEmpty(s.getVariety());
                if (pass == 0 && (!specific || !s.getVariety().equals(variety)))
                {
                    continue;
                }
                if (pass == 1 && specific)
                {
                    continue;
                }
                if (satisfies(s, sugarCenter, sugarEdge, fruitShape, weightKg))
                {
                    return s;
                }
            }
        }
        return null;
    }

    private static boolean satisfies(WmGradeStandard s, BigDecimal sugarCenter, BigDecimal sugarEdge, String fruitShape, BigDecimal weightKg)
    {
        if (s.getSugarCenterMin() != null && sugarCenter.compareTo(s.getSugarCenterMin()) < 0)
        {
            return false;
        }
        if (s.getSugarEdgeMin() != null && (sugarEdge == null || sugarEdge.compareTo(s.getSugarEdgeMin()) < 0))
        {
            return false;
        }
        if (s.getWeightMin() != null && (weightKg == null || weightKg.compareTo(s.getWeightMin()) < 0))
        {
            return false;
        }
        if (s.getWeightMax() != null && (weightKg == null || weightKg.compareTo(s.getWeightMax()) > 0))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(s.getShapeAllow()))
        {
            List<String> allow = Arrays.asList(s.getShapeAllow().split(","));
            if (fruitShape == null || !allow.contains(fruitShape))
            {
                return false;
            }
        }
        return true;
    }
}
