package dto;

import exceptions.GeneralException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by K on 5/3/2016.
 */
public class Picture {

    private XYSeriesCollection seriesCollection;
    private JFreeChart chart;

    public Picture() {
        seriesCollection = new XYSeriesCollection();
    }

    public Picture addSeries(XYSeries series) {
        seriesCollection.addSeries(series);
        return this;
    }

    public void plotScatter(String xLabel, String yLabel, String pathToFile) throws GeneralException {
        final JFreeChart chart = ChartFactory.createScatterPlot(
                null
                , xLabel
                , yLabel
                , seriesCollection
                , PlotOrientation.VERTICAL
                , true
                , true
                , false
        );
        setAllChartParameters(chart);
        saveToFile(pathToFile, chart);
    }

    public void plotLine(String xLabel, String yLabel, String pathToFile) throws GeneralException {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                null
                , xLabel
                , yLabel
                , seriesCollection
                , PlotOrientation.VERTICAL
                , true
                , true
                , false
        );
        setAllChartParameters(chart);
        saveToFile(pathToFile, chart);
    }

    private void setAllChartParameters(JFreeChart chart) throws GeneralException {
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairVisible(true);

        ValueAxis bottomAxis = plot.getDomainAxis();
        ValueAxis leftAxis = plot.getRangeAxis();
        ValueAxis topAxis = null;
        ValueAxis rightAxis = null;

        try {
            topAxis = (ValueAxis) bottomAxis.clone();
            setAxisParameters(topAxis);
            topAxis.setTickLabelsVisible(false);
            topAxis.setRange(bottomAxis.getRange());
            topAxis.setLabel(null);
            plot.setDomainAxis(1, topAxis);
        } catch (Exception e) {
            throw new GeneralException("Negalima klonuoti", e);
        }

        try {
            rightAxis = (ValueAxis) plot.getRangeAxis().clone();
            setAxisParameters(rightAxis);
            rightAxis.setTickLabelsVisible(false);
            rightAxis.setRange(leftAxis.getRange());
            rightAxis.setLabel(null);
            plot.setRangeAxis(1, rightAxis);
        } catch (Exception e) {
            throw new GeneralException("Negalima klonuoti", e);
        }

        setAxisParameters(bottomAxis);
        setAxisParameters(leftAxis);
        leftAxis.setLabelAngle(Math.PI / 2);

        XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
            float length = i * 1.0f + 1.0f;
            renderer.setSeriesStroke(i, new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
                    , length, new float[]{length, 10.0f}, 0.0f));
            renderer.setSeriesPaint(i, new Color(0, 0, 0, 191));
        }
    }

    private void setAxisParameters(ValueAxis axis) {
        Stroke stroke = new BasicStroke(2);

        axis.setMinorTickMarksVisible(true);
        axis.setMinorTickMarkInsideLength(5);
        axis.setMinorTickMarkOutsideLength(0);
        axis.setTickMarksVisible(true);
        axis.setTickMarkInsideLength(10);
        axis.setTickMarkOutsideLength(0);
        axis.setAutoTickUnitSelection(true);
        axis.setAxisLineStroke(stroke);
        axis.setTickMarkStroke(stroke);
        axis.setMinorTickCount(4);
    }

    synchronized private void saveToFile(String path, JFreeChart chart) throws GeneralException {
        try {
            ImageIO.setUseCache(false);
            ImageEncoderFactory.setImageEncoder("png","org.jfree.chart.encoders.KeypointPNGEncoderAdapter");
            File file = new File(path);
            FileOutputStream os = new FileOutputStream(file);

            ChartUtilities.writeChartAsJPEG(os, chart, 800, 800);
            os.flush();
            os.close();
        } catch (Exception e) {
            throw new GeneralException("Chart could not be written to file!", e);
        }
    }


}
