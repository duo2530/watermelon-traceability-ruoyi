package com.ruoyi.trace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.service.impl.WmGradeServiceImpl;

/**
 * 分级判定规则测试
 */
public class GradeMatchTest
{
    private static WmGradeStandard std(String code, String variety, String sugar, String edge, String wMin, String wMax, String shape)
    {
        WmGradeStandard s = new WmGradeStandard();
        s.setGradeCode(code);
        s.setGradeName(code);
        s.setVariety(variety);
        s.setSugarCenterMin(sugar == null ? null : new BigDecimal(sugar));
        s.setSugarEdgeMin(edge == null ? null : new BigDecimal(edge));
        s.setWeightMin(wMin == null ? null : new BigDecimal(wMin));
        s.setWeightMax(wMax == null ? null : new BigDecimal(wMax));
        s.setShapeAllow(shape);
        return s;
    }

    private final List<WmGradeStandard> standards = Arrays.asList(
            std("SPECIAL", "", "12", "9", "5", "9", "1"),
            std("FIRST", "", "11", "8", "4", "10", "1,2"),
            std("SECOND", "", "10", null, "3", null, "1,2"),
            std("KYLIN_TOP", "麒麟", "13", null, null, null, ""));

    private String grade(String variety, String sugar, String edge, String shape, String w)
    {
        WmGradeStandard g = WmGradeServiceImpl.match(standards, variety, new BigDecimal(sugar),
                edge == null ? null : new BigDecimal(edge), shape, w == null ? null : new BigDecimal(w));
        return g == null ? null : g.getGradeCode();
    }

    @Test
    public void matchesBestGradeFirst()
    {
        assertEquals("SPECIAL", grade("8424", "12.5", "9.5", "1", "6"));
        assertEquals("FIRST", grade("8424", "12.5", "9.5", "2", "6"));
        assertEquals("SECOND", grade("8424", "10.5", null, "1", "3.5"));
    }

    @Test
    public void belowAllIsNull()
    {
        assertNull(grade("8424", "9.9", "9", "1", "6"));
        assertNull(grade("8424", "12", "9", "3", "6"));
    }

    @Test
    public void varietySpecificWins()
    {
        assertEquals("KYLIN_TOP", grade("麒麟", "13.2", null, "3", null));
        assertEquals("FIRST", grade("麒麟", "11.5", "8.5", "2", "6"));
    }
}
