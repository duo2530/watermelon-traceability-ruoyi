package com.ruoyi.trace.mapper;

import java.util.List;
import com.ruoyi.trace.domain.WmPlot;

/**
 * 地块Mapper接口
 *
 * @author ruoyi
 */
public interface WmPlotMapper
{
    public WmPlot selectWmPlotById(Long plotId);

    public List<WmPlot> selectWmPlotList(WmPlot plot);

    public int insertWmPlot(WmPlot plot);

    public int updateWmPlot(WmPlot plot);

    public int deleteWmPlotById(Long plotId);

    public int deleteWmPlotByIds(Long[] plotIds);

    public int countBatchByPlotIds(Long[] plotIds);
}
