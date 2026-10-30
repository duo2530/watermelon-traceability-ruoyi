package com.ruoyi.trace.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * 离线同步条目（小程序本地队列中的一条）
 *
 * @author ruoyi
 */
public class WmSyncItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 客户端生成的唯一ID（幂等键） */
    private String clientId;

    /** 数据类型 farm / qc */
    private String type;

    /** 业务数据，字段与 WmFarmRecord / WmQcRecord 一致 */
    private Map<String, Object> payload;

    /** 客户端创建时间戳(ms) */
    private Long createdAt;

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map<String, Object> getPayload()
    {
        return payload;
    }

    public void setPayload(Map<String, Object> payload)
    {
        this.payload = payload;
    }

    public Long getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt)
    {
        this.createdAt = createdAt;
    }
}
