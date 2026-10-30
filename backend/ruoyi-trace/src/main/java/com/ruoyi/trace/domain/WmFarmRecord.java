package com.ruoyi.trace.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 农事记录对象 wm_farm_record
 *
 * @author ruoyi
 */
public class WmFarmRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 农事记录ID */
    @Excel(name = "农事记录ID")
    private Long recordId;

    /** 批次ID */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    /** 农事类型 */
    @Excel(name = "农事类型")
    @NotBlank(message = "农事类型不能为空")
    private String recordType;

    /** 作业内容 */
    @Excel(name = "作业内容")
    private String content;

    /** 投入品 */
    @Excel(name = "投入品")
    private String inputName;

    /** 用量 */
    @Excel(name = "用量")
    private String inputAmount;

    /** 作业时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "作业时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    /** 天气 */
    @Excel(name = "天气")
    private String weather;

    /** 图片 */
    private String images;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operator;

    /** 客户端ID */
    private String clientId;

    /** 批次溯源码 */
    @Excel(name = "批次溯源码")
    private String batchCode;

    /** farmerId */
    private Long farmerId;

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getRecordId()
    {
        return recordId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setInputName(String inputName)
    {
        this.inputName = inputName;
    }

    public String getInputName()
    {
        return inputName;
    }

    public void setInputAmount(String inputAmount)
    {
        this.inputAmount = inputAmount;
    }

    public String getInputAmount()
    {
        return inputAmount;
    }

    public void setRecordTime(Date recordTime)
    {
        this.recordTime = recordTime;
    }

    public Date getRecordTime()
    {
        return recordTime;
    }

    public void setWeather(String weather)
    {
        this.weather = weather;
    }

    public String getWeather()
    {
        return weather;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getImages()
    {
        return images;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getOperator()
    {
        return operator;
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
            .append("recordId", getRecordId())
            .append("batchId", getBatchId())
            .append("recordType", getRecordType())
            .append("content", getContent())
            .append("inputName", getInputName())
            .append("inputAmount", getInputAmount())
            .append("recordTime", getRecordTime())
            .append("weather", getWeather())
            .append("images", getImages())
            .append("operator", getOperator())
            .append("clientId", getClientId())
            .append("batchCode", getBatchCode())
            .append("farmerId", getFarmerId())
            .toString();
    }
}
