package com.ruoyi.trace.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmFarmRecord;
import com.ruoyi.trace.service.IWmBatchService;
import com.ruoyi.trace.service.IWmFarmRecordService;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 农事记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/farm")
public class WmFarmRecordController extends BaseController
{
    @Autowired
    private IWmFarmRecordService farmRecordService;

    @Autowired
    private IWmBatchService batchService;

    @PreAuthorize("@ss.hasPermi('trace:farm:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmFarmRecord record)
    {
        record.setFarmerId(TraceDataScope.farmerScope());
        startPage();
        List<WmFarmRecord> list = farmRecordService.selectWmFarmRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('trace:farm:export')")
    @Log(title = "农事记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmFarmRecord record)
    {
        record.setFarmerId(TraceDataScope.farmerScope());
        List<WmFarmRecord> list = farmRecordService.selectWmFarmRecordList(record);
        ExcelUtil<WmFarmRecord> util = new ExcelUtil<WmFarmRecord>(WmFarmRecord.class);
        util.exportExcel(response, list, "农事记录");
    }

    @PreAuthorize("@ss.hasPermi('trace:farm:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(load(recordId));
    }

    @PreAuthorize("@ss.hasPermi('trace:farm:add')")
    @Log(title = "农事记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmFarmRecord record)
    {
        checkBatch(record.getBatchId());
        // 在线录入不走幂等键；离线请走 /trace/sync/batch
        record.setClientId(null);
        if (StringUtils.isEmpty(record.getOperator()))
        {
            record.setOperator(getLoginUser().getUser().getNickName());
        }
        record.setCreateBy(getUsername());
        return toAjax(farmRecordService.insertWmFarmRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('trace:farm:edit')")
    @Log(title = "农事记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmFarmRecord record)
    {
        load(record.getRecordId());
        checkBatch(record.getBatchId());
        record.setUpdateBy(getUsername());
        return toAjax(farmRecordService.updateWmFarmRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('trace:farm:remove')")
    @Log(title = "农事记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        for (Long id : recordIds)
        {
            WmFarmRecord r = load(id);
            checkBatch(r.getBatchId());
        }
        return toAjax(farmRecordService.deleteWmFarmRecordByIds(recordIds));
    }

    private WmFarmRecord load(Long recordId)
    {
        WmFarmRecord r = farmRecordService.selectWmFarmRecordById(recordId);
        if (r == null)
        {
            throw new ServiceException("农事记录不存在");
        }
        TraceDataScope.checkOwner(r.getFarmerId());
        return r;
    }

    /**
     * 校验批次存在、归属当前农户且未归档
     */
    private void checkBatch(Long batchId)
    {
        WmBatch batch = batchService.selectWmBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在");
        }
        TraceDataScope.checkOwner(batch.getFarmerId());
        if ("2".equals(batch.getStatus()))
        {
            throw new ServiceException("批次已归档，不能修改记录");
        }
    }
}
