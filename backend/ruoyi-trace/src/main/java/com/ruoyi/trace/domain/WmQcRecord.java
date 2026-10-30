package com.ruoyi.trace.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 品控记录对象 wm_qc_record
 *
 * @author ruoyi
 */
public class WmQcRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 品控记录ID */
    @Excel(name = "品控记录ID")
    private Long qcId;

    /** 批次ID */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    /** 样品编号 */
    @Excel(name = "样品编号")
    private String sampleNo;

    /** 中心糖度 */
    @Excel(name = "中心糖度")
    @NotNull(message = "中心糖度不能为空")
    private BigDecimal sugarCenter;

    /** 边糖 */
    @Excel(name = "边糖")
    private BigDecimal sugarEdge;

    /** 果型 */
    @Excel(name = "果型")
    private String fruitShape;

    /** 单果重(kg) */
    @Excel(name = "单果重(kg)")
    private BigDecimal weightKg;

    /** 等级编码 */
    @Excel(name = "等级编码")
    private String gradeCode;

    /** 等级 */
    @Excel(name = "等级")
    private String gradeName;

    /** 检测时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "检测时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /** 图片 */
    private String images;

    /** 检测人 */
    @Excel(name = "检测人")
    private String inspector;

    /** 客户端ID */
    private String clientId;

    /** 批次溯源码 */
    @Excel(name = "批次溯源码")
    private String batchCode;

    /** 品种 */
    @Excel(name = "品种")
    private String variety;

    /** farmerId */
    private Long farmerId;

    public void setQcId(Long qcId)
    {
        this.qcId = qcId;
    }

    public Long getQcId()
    {
        return qcId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setSampleNo(String sampleNo)
    {
        this.sampleNo = sampleNo;
    }

    public String getSampleNo()
    {
        return sampleNo;
    }

    public void setSugarCenter(BigDecimal sugarCenter)
    {
        this.sugarCenter = sugarCenter;
    }

    public BigDecimal getSugarCenter()
    {
        return sugarCenter;
    }

    public void setSugarEdge(BigDecimal sugarEdge)
    {
        this.sugarEdge = sugarEdge;
    }

    public BigDecimal getSugarEdge()
    {
        return sugarEdge;
    }

    public void setFruitShape(String fruitShape)
    {
        this.fruitShape = fruitShape;
    }

    public String getFruitShape()
    {
        return fruitShape;
    }

    public void setWeightKg(BigDecimal weightKg)
    {
        this.weightKg = weightKg;
    }

    public BigDecimal getWeightKg()
    {
        return weightKg;
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

    public void setCheckTime(Date checkTime)
    {
        this.checkTime = checkTime;
    }

    public Date getCheckTime()
    {
        return checkTime;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getImages()
    {
        return images;
    }

    public void setInspector(String inspector)
    {
        this.inspector = inspector;
    }

    public String getInspector()
    {
        return inspector;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setVariety(String variety)
    {
        this.variety = variety;
    }

    public String getVariety()
    {
        return variety;
    }

    public void setFarmerId(Long farmerId)
    {
        this.farmerId = farmerId;
    }

    public Long getFarmerId()
    {
        return farmerId;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("qcId", getQcId())
            .append("batchId", getBatchId())
            .append("sampleNo", getSampleNo())
            .append("sugarCenter", getSugarCenter())
            .append("sugarEdge", getSugarEdge())
            .append("fruitShape", getFruitShape())
            .append("weightKg", getWeightKg())
            .append("gradeCode", getGradeCode())
            .append("gradeName", getGradeName())
            .append("checkTime", getCheckTime())
            .append("images", getImages())
            .append("inspector", getInspector())
            .append("clientId", getClientId())
            .append("batchCode", getBatchCode())
            .append("variety", getVariety())
            .append("farmerId", getFarmerId())
            .toString();
    }
}
