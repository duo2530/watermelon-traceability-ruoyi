package com.ruoyi.trace.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 地块对象 wm_plot
 *
 * @author ruoyi
 */
public class WmPlot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 地块ID */
    @Excel(name = "地块ID")
    private Long plotId;

    /** 地块名称 */
    @Excel(name = "地块名称")
    @NotBlank(message = "地块名称不能为空")
    private String plotName;

    /** 面积(亩) */
    @Excel(name = "面积(亩)")
    private BigDecimal areaMu;

    /** 地址 */
    @Excel(name = "地址")
    private String address;

    /** 经度 */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /** 土壤类型 */
    @Excel(name = "土壤类型")
    private String soilType;

    /** 农户ID */
    private Long farmerId;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 负责农户 */
    @Excel(name = "负责农户")
    private String farmerName;

    public void setPlotId(Long plotId)
    {
        this.plotId = plotId;
    }

    public Long getPlotId()
    {
        return plotId;
    }

    public void setPlotName(String plotName)
    {
        this.plotName = plotName;
    }

    public String getPlotName()
    {
        return plotName;
    }

    public void setAreaMu(BigDecimal areaMu)
    {
        this.areaMu = areaMu;
    }

    public BigDecimal getAreaMu()
    {
        return areaMu;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }

    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude()
    {
        return longitude;
    }

    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude()
    {
        return latitude;
    }

    public void setSoilType(String soilType)
    {
        this.soilType = soilType;
    }

    public String getSoilType()
    {
        return soilType;
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
            .append("plotId", getPlotId())
            .append("plotName", getPlotName())
            .append("areaMu", getAreaMu())
            .append("address", getAddress())
            .append("longitude", getLongitude())
            .append("latitude", getLatitude())
            .append("soilType", getSoilType())
            .append("farmerId", getFarmerId())
            .append("status", getStatus())
            .append("farmerName", getFarmerName())
            .toString();
    }
}
