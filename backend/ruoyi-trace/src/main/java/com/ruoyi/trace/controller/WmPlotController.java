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
import com.ruoyi.trace.domain.WmPlot;
import com.ruoyi.trace.service.IWmPlotService;
import com.ruoyi.trace.mapper.WmPlotMapper;
import com.ruoyi.trace.util.TraceDataScope;

/**
 * 地块Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/plot")
public class WmPlotController extends BaseController
{
    @Autowired
    private IWmPlotService plotService;

    @Autowired
    private WmPlotMapper plotMapper;

    @PreAuthorize("@ss.hasPermi('trace:plot:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmPlot plot)
    {
        scope(plot);
        startPage();
        List<WmPlot> list = plotService.selectWmPlotList(plot);
        return getDataTable(list);
    }

    /**
     * 下拉选项（不分页）
     */
    @PreAuthorize("@ss.hasPermi('trace:plot:list')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        WmPlot plot = new WmPlot();
        plot.setStatus("0");
        scope(plot);
        return success(plotService.selectWmPlotList(plot));
    }

    @PreAuthorize("@ss.hasPermi('trace:plot:export')")
    @Log(title = "地块", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmPlot plot)
    {
        scope(plot);
        List<WmPlot> list = plotService.selectWmPlotList(plot);
        ExcelUtil<WmPlot> util = new ExcelUtil<WmPlot>(WmPlot.class);
        util.exportExcel(response, list, "地块数据");
    }

    @PreAuthorize("@ss.hasPermi('trace:plot:query')")
    @GetMapping(value = "/{plotId}")
    public AjaxResult getInfo(@PathVariable("plotId") Long plotId)
    {
        return success(load(plotId));
    }

    @PreAuthorize("@ss.hasPermi('trace:plot:add')")
    @Log(title = "地块", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmPlot plot)
    {
        Long scope = TraceDataScope.farmerScope();
        if (scope != null || plot.getFarmerId() == null)
        {
            plot.setFarmerId(getUserId());
        }
        plot.setCreateBy(getUsername());
        return toAjax(plotService.insertWmPlot(plot));
    }

    @PreAuthorize("@ss.hasPermi('trace:plot:edit')")
    @Log(title = "地块", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmPlot plot)
    {
        load(plot.getPlotId());
        if (TraceDataScope.farmerScope() != null)
        {
            // 农户不能把地块转给他人
            plot.setFarmerId(null);
        }
        plot.setUpdateBy(getUsername());
        return toAjax(plotService.updateWmPlot(plot));
    }

    @PreAuthorize("@ss.hasPermi('trace:plot:remove')")
    @Log(title = "地块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{plotIds}")
    public AjaxResult remove(@PathVariable Long[] plotIds)
    {
        for (Long id : plotIds)
        {
            load(id);
        }
        if (plotMapper.countBatchByPlotIds(plotIds) > 0)
        {
            return error("地块下存在种植批次，不能删除");
        }
        return toAjax(plotService.deleteWmPlotByIds(plotIds));
    }

    private void scope(WmPlot plot)
    {
        Long scope = TraceDataScope.farmerScope();
        if (scope != null)
        {
            plot.setFarmerId(scope);
        }
    }

    private WmPlot load(Long plotId)
    {
        WmPlot plot = plotService.selectWmPlotById(plotId);
        if (plot == null)
        {
            throw new ServiceException("地块不存在");
        }
        TraceDataScope.checkOwner(plot.getFarmerId());
        return plot;
    }
}
