package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmBatch;

/**
 * 批次Mapper接口
 *
 * @author ruoyi
 */
public interface WmBatchMapper
{
    public WmBatch selectWmBatchById(Long batchId);

    public List<WmBatch> selectWmBatchList(WmBatch batch);

    public int insertWmBatch(WmBatch batch);

    public int updateWmBatch(WmBatch batch);

    public int deleteWmBatchById(Long batchId);

    public int deleteWmBatchByIds(Long[] batchIds);

    public WmBatch selectWmBatchByCode(String batchCode);

    public String selectMaxBatchCode(String codePrefix);

    public int increaseScanCount(Long batchId);
}
