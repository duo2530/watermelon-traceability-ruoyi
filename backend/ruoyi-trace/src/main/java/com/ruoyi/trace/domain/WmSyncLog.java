package com.ruoyi.trace.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 同步日志对象 wm_sync_log
 *
 * @author ruoyi
 */
public class WmSyncLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 同步日志ID */
    @Excel(name = "同步日志ID")
    private Long logId;

    /** 客户端ID */
    private String clientId;

    /** 类型 */
    private String dataType;

    /** 结果 */
    private String result;

    /** 说明 */
    private String message;

    /** 业务ID */
    private Long bizId;

    /** 提交人 */
    @Excel(name = "提交人")
    private String createBy;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getLogId()
    {
        return logId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getResult()
    {
        return result;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("clientId", getClientId())
            .append("dataType", getDataType())
            .append("result", getResult())
            .append("message", getMessage())
            .append("bizId", getBizId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
