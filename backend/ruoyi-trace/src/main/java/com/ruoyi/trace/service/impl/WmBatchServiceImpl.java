package com.ruoyi.trace.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.mapper.WmBatchMapper;
import com.ruoyi.trace.mapper.WmFarmRecordMapper;
import com.ruoyi.trace.mapper.WmQcRecordMapper;
import com.ruoyi.trace.service.IWmBatchService;
import com.ruoyi.trace.util.QrCodeUtils;

/**
 * 批次Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WmBatchServiceImpl implements IWmBatchService
{
    private static final String CODE_PREFIX = "WM";

    @Autowired
    private WmBatchMapper wmBatchMapper;

    @Autowired
    private WmFarmRecordMapper farmRecordMapper;

    @Autowired
    private WmQcRecordMapper qcRecordMapper;

    /** 二维码内容前缀，后接批次码 */
    @Value("${trace.qr-prefix:http://localhost:9090/#/pages/consumer/trace?code=}")
    private String qrPrefix;

    @Override
    public WmBatch selectWmBatchById(Long batchId)
    {
        return wmBatchMapper.selectWmBatchById(batchId);
    }

    @Override
    public WmBatch selectWmBatchByCode(String batchCode)
    {
        return wmBatchMapper.selectWmBatchByCode(batchCode);
    }

    @Override
    public List<WmBatch> selectWmBatchList(WmBatch wmBatch)
    {
        return wmBatchMapper.selectWmBatchList(wmBatch);
    }

    @Override
    public int insertWmBatch(WmBatch wmBatch)
    {
        if (StringUtils.isEmpty(wmBatch.getStatus()))
        {
            wmBatch.setStatus("0");
        }
        // 批次码唯一键冲突时重试（并发新增）
        for (int i = 0; i < 5; i++)
        {
            wmBatch.setBatchCode(nextBatchCode());
            try
            {
                return wmBatchMapper.insertWmBatch(wmBatch);
            }
            catch (DuplicateKeyException e)
            {
                // retry
            }
        }
        throw new ServiceException("批次码生成冲突，请重试");
    }

    private String nextBatchCode()
    {
        String prefix = CODE_PREFIX + DateUtils.parseDateToStr("yyyyMMdd", new Date());
        String max = wmBatchMapper.selectMaxBatchCode(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(max) && max.length() > prefix.length())
        {
            seq = Integer.parseInt(max.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    @Override
    public int updateWmBatch(WmBatch wmBatch)
    {
        // 批次码、二维码、扫码数不允许通过修改接口变更
        wmBatch.setBatchCode(null);
        wmBatch.setQrPath(null);
        wmBatch.setScanCount(null);
        return wmBatchMapper.updateWmBatch(wmBatch);
    }

    @Override
    public int deleteWmBatchByIds(Long[] batchIds)
    {
        if (farmRecordMapper.countWmFarmRecordByBatchIds(batchIds) > 0 || qcRecordMapper.countWmQcRecordByBatchIds(batchIds) > 0)
        {
            throw new ServiceException("批次下存在农事或品控记录，不能删除，可改为归档");
        }
        return wmBatchMapper.deleteWmBatchByIds(batchIds);
    }

    @Override
    public WmBatch generateQrCode(Long batchId)
    {
        WmBatch batch = wmBatchMapper.selectWmBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在");
        }
        try
        {
            String path = QrCodeUtils.generate(buildQrContent(batch.getBatchCode()), batch.getBatchCode(), 400);
            WmBatch update = new WmBatch();
            update.setBatchId(batchId);
            update.setQrPath(path);
            wmBatchMapper.updateWmBatch(update);
            batch.setQrPath(path);
            return batch;
        }
        catch (Exception e)
        {
            throw new ServiceException("二维码生成失败：" + e.getMessage());
        }
    }

    @Override
    public String buildQrContent(String batchCode)
    {
        return qrPrefix + batchCode;
    }

    @Override
    public int increaseScanCount(Long batchId)
    {
        return wmBatchMapper.increaseScanCount(batchId);
    }
}
