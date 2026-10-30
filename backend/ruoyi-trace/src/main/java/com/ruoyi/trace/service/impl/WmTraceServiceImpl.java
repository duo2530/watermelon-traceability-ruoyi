package com.ruoyi.trace.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trace.domain.WmBatch;
import com.ruoyi.trace.domain.WmFarmRecord;
import com.ruoyi.trace.domain.WmPlot;
import com.ruoyi.trace.domain.WmQcRecord;
import com.ruoyi.trace.mapper.WmBatchMapper;
import com.ruoyi.trace.mapper.WmFarmRecordMapper;
import com.ruoyi.trace.mapper.WmPlotMapper;
import com.ruoyi.trace.mapper.WmQcRecordMapper;
import com.ruoyi.trace.mapper.WmStatsMapper;
import com.ruoyi.trace.service.IWmTraceService;

/**
 * 溯源查询 / 统计实现
 *
 * @author ruoyi
 */
@Service
public class WmTraceServiceImpl implements IWmTraceService
{
    @Autowired
    private WmBatchMapper batchMapper;

    @Autowired
    private WmPlotMapper plotMapper;

    @Autowired
    private WmFarmRecordMapper farmRecordMapper;

    @Autowired
    private WmQcRecordMapper qcRecordMapper;

    @Autowired
    private WmStatsMapper statsMapper;

    @Override
    public Map<String, Object> publicTrace(String batchCode)
    {
        WmBatch batch = batchMapper.selectWmBatchByCode(batchCode);
        if (batch == null)
        {
            return null;
        }
        batchMapper.increaseScanCount(batch.getBatchId());

        Map<String, Object> result = new LinkedHashMap<>();
        // 只暴露公开字段，不返回农户ID、创建人等内部信息
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("batchCode", batch.getBatchCode());
        b.put("variety", batch.getVariety());
        b.put("plantDate", batch.getPlantDate());
        b.put("harvestDate", batch.getHarvestDate());
        b.put("status", batch.getStatus());
        b.put("statusLabel", DictUtils.getDictLabel("wm_batch_status", batch.getStatus()));
        b.put("coverImage", batch.getCoverImage());
        b.put("scanCount", (batch.getScanCount() == null ? 0 : batch.getScanCount()) + 1);
        b.put("farmerName", maskName(batch.getFarmerName()));
        result.put("batch", b);

        Map<String, Object> brand = new LinkedHashMap<>();
        brand.put("name", batch.getBrandName());
        brand.put("logo", batch.getBrandLogo());
        brand.put("desc", batch.getBrandDesc());
        result.put("brand", brand);

        if (batch.getPlotId() != null)
        {
            WmPlot plot = plotMapper.selectWmPlotById(batch.getPlotId());
            if (plot != null)
            {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("plotName", plot.getPlotName());
                p.put("address", plot.getAddress());
                p.put("areaMu", plot.getAreaMu());
                p.put("soilType", plot.getSoilType());
                p.put("longitude", plot.getLongitude());
                p.put("latitude", plot.getLatitude());
                result.put("plot", p);
            }
        }

        WmFarmRecord fq = new WmFarmRecord();
        fq.setBatchId(batch.getBatchId());
        List<WmFarmRecord> farms = farmRecordMapper.selectWmFarmRecordList(fq);
        List<Map<String, Object>> timeline = new ArrayList<>();
        // 列表按时间倒序，时间线按正序展示
        for (int i = farms.size() - 1; i >= 0; i--)
        {
            WmFarmRecord r = farms.get(i);
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("recordType", r.getRecordType());
            t.put("recordTypeLabel", DictUtils.getDictLabel("wm_farm_type", r.getRecordType()));
            t.put("content", r.getContent());
            t.put("inputName", r.getInputName());
            t.put("inputAmount", r.getInputAmount());
            t.put("recordTime", r.getRecordTime());
            t.put("weather", r.getWeather());
            t.put("images", splitImages(r.getImages()));
            timeline.add(t);
        }
        result.put("timeline", timeline);

        WmQcRecord qq = new WmQcRecord();
        qq.setBatchId(batch.getBatchId());
        List<WmQcRecord> qcs = qcRecordMapper.selectWmQcRecordList(qq);
        result.put("quality", summarizeQc(qcs));
        return result;
    }

    private Map<String, Object> summarizeQc(List<WmQcRecord> qcs)
    {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("sampleCount", qcs.size());
        if (qcs.isEmpty())
        {
            return q;
        }
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;
        Map<String, Integer> gradeCount = new LinkedHashMap<>();
        List<Map<String, Object>> samples = new ArrayList<>();
        for (WmQcRecord r : qcs)
        {
            BigDecimal s = r.getSugarCenter();
            sum = sum.add(s);
            max = max == null || s.compareTo(max) > 0 ? s : max;
            min = min == null || s.compareTo(min) < 0 ? s : min;
            String g = StringUtils.isEmpty(r.getGradeName()) ? "等外" : r.getGradeName();
            gradeCount.merge(g, 1, Integer::sum);
            if (samples.size() < 10)
            {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sampleNo", r.getSampleNo());
                m.put("sugarCenter", r.getSugarCenter());
                m.put("sugarEdge", r.getSugarEdge());
                m.put("fruitShape", DictUtils.getDictLabel("wm_fruit_shape", r.getFruitShape()));
                m.put("weightKg", r.getWeightKg());
                m.put("gradeName", g);
                m.put("checkTime", r.getCheckTime());
                m.put("images", splitImages(r.getImages()));
                samples.add(m);
            }
        }
        q.put("avgSugar", sum.divide(new BigDecimal(qcs.size()), 1, RoundingMode.HALF_UP));
        q.put("maxSugar", max);
        q.put("minSugar", min);
        // 主等级 = 样本数最多的等级
        String main = null;
        int best = -1;
        for (Map.Entry<String, Integer> e : gradeCount.entrySet())
        {
            if (e.getValue() > best)
            {
                best = e.getValue();
                main = e.getKey();
            }
        }
        q.put("mainGrade", main);
        q.put("gradeCount", gradeCount);
        q.put("samples", samples);
        return q;
    }

    private static List<String> splitImages(String images)
    {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(images))
        {
            for (String s : images.split(","))
            {
                if (StringUtils.isNotBlank(s))
                {
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    private static String maskName(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return "";
        }
        return name.substring(0, 1) + "**";
    }

    @Override
    public Map<String, Object> stats(Long farmerId, String variety, String beginTime, String endTime)
    {
        WmQcRecord query = new WmQcRecord();
        query.setFarmerId(farmerId);
        query.setVariety(variety);
        query.getParams().put("beginTime", beginTime);
        query.getParams().put("endTime", endTime);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("overview", statsMapper.selectOverview(query));
        r.put("gradeDistribution", statsMapper.selectGradeDistribution(query));
        r.put("sugarDistribution", statsMapper.selectSugarDistribution(query));
        r.put("monthlyTrend", statsMapper.selectMonthlyTrend(query));
        r.put("varietySummary", statsMapper.selectVarietySummary(query));
        r.put("batchSummary", statsMapper.selectBatchSummary(query));
        return r;
    }
}
