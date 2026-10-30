package com.ruoyi.trace.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmFarmRecord;
import com.ruoyi.trace.domain.WmQcRecord;
import com.ruoyi.trace.domain.WmSyncItem;
import com.ruoyi.trace.domain.WmSyncLog;
import com.ruoyi.trace.mapper.WmBatchMapper;
import com.ruoyi.trace.mapper.WmFarmRecordMapper;
import com.ruoyi.trace.mapper.WmQcRecordMapper;
import com.ruoyi.trace.mapper.WmSyncLogMapper;
import com.ruoyi.trace.service.IWmFarmRecordService;
import com.ruoyi.trace.service.IWmQcRecordService;
import com.ruoyi.trace.service.IWmSyncService;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 离线同步实现：逐条处理，单条失败不影响其他条目
 *
 * @author ruoyi
 */
@Service
public class WmSyncServiceImpl implements IWmSyncService
{
    private static final Logger log = LoggerFactory.getLogger(WmSyncServiceImpl.class);

    public static final String ACCEPTED = "accepted";
    public static final String DUPLICATE = "duplicate";
    public static final String REJECTED = "rejected";

    private static final int MAX_ITEMS = 200;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setTimeZone(TimeZone.getDefault());

    @Autowired
    private WmBatchMapper batchMapper;

    @Autowired
    private WmFarmRecordMapper farmRecordMapper;

    @Autowired
    private WmQcRecordMapper qcRecordMapper;

    @Autowired
    private IWmFarmRecordService farmRecordService;

    @Autowired
    private IWmQcRecordService qcRecordService;

    @Autowired
    private WmSyncLogMapper syncLogMapper;

    @Override
    public List<Map<String, Object>> syncBatch(List<WmSyncItem> items, String username)
    {
        if (items == null || items.isEmpty())
        {
            return new ArrayList<>();
        }
        if (items.size() > MAX_ITEMS)
        {
            throw new ServiceException("单次同步最多 " + MAX_ITEMS + " 条");
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (WmSyncItem item : items)
        {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("clientId", item.getClientId());
            String result;
            Long bizId = null;
            String message = "";
            try
            {
                bizId = handle(item, username);
                result = ACCEPTED;
            }
            catch (DuplicateException e)
            {
                result = DUPLICATE;
                bizId = e.bizId;
                message = "已同步过";
            }
            catch (DuplicateKeyException e)
            {
                result = DUPLICATE;
                message = "已同步过";
            }
            catch (ServiceException e)
            {
                result = REJECTED;
                message = e.getMessage();
            }
            catch (Exception e)
            {
                result = REJECTED;
                message = "数据格式错误";
                log.warn("离线同步失败 clientId={} : {}", item.getClientId(), e.getMessage());
            }
            r.put("result", result);
            r.put("bizId", bizId);
            r.put("message", message);
            results.add(r);
            writeLog(item, result, message, bizId, username);
        }
        return results;
    }

    private Long handle(WmSyncItem item, String username)
    {
        String clientId = item.getClientId();
        if (StringUtils.isEmpty(clientId) || clientId.length() > 64)
        {
            throw new ServiceException("clientId 无效");
        }
        if (item.getPayload() == null)
        {
            throw new ServiceException("payload 为空");
        }
        if ("farm".equals(item.getType()))
        {
            WmFarmRecord exist = farmRecordMapper.selectWmFarmRecordByClientId(clientId);
            if (exist != null)
            {
                throw new DuplicateException(exist.getRecordId());
            }
            WmFarmRecord rec = mapper.convertValue(item.getPayload(), WmFarmRecord.class);
            rec.setRecordId(null);
            checkBatch(rec.getBatchId());
            if (StringUtils.isEmpty(rec.getRecordType()))
            {
                throw new ServiceException("农事类型不能为空");
            }
            rec.setClientId(clientId);
            rec.setCreateBy(username);
            if (StringUtils.isEmpty(rec.getOperator()))
            {
                rec.setOperator(username);
            }
            farmRecordService.insertWmFarmRecord(rec);
            return rec.getRecordId();
        }
        else if ("qc".equals(item.getType()))
        {
            WmQcRecord exist = qcRecordMapper.selectWmQcRecordByClientId(clientId);
            if (exist != null)
            {
                throw new DuplicateException(exist.getQcId());
            }
            WmQcRecord rec = mapper.convertValue(item.getPayload(), WmQcRecord.class);
            rec.setQcId(null);
            checkBatch(rec.getBatchId());
            checkSugar(rec.getSugarCenter(), true);
            checkSugar(rec.getSugarEdge(), false);
            rec.setClientId(clientId);
            rec.setCreateBy(username);
            if (StringUtils.isEmpty(rec.getInspector()))
            {
                rec.setInspector(username);
            }
            qcRecordService.insertWmQcRecord(rec);
            return rec.getQcId();
        }
        throw new ServiceException("未知数据类型：" + item.getType());
    }

    private void checkBatch(Long batchId)
    {
        if (batchId == null)
        {
            throw new ServiceException("批次不能为空");
        }
        WmBatch batch = batchMapper.selectWmBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在");
        }
        TraceDataScope.checkOwner(batch.getFarmerId());
        if ("2".equals(batch.getStatus()))
        {
            throw new ServiceException("批次已归档，不能再录入");
        }
    }

    /**
     * 糖度合法性校验 0-30 °Brix
     */
    public static void checkSugar(BigDecimal sugar, boolean required)
    {
        if (sugar == null)
        {
            if (required)
            {
                throw new ServiceException("中心糖度不能为空");
            }
            return;
        }
        if (sugar.compareTo(BigDecimal.ZERO) < 0 || sugar.compareTo(new BigDecimal("30")) > 0)
        {
            throw new ServiceException("糖度应在 0-30 °Brix 之间");
        }
    }

    private void writeLog(WmSyncItem item, String result, String message, Long bizId, String username)
    {
        try
        {
            WmSyncLog l = new WmSyncLog();
            l.setClientId(StringUtils.substring(StringUtils.nvl(item.getClientId(), ""), 0, 64));
            l.setDataType(StringUtils.substring(StringUtils.nvl(item.getType(), ""), 0, 16));
            l.setResult(result);
            l.setMessage(StringUtils.substring(message, 0, 500));
            l.setBizId(bizId);
            l.setCreateBy(username);
            syncLogMapper.insertWmSyncLog(l);
        }
        catch (Exception e)
        {
            log.warn("写同步日志失败: {}", e.getMessage());
        }
    }

    private static class DuplicateException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private final Long bizId;

        DuplicateException(Long bizId)
        {
            super("duplicate", null, false, false);
            this.bizId = bizId;
        }
    }
}
