package service.impl;


import dao.PacketDao;
import dto.PacketsInfo;
import dto.StorageByDestinationInTimeDomain;
import dto.ValueInTimeInterval;
import dto.mutualInformation.MutualInformationTable;
import entities.Packet;
import exceptions.GeneralException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.DataService;

import javax.transaction.Transactional;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("dataService")
public class DataServiceImpl implements DataService {

    @Autowired
    private PacketDao packetDao;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    @Override
    @Transactional
    public void uploadFileToDatabase(String[] fileNames) throws GeneralException {
        int id = packetDao.getMaxPacketId() + 1;
        for (int i = 0, j = 1; i < fileNames.length; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileNames[i]));
                String line;
                List<Packet> temp = new ArrayList<Packet>();
                while ((line = br.readLine()) != null) {
                    /* Gaunam paketą iš eilutės */
                    Packet p = getPacket(line, i);
                    p.setId(id);
                    temp.add(p);

                    if (j % 1000 == 0) {
                        packetDao.insertPackets(temp);
                        temp = new ArrayList<Packet>();
                    }
                    j++; id++;
                }
                packetDao.insertPackets(temp);
            } catch (Exception e) {
                throw new GeneralException("Could not upload file to the database!", e);
            }
        }
    }

    @Override
    @Transactional
    public String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException {
        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        /* Pasiemame entropijos reikšmes */
        List<ValueInTimeInterval> valuesInTimeIntervals = storage.getListOfEntropies();
        StringBuilder result = new StringBuilder();
        for (ValueInTimeInterval v: valuesInTimeIntervals) {
            result.append(v.getTime()).append("\t").append(v.getValue()).append("\n");
        }
        return result.toString();
    }

    @Override
    public String getMutualInformation(List<Double> currentValues, List<Double> shiftedValues, int numberOfItems) throws GeneralException {
        MutualInformationTable table = new MutualInformationTable(currentValues, shiftedValues);
        return String.valueOf(table.getMutualInformation());
    }

    @Override
    public String getMutualInformationList(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, Integer dimension) throws GeneralException {
        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        List<Double> mutualInformationList = new ArrayList<Double>();
        List<ValueInTimeInterval> valuesInTimeIntervals = storage.getListOfEntropies();
        List<Double> firstList = convertToDoubleList(valuesInTimeIntervals.subList(0, dimension));

        XYSeries series = new XYSeries("First");
        for (int i = 0; i < 200; i ++) {
            List<Double> secondList = convertToDoubleList(valuesInTimeIntervals.subList(i, i + dimension));
            Double mutualInformation = new MutualInformationTable(firstList, secondList).getMutualInformation();
            mutualInformationList.add(mutualInformation);
            series.add(i, mutualInformation);
        }

        plot(series);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mutualInformationList.size(); i++) {
            result.append(i).append("\t").append(mutualInformationList.get(i)).append("\n");
        }
        return result.toString();
    }

    private void plot(XYSeries series) throws GeneralException {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                null
                , "Laiko postūmis"
                , "I"
                , seriesCollection
                , PlotOrientation.VERTICAL
                , true
                , true
                , false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);

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
        leftAxis.setLabelAngle(3.14 / 2);



        saveToFile("C:\\Users\\K\\Desktop\\Bakalauras\\latex\\paveiksleliai\\xxx.png", chart);
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
        axis.setMinorTickCount(1);
    }

    private void saveToFile(String path, JFreeChart chart) throws GeneralException {
        try {
            File file = new File(path);
            FileOutputStream os = new FileOutputStream(file);
            ChartUtilities.writeChartAsPNG(os, chart, 800, 800);
            os.flush();
            os.close();
        } catch (Exception e) {
            throw new GeneralException("Chart could not be written to file!", e);
        }
    }

    private List<Double> convertToDoubleList(List<ValueInTimeInterval> valueInTimeIntervalList) {
        List<Double> doubleList = new ArrayList<Double>();
        for (ValueInTimeInterval valueInTimeInterval: valueInTimeIntervalList) {
            doubleList.add(valueInTimeInterval.getValue());
        }
        return doubleList;
    }

    private StorageByDestinationInTimeDomain getStorageWithCalculatedEntropy(List<PacketsInfo> packetsInfo, int windowWidth) {
        StorageByDestinationInTimeDomain storage = new StorageByDestinationInTimeDomain();

        if (packetsInfo != null && !packetsInfo.isEmpty()) { //Tiesiog patikrinam ar grįžo kas nors iš duombazės
            storage.setWindowWidth(windowWidth); //Nustatome lango plotį, kitaip būtų naudojamas defaultinis

            for (int i = 0; i < packetsInfo.size(); i++) { //Važiuojam per paketų informaciją saugojančius objektus
                PacketsInfo pi = packetsInfo.get(i);

                /* Jeigu apdorojamo objekto laikas pasikeitė, tarkim visi prieš tai apdorojami paketai
                * buvo iš pirmos sekundės, o dabar jau gavome paketą iš antros sekundės, tai dabartinio
                * interalo paketus išsisaugojame atminyje, ir išvalome dabartinio laiko paketų masyvą,
                * kad galėtumėme saugoti naujus paketus */
                if (storage.timeExceedsCurrentTime(pi.getTime())) {
                    storage
                            .addCurrentIntervalToStorage()
                            .cleanCurrentInterval();
                }

                /* Tiesiog paduodame dabartinį objektą apdorojimui */
                storage.addNewPacketInfo(pi);
            }
        }
        return storage;
    }

    @Override
    public String getEntropyAgainstEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, Integer goBack) throws GeneralException {
        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        /* Pasiemame entropijos reikšmes */
        List<ValueInTimeInterval> valuesInTimeIntervals = storage.getListOfEntropies();

        int embeddingDimension = 7;
        List<Double> parameterValues = getParameterValues(embeddingDimension);
        StringBuilder result = new StringBuilder();

        Double predictedValue = 0.0;
        for (int i = embeddingDimension; i < valuesInTimeIntervals.size(); i++) {
            Double currentValue = valuesInTimeIntervals.get(i).getValue();
            for (int j = 0; j < embeddingDimension; j++) {
                Double previousValue = valuesInTimeIntervals.get(i - j - 1).getValue();
                Double parameterValue = parameterValues.get(j);
                predictedValue += previousValue * parameterValue;
            }

            //String time = simpleDateFormat.format();
            String line = getLine("\t", String.valueOf(valuesInTimeIntervals.get(i).getTime().getTime()), decimalFormat.format(currentValue), decimalFormat.format(predictedValue));
            result.append(line).append("\n");

            predictedValue = 0.0;
        }

        return result.toString();
    }

    private String getLine(String separator, String... columns) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                line.append(separator);
            }
            line.append(columns[i]);
        }
        return line.toString();
    }

    private List<Double> getParameterValues(int dimensionCount) {
        List<Double> parameterValues = new ArrayList<Double>();
        Double parameterValue = 0.5;
        Double multiplier = 0.5;
        Double leftOver = 1.0;
        for (int i = 0; i < dimensionCount; i++) {
            if (i + 1 < dimensionCount) {
                parameterValues.add(parameterValue);
                leftOver -= parameterValue;
                parameterValue *= multiplier;
            } else {
                parameterValues.add(leftOver);
            }
        }
        return parameterValues;
    }

    private Packet getPacket(String line, int fileIndex) throws GeneralException {
        /* Suskaidom eilutę pagal "," */
        String[] cols = line.split("\\\",\\\"");

        /* Nuo pirmo stulpelio pašalinam kabutes*/
        cols[0] = cols[0].substring(1);

        /* Nuo paskutinio stulpelio pašalinam kabutes*/
        cols[cols.length - 1] = cols[cols.length - 1].substring(0, cols[cols.length - 1].length() - 1);

        Packet packet = new Packet();

        // Stulpelių indeksai ---> [NR, LAIKAS, ŠALTINIO IP, PASKIRTIES IP, PROTOKOLAS]
        int timestampColIndex = 1;
        int sourceColIndex = 2;
        int destinationColIndex = 3;
        int protocolColIndex = 4;

        Timestamp timestamp = getTimestampFromString(cols[timestampColIndex]);
        packet.setTimestamp(timestamp);
        packet.setSource(cols[sourceColIndex]);
        packet.setDestination(cols[destinationColIndex]);
        packet.setProtocol(cols[protocolColIndex]);

        return packet;
    }

    private Timestamp getTimestampFromString(String timestampString) throws GeneralException {
        try {
            String[] parts = timestampString.split("\\.");
            Date parsedDate = simpleDateFormat.parse(parts[0]);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            timestamp.setNanos(Integer.parseInt(parts[1]) * 1000);
            return timestamp;
        } catch (Exception e) {
            throw new GeneralException("Blogas datos formatas!", e);
        }
    }

}
