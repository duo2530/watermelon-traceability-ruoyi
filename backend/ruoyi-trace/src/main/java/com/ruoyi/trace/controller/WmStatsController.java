package com.ruoyi.trace.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelSheet;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmFarmRecord;
import com.ruoyi.trace.domain.WmQcRecord;
import com.ruoyi.trace.service.IWmBatchService;
import com.ruoyi.trace.service.IWmFarmRecordService;
import com.ruoyi.trace.service.IWmQcRecordService;
import com.ruoyi.trace.service.IWmTraceService;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 品控统计 / 数据导出归档
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace")
public class WmStatsController extends BaseController
{
    @Autowired
    private IWmTraceService traceService;

    @Autowired
    private IWmBatchService batchService;

    @Autowired
    private IWmFarmRecordService farmRecordService;

    @Autowired
    private IWmQcRecordService qcRecordService;

    /**
     * 品控统计（农户只统计自己的数据）
     */
    @PreAuthorize("@ss.hasAnyPermi('trace:stats:view,trace:qc:list')")
    @GetMapping("/stats")
    public AjaxResult stats(@RequestParam(required = false) String variety, @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime)
    {
        return success(traceService.stats(TraceDataScope.farmerScope(), variety, beginTime, endTime));
    }

    /**
     * 归档导出：批次 + 农事 + 品控三个 Sheet 一个文件
     *
     * @param batchId 为空则导出全部（可按 status / 时间过滤）
     */
    @PreAuthorize("@ss.hasPermi('trace:archive:export')")
    @Log(title = "溯源归档导出", businessType = BusinessType.EXPORT)
    @PostMapping("/archive/export")
    public void export(HttpServletResponse response, @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime)
    {
        WmBatch bq = new WmBatch();
        bq.setStatus(status);
        bq.getParams().put("beginTime", beginTime);
        bq.getParams().put("endTime", endTime);
        bq.setFarmerId(TraceDataScope.farmerScope());
        List<WmBatch> batches = new ArrayList<>();
        if (batchId != null)
        {
            WmBatch b = batchService.selectWmBatchById(batchId);
            if (b != null)
            {
                TraceDataScope.checkOwner(b.getFarmerId());
                batches.add(b);
            }
        }
        else
        {
            batches = batchService.selectWmBatchList(bq);
        }
        List<WmFarmRecord> farms = new ArrayList<>();
        List<WmQcRecord> qcs = new ArrayList<>();
        for (WmBatch b : batches)
        {
            WmFarmRecord fq = new WmFarmRecord();
            fq.setBatchId(b.getBatchId());
            farms.addAll(farmRecordService.selectWmFarmRecordList(fq));
            WmQcRecord qq = new WmQcRecord();
            qq.setBatchId(b.getBatchId());
            qcs.addAll(qcRecordService.selectWmQcRecordList(qq));
        }
        List<ExcelSheet<?>> sheets = new ArrayList<>();
        sheets.add(new ExcelSheet<>("种植批次", batches, WmBatch.class));
        sheets.add(new ExcelSheet<>("农事记录", farms, WmFarmRecord.class));
        sheets.add(new ExcelSheet<>("品控记录", qcs, WmQcRecord.class));
        ExcelUtil.exportMultiSheet(response, sheets);
    }

    /**
     * 批次归档（归档后农户不可再录入/修改）
     */
    @PreAuthorize("@ss.hasPermi('trace:batch:archive')")
    @Log(title = "批次归档", businessType = BusinessType.UPDATE)
    @PutMapping("/archive/{batchIds}")
    public AjaxResult archive(@PathVariable Long[] batchIds)
    {
        int rows = 0;
        for (Long id : batchIds)
        {
            WmBatch b = new WmBatch();
            b.setBatchId(id);
            b.setStatus("2");
            b.setUpdateBy(getUsername());
            rows += batchService.updateWmBatch(b);
        }
        return toAjax(rows);
    }
}
