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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmPlot;
import com.ruoyi.trace.service.IWmBatchService;
import com.ruoyi.trace.service.IWmPlotService;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 种植批次Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/batch")
public class WmBatchController extends BaseController
{
    @Autowired
    private IWmBatchService batchService;

    @Autowired
    private IWmPlotService plotService;

    @PreAuthorize("@ss.hasPermi('trace:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmBatch batch)
    {
        scope(batch);
        startPage();
        List<WmBatch> list = batchService.selectWmBatchList(batch);
        return getDataTable(list);
    }

    /**
     * 下拉选项（不分页，默认排除已归档）
     */
    @PreAuthorize("@ss.hasPermi('trace:batch:list')")
    @GetMapping("/options")
    public AjaxResult options(WmBatch batch)
    {
        scope(batch);
        List<WmBatch> list = batchService.selectWmBatchList(batch);
        if (batch.getStatus() == null)
        {
            list.removeIf(b -> "2".equals(b.getStatus()));
        }
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('trace:batch:export')")
    @Log(title = "种植批次", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmBatch batch)
    {
        scope(batch);
        List<WmBatch> list = batchService.selectWmBatchList(batch);
        ExcelUtil<WmBatch> util = new ExcelUtil<WmBatch>(WmBatch.class);
        util.exportExcel(response, list, "批次数据");
    }

    @PreAuthorize("@ss.hasPermi('trace:batch:query')")
    @GetMapping(value = "/{batchId}")
    public AjaxResult getInfo(@PathVariable("batchId") Long batchId)
    {
        WmBatch batch = load(batchId);
        AjaxResult ajax = success(batch);
        ajax.put("qrContent", batchService.buildQrContent(batch.getBatchCode()));
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('trace:batch:add')")
    @Log(title = "种植批次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmBatch batch)
    {
        checkPlot(batch.getPlotId());
        if (TraceDataScope.farmerScope() != null || batch.getFarmerId() == null)
        {
            batch.setFarmerId(getUserId());
        }
        batch.setCreateBy(getUsername());
        batchService.insertWmBatch(batch);
        // 新增后直接生成二维码，农户无需再点一次
        return success(batchService.generateQrCode(batch.getBatchId()));
    }

    @PreAuthorize("@ss.hasPermi('trace:batch:edit')")
    @Log(title = "种植批次", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmBatch batch)
    {
        WmBatch old = load(batch.getBatchId());
        if ("2".equals(old.getStatus()) && TraceDataScope.farmerScope() != null)
        {
            return error("批次已归档，不能修改");
        }
        checkPlot(batch.getPlotId());
        if (TraceDataScope.farmerScope() != null)
        {
            batch.setFarmerId(null);
            // 农户不能自行归档
            if ("2".equals(batch.getStatus()))
            {
                batch.setStatus(null);
            }
        }
        batch.setUpdateBy(getUsername());
        return toAjax(batchService.updateWmBatch(batch));
    }

    @PreAuthorize("@ss.hasPermi('trace:batch:remove')")
    @Log(title = "种植批次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{batchIds}")
    public AjaxResult remove(@PathVariable Long[] batchIds)
    {
        for (Long id : batchIds)
        {
            load(id);
        }
        return toAjax(batchService.deleteWmBatchByIds(batchIds));
    }

    /**
     * 生成/重新生成批次二维码
     */
    @PreAuthorize("@ss.hasPermi('trace:batch:qrcode')")
    @Log(title = "批次二维码", businessType = BusinessType.UPDATE)
    @PostMapping("/{batchId}/qrcode")
    public AjaxResult qrcode(@PathVariable("batchId") Long batchId)
    {
        load(batchId);
        WmBatch batch = batchService.generateQrCode(batchId);
        AjaxResult ajax = success(batch);
        ajax.put("qrContent", batchService.buildQrContent(batch.getBatchCode()));
        return ajax;
    }

    private void scope(WmBatch batch)
    {
        Long scope = TraceDataScope.farmerScope();
        if (scope != null)
        {
            batch.setFarmerId(scope);
        }
    }

    private void checkPlot(Long plotId)
    {
        if (plotId == null)
        {
            return;
        }
        WmPlot plot = plotService.selectWmPlotById(plotId);
        if (plot == null)
        {
            throw new ServiceException("地块不存在");
        }
        TraceDataScope.checkOwner(plot.getFarmerId());
    }

    private WmBatch load(Long batchId)
    {
        WmBatch batch = batchService.selectWmBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在");
        }
        TraceDataScope.checkOwner(batch.getFarmerId());
        return batch;
    }
}
