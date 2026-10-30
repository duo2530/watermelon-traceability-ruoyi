package com.ruoyi.trace.controller;

import java.util.List;
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
import com.ruoyi.trace.domain.WmGradeStandard;
import com.ruoyi.trace.service.IWmGradeStandardService;

/**
 * 分级标准Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/trace/grade")
public class WmGradeStandardController extends BaseController
{
    @Autowired
    private IWmGradeStandardService gradeStandardService;

    @PreAuthorize("@ss.hasPermi('trace:grade:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmGradeStandard standard)
    {
        startPage();
        List<WmGradeStandard> list = gradeStandardService.selectWmGradeStandardList(standard);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('trace:grade:query')")
    @GetMapping(value = "/{standardId}")
    public AjaxResult getInfo(@PathVariable("standardId") Long standardId)
    {
        return success(gradeStandardService.selectWmGradeStandardById(standardId));
    }

    @PreAuthorize("@ss.hasPermi('trace:grade:add')")
    @Log(title = "分级标准", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmGradeStandard standard)
    {
        String err = validate(standard);
        if (err != null)
        {
            return error(err);
        }
        standard.setCreateBy(getUsername());
        return toAjax(gradeStandardService.insertWmGradeStandard(standard));
    }

    @PreAuthorize("@ss.hasPermi('trace:grade:edit')")
    @Log(title = "分级标准", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmGradeStandard standard)
    {
        String err = validate(standard);
        if (err != null)
        {
            return error(err);
        }
        standard.setUpdateBy(getUsername());
        return toAjax(gradeStandardService.updateWmGradeStandard(standard));
    }

    @PreAuthorize("@ss.hasPermi('trace:grade:remove')")
    @Log(title = "分级标准", businessType = BusinessType.DELETE)
    @DeleteMapping("/{standardIds}")
    public AjaxResult remove(@PathVariable Long[] standardIds)
    {
        return toAjax(gradeStandardService.deleteWmGradeStandardByIds(standardIds));
    }

    private String validate(WmGradeStandard s)
    {
        if (s.getWeightMin() != null && s.getWeightMax() != null && s.getWeightMin().compareTo(s.getWeightMax()) > 0)
        {
            return "单果重下限不能大于上限";
        }
        if (s.getShapeAllow() != null && !s.getShapeAllow().matches("^[0-9,]*$"))
        {
            return "允许果型格式应为逗号分隔的字典值，如 1,2";
        }
        return null;
    }
}
