package com.ruoyi.trace.service;

import java.math.BigDecimal;
import com.ruoyi.trace.domain.WmGradeStandard;

/**
 * 分级判定
 *
 * @author ruoyi
 */
public interface IWmGradeService
{
    /**
     * 按 sort 升序找第一条满足全部条件的启用标准；都不满足返回 null（等外）
     */
    public WmGradeStandard evaluate(String variety, BigDecimal sugarCenter, BigDecimal sugarEdge, String fruitShape, BigDecimal weightKg);
}
