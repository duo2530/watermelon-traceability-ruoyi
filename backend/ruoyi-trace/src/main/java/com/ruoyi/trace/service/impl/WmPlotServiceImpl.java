package com.ruoyi.trace.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trace.domain.WmPlot;
import com.ruoyi.trace.mapper.WmPlotMapper;
import com.ruoyi.trace.service.IWmPlotService;

/**
 * 地块Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WmPlotServiceImpl implements IWmPlotService
{
    @Autowired
    private WmPlotMapper plotMapper;

    @Override
    public WmPlot selectWmPlotById(Long plotId)
    {
        return plotMapper.selectWmPlotById(plotId);
    }

    @Override
    public List<WmPlot> selectWmPlotList(WmPlot plot)
    {
        return plotMapper.selectWmPlotList(plot);
    }

    @Override
    public int insertWmPlot(WmPlot plot)
    {
        return plotMapper.insertWmPlot(plot);
    }

    @Override
    public int updateWmPlot(WmPlot plot)
    {
        return plotMapper.updateWmPlot(plot);
    }

    @Override
    public int deleteWmPlotByIds(Long[] plotIds)
    {
        return plotMapper.deleteWmPlotByIds(plotIds);
    }
}
