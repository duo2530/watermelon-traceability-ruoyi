package com.ruoyi.trace.service;

import java.util.List;
import com.ruoyi.trace.domain.WmBatch;

/**
 * 批次Service接口
 *
 * @author ruoyi
 */
public interface IWmBatchService
{
    public WmBatch selectWmBatchById(Long batchId);

    public WmBatch selectWmBatchByCode(String batchCode);

    public List<WmBatch> selectWmBatchList(WmBatch wmBatch);

    /** 新增批次，自动生成批次码 WM+yyyyMMdd+4位序号 */
    public int insertWmBatch(WmBatch wmBatch);

    public int updateWmBatch(WmBatch wmBatch);

    /** 删除批次（存在农事/品控记录时拒绝） */
    public int deleteWmBatchByIds(Long[] batchIds);

    /** 生成（或重新生成）批次溯源二维码，返回更新后的批次 */
    public WmBatch generateQrCode(Long batchId);

    /** 二维码内容 */
    public String buildQrContent(String batchCode);

    public int increaseScanCount(Long batchId);
}
