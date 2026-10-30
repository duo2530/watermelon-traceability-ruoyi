package com.ruoyi.trace.service;

import java.util.List;
import com.ruoyi.trace.domain.WmPlot;

/**
 * 地块Service接口
 *
 * @author ruoyi
 */
public interface IWmPlotService
{
    public WmPlot selectWmPlotById(Long plotId);

    public List<WmPlot> selectWmPlotList(WmPlot plot);

    public int insertWmPlot(WmPlot plot);

    public int updateWmPlot(WmPlot plot);

    public int deleteWmPlotByIds(Long[] plotIds);
}
