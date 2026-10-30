package com.ruoyi.trace.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
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
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.domain.WmQcRecord;
import com.ruoyi.trace.service.IWmBatchService;
import com.ruoyi.trace.service.IWmGradeService;
import com.ruoyi.trace.service.IWmQcRecordService;
import com.ruoyi.trace.service.impl.WmSyncServiceImpl;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 品控记录Controller（糖度 / 果型分级）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/qc")
public class WmQcRecordController extends BaseController
{
    @Autowired
    private IWmQcRecordService qcRecordService;

    @Autowired
    private IWmBatchService batchService;

    @Autowired
    private IWmGradeService gradeService;

    @PreAuthorize("@ss.hasPermi('trace:qc:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmQcRecord record)
    {
        record.setFarmerId(TraceDataScope.farmerScope());
        startPage();
        List<WmQcRecord> list = qcRecordService.selectWmQcRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('trace:qc:export')")
    @Log(title = "品控记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmQcRecord record)
    {
        record.setFarmerId(TraceDataScope.farmerScope());
        List<WmQcRecord> list = qcRecordService.selectWmQcRecordList(record);
        ExcelUtil<WmQcRecord> util = new ExcelUtil<WmQcRecord>(WmQcRecord.class);
        util.exportExcel(response, list, "品控记录");
    }

    @PreAuthorize("@ss.hasPermi('trace:qc:query')")
    @GetMapping(value = "/{qcId}")
    public AjaxResult getInfo(@PathVariable("qcId") Long qcId)
    {
        return success(load(qcId));
    }

    /**
     * 预判等级（录入表单实时提示用，不落库）
     */
    @PreAuthorize("@ss.hasPermi('trace:qc:add')")
    @GetMapping("/evaluate")
    public AjaxResult evaluate(@RequestParam(required = false) Long batchId, @RequestParam BigDecimal sugarCenter,
            @RequestParam(required = false) BigDecimal sugarEdge, @RequestParam(required = false) String fruitShape,
            @RequestParam(required = false) BigDecimal weightKg)
    {
        String variety = null;
        if (batchId != null)
        {
            WmBatch batch = batchService.selectWmBatchById(batchId);
            variety = batch != null ? batch.getVariety() : null;
        }
        WmGradeStandard g = gradeService.evaluate(variety, sugarCenter, sugarEdge, fruitShape, weightKg);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("gradeCode", g != null ? g.getGradeCode() : "NONE");
        ajax.put("gradeName", g != null ? g.getGradeName() : "等外");
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('trace:qc:add')")
    @Log(title = "品控记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmQcRecord record)
    {
        checkBatch(record.getBatchId());
        WmSyncServiceImpl.checkSugar(record.getSugarCenter(), true);
        WmSyncServiceImpl.checkSugar(record.getSugarEdge(), false);
        record.setClientId(null);
        if (StringUtils.isEmpty(record.getInspector()))
        {
            record.setInspector(getLoginUser().getUser().getNickName());
        }
        record.setCreateBy(getUsername());
        qcRecordService.insertWmQcRecord(record);
        return success(record);
    }

    @PreAuthorize("@ss.hasPermi('trace:qc:edit')")
    @Log(title = "品控记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmQcRecord record)
    {
        load(record.getQcId());
        checkBatch(record.getBatchId());
        WmSyncServiceImpl.checkSugar(record.getSugarCenter(), true);
        WmSyncServiceImpl.checkSugar(record.getSugarEdge(), false);
        record.setUpdateBy(getUsername());
        return toAjax(qcRecordService.updateWmQcRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('trace:qc:remove')")
    @Log(title = "品控记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{qcIds}")
    public AjaxResult remove(@PathVariable Long[] qcIds)
    {
        for (Long id : qcIds)
        {
            WmQcRecord r = load(id);
            checkBatch(r.getBatchId());
        }
        return toAjax(qcRecordService.deleteWmQcRecordByIds(qcIds));
    }

    private WmQcRecord load(Long qcId)
    {
        WmQcRecord r = qcRecordService.selectWmQcRecordById(qcId);
        if (r == null)
        {
            throw new ServiceException("品控记录不存在");
        }
        TraceDataScope.checkOwner(r.getFarmerId());
        return r;
    }

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
