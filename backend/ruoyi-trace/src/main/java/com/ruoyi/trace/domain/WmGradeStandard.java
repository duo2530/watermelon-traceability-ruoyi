package com.ruoyi.trace.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 分级标准对象 wm_grade_standard
 *
 * @author ruoyi
 */
public class WmGradeStandard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分级标准ID */
    @Excel(name = "分级标准ID")
    private Long standardId;

    /** 等级编码 */
    @Excel(name = "等级编码")
    @NotBlank(message = "等级编码不能为空")
    private String gradeCode;

    /** 等级名称 */
    @Excel(name = "等级名称")
    @NotBlank(message = "等级名称不能为空")
    private String gradeName;

    /** 适用品种 */
    @Excel(name = "适用品种")
    private String variety;

    /** 中心糖度下限 */
    @Excel(name = "中心糖度下限")
    private BigDecimal sugarCenterMin;

    /** 边糖下限 */
    @Excel(name = "边糖下限")
    private BigDecimal sugarEdgeMin;

    /** 单果重下限 */
    @Excel(name = "单果重下限")
    private BigDecimal weightMin;

    /** 单果重上限 */
    @Excel(name = "单果重上限")
    private BigDecimal weightMax;

    /** 允许果型 */
    @Excel(name = "允许果型")
    private String shapeAllow;

    /** 优先级 */
    @Excel(name = "优先级")
    private Integer sort;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public void setStandardId(Long standardId)
    {
        this.standardId = standardId;
    }

    public Long getStandardId()
    {
        return standardId;
    }

    public void setGradeCode(String gradeCode)
    {
        this.gradeCode = gradeCode;
    }

    public String getGradeCode()
    {
        return gradeCode;
    }

    public void setGradeName(String gradeName)
    {
        this.gradeName = gradeName;
    }

    public String getGradeName()
    {
        return gradeName;
    }

    public void setVariety(String variety)
    {
        this.variety = variety;
    }

    public String getVariety()
    {
        return variety;
    }

    public void setSugarCenterMin(BigDecimal sugarCenterMin)
    {
        this.sugarCenterMin = sugarCenterMin;
    }

    public BigDecimal getSugarCenterMin()
    {
        return sugarCenterMin;
    }

    public void setSugarEdgeMin(BigDecimal sugarEdgeMin)
    {
        this.sugarEdgeMin = sugarEdgeMin;
    }

    public BigDecimal getSugarEdgeMin()
    {
        return sugarEdgeMin;
    }

    public void setWeightMin(BigDecimal weightMin)
    {
        this.weightMin = weightMin;
    }

    public BigDecimal getWeightMin()
    {
        return weightMin;
    }

    public void setWeightMax(BigDecimal weightMax)
    {
        this.weightMax = weightMax;
    }

    public BigDecimal getWeightMax()
    {
        return weightMax;
    }

    public void setShapeAllow(String shapeAllow)
    {
        this.shapeAllow = shapeAllow;
    }

    public String getShapeAllow()
    {
        return shapeAllow;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("standardId", getStandardId())
            .append("gradeCode", getGradeCode())
            .append("gradeName", getGradeName())
            .append("variety", getVariety())
            .append("sugarCenterMin", getSugarCenterMin())
            .append("sugarEdgeMin", getSugarEdgeMin())
            .append("weightMin", getWeightMin())
            .append("weightMax", getWeightMax())
            .append("shapeAllow", getShapeAllow())
            .append("sort", getSort())
            .append("status", getStatus())
            .toString();
    }
}
