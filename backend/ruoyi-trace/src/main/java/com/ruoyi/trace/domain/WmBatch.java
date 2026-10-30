package com.ruoyi.trace.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 批次对象 wm_batch
 *
 * @author ruoyi
 */
public class WmBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 批次ID */
    @Excel(name = "批次ID")
    private Long batchId;

    /** 批次溯源码 */
    @Excel(name = "批次溯源码")
    private String batchCode;

    /** 地块ID */
    private Long plotId;

    /** 品种 */
    @Excel(name = "品种")
    @NotBlank(message = "品种不能为空")
    private String variety;

    /** 定植日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "定植日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date plantDate;

    /** 采收日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "采收日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date harvestDate;

    /** 预计产量(kg) */
    @Excel(name = "预计产量(kg)")
    private BigDecimal expectedYieldKg;

    /** 品牌名称 */
    @Excel(name = "品牌名称")
    private String brandName;

    /** 品牌LOGO */
    private String brandLogo;

    /** 品牌介绍 */
    @Excel(name = "品牌介绍")
    private String brandDesc;

    /** 封面图 */
    private String coverImage;

    /** 二维码 */
    private String qrPath;

    /** 扫码次数 */
    @Excel(name = "扫码次数")
    private Integer scanCount;

    /** 农户ID */
    private Long farmerId;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 地块 */
    @Excel(name = "地块")
    private String plotName;

    /** 农户 */
    @Excel(name = "农户")
    private String farmerName;

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setPlotId(Long plotId)
    {
        this.plotId = plotId;
    }

    public Long getPlotId()
    {
        return plotId;
    }

    public void setVariety(String variety)
    {
        this.variety = variety;
    }

    public String getVariety()
    {
        return variety;
    }

    public void setPlantDate(Date plantDate)
    {
        this.plantDate = plantDate;
    }

    public Date getPlantDate()
    {
        return plantDate;
    }

    public void setHarvestDate(Date harvestDate)
    {
        this.harvestDate = harvestDate;
    }

    public Date getHarvestDate()
    {
        return harvestDate;
    }

    public void setExpectedYieldKg(BigDecimal expectedYieldKg)
    {
        this.expectedYieldKg = expectedYieldKg;
    }

    public BigDecimal getExpectedYieldKg()
    {
        return expectedYieldKg;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandLogo(String brandLogo)
    {
        this.brandLogo = brandLogo;
    }

    public String getBrandLogo()
    {
        return brandLogo;
    }

    public void setBrandDesc(String brandDesc)
    {
        this.brandDesc = brandDesc;
    }

    public String getBrandDesc()
    {
        return brandDesc;
    }

    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }

    public String getCoverImage()
    {
        return coverImage;
    }

    public void setQrPath(String qrPath)
    {
        this.qrPath = qrPath;
    }

    public String getQrPath()
    {
        return qrPath;
    }

    public void setScanCount(Integer scanCount)
    {
        this.scanCount = scanCount;
    }

    public Integer getScanCount()
    {
        return scanCount;
    }

    public void setFarmerId(Long farmerId)
    {
        this.farmerId = farmerId;
    }

    public Long getFarmerId()
    {
        return farmerId;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setPlotName(String plotName)
    {
        this.plotName = plotName;
    }

    public String getPlotName()
    {
        return plotName;
    }

    public void setFarmerName(String farmerName)
    {
        this.farmerName = farmerName;
    }

    public String getFarmerName()
    {
        return farmerName;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("batchId", getBatchId())
            .append("batchCode", getBatchCode())
            .append("plotId", getPlotId())
            .append("variety", getVariety())
            .append("plantDate", getPlantDate())
            .append("harvestDate", getHarvestDate())
            .append("expectedYieldKg", getExpectedYieldKg())
            .append("brandName", getBrandName())
            .append("brandLogo", getBrandLogo())
            .append("brandDesc", getBrandDesc())
            .append("coverImage", getCoverImage())
            .append("qrPath", getQrPath())
            .append("scanCount", getScanCount())
            .append("farmerId", getFarmerId())
            .append("status", getStatus())
            .append("plotName", getPlotName())
            .append("farmerName", getFarmerName())
            .toString();
    }
}
