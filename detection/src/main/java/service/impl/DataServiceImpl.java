package service.impl;


import dao.PacketDao;
import dto.PacketsInfo;
import dto.Picture;
import dto.StorageByDestinationInTimeDomain;
import dto.ValueInTimeInterval;
import dto.mutualInformation.MutualInformationTable;
import entities.Packet;
import exceptions.GeneralException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
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
import java.util.Collections;
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
                    j++;
                    id++;
                }
                packetDao.insertPackets(temp);
            } catch (Exception e) {
                throw new GeneralException("Could not upload file to the database!", e);
            }
        }
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

    @Override
    @Transactional
    public String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException {
        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        /* Pasiemame entropijos reikšmes */
        List<ValueInTimeInterval> valuesInTimeIntervals = storage.getListOfEntropies();
        StringBuilder result = new StringBuilder();
        for (ValueInTimeInterval v : valuesInTimeIntervals) {
            result.append(v.getTime()).append("\t").append(v.getValue()).append("\n");
        }
        return result.toString();
    }

    @Override
    public String calculateMutualInformationReturnOptimalTimeDelay(Timestamp start, Timestamp end, Integer increment
            , Integer windowWidth, List<Integer> pointCountList) throws GeneralException {
        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        List<Double> valueList = convertToDoubleList(storage.getListOfEntropies());

        StringBuilder result = new StringBuilder();
        List<Integer> localMinimumList = new ArrayList<Integer>();
        int pointsToCalculate = 100;
        Picture mutualEntropyGraph = new Picture();
        for (int i = 0; i < pointCountList.size(); i++) {
            XYSeries series = getSeries(pointCountList.get(i), pointsToCalculate, valueList);
            mutualEntropyGraph.addSeries(series);

            Double firstLocalMinimum = getFirstLocalMinimum(series.getItems());
            localMinimumList.add(firstLocalMinimum.intValue());
            result.append(getLine("\t", String.valueOf(pointCountList.get(i)), String.valueOf(firstLocalMinimum))).append("\n");
        }

        mutualEntropyGraph.plotLine("X", "Y", "C:\\Users\\K\\Desktop\\Bakalauras\\latex\\paveiksleliai\\mutual_information.png");

        for (int i = 0; i < pointCountList.size(); i++) {
            Picture phaseSpace = new Picture();
            XYSeries phaseSpaceSeries = new XYSeries(pointCountList.get(i));
            for (int j = 0; j < pointCountList.get(i); j++) {
                Double x = valueList.get(j);
                Double y = valueList.get(j + localMinimumList.get(i));
                phaseSpaceSeries.add(x, y);
            }
            phaseSpace.addSeries(phaseSpaceSeries);
            phaseSpace.plotScatter("X", "Y", "C:\\Users\\K\\Desktop\\Bakalauras\\latex\\paveiksleliai\\phase_space_" + pointCountList.get(i) + ".png");
        }

        return result.toString();
    }

    private Double getFirstLocalMinimum(List<XYDataItem> items) {
        Double lastValue = items.get(0).getYValue();
        for (int i = 0; i < items.size(); i++) {
            Double value = items.get(i).getYValue();
            if (value > lastValue) {
                return items.get(i).getXValue();
            }
            lastValue = value;
        }
        return 0.0;
    }

    private XYSeries getSeries(int dimension, int pointsToCalculate, List<Double> valueList) throws GeneralException {
        List<Double> firstList = valueList.subList(0, dimension);

        XYSeries series = new XYSeries(String.valueOf(dimension));
        for (int i = 0; i < pointsToCalculate; i++) {
            List<Double> secondList = valueList.subList(i, i + dimension);
            Double mutualInformation = new MutualInformationTable(firstList, secondList).getMutualInformation();
            series.add(i, mutualInformation);
        }

        return series;
    }

    private List<Double> convertToDoubleList(List<ValueInTimeInterval> valueInTimeIntervalList) {
        List<Double> doubleList = new ArrayList<Double>();
        for (ValueInTimeInterval valueInTimeInterval : valueInTimeIntervalList) {
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

    @Override
    public String getPredictionParams(Timestamp start, Timestamp end, Integer increment, Integer windowWidth
    , Integer dimensionCount, List<Integer> pointCount, Integer optimalTimeDelay) throws GeneralException {

        List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
        StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        List<Double> valueList = convertToDoubleList(storage.getListOfEntropies());

        Integer pointsToPredict = 100;
        Double startAt = 0.8;

        XYSeries realValueSeries = new XYSeries(pointsToPredict);
        XYSeries predictedValueSeries = new XYSeries(pointsToPredict);

        Integer pointCountas = pointCount.get(0);

        Integer size = pointCountas + optimalTimeDelay * dimensionCount;
        Integer startIndex = new Double(size * startAt).intValue();
        List<Double> valueSubList = new ArrayList<Double>(valueList.subList(0, size));

        for (int i = startIndex; i < size; i ++) {
            realValueSeries.add(i, valueSubList.get(i));
            predictedValueSeries.add(i, valueSubList.get(i));
        }
        for (int i = 0; i < pointsToPredict; i++) {
            Double predictedValue = getPredictedValue(valueSubList, dimensionCount, pointCountas, optimalTimeDelay);
            Double nextRealValue = valueList.get(size - 1 + i);

            realValueSeries.add(i + size, nextRealValue);
            predictedValueSeries.add(i + size, predictedValue);

            valueSubList.remove(0);
            valueSubList.add(predictedValue);
        }

        Picture picture = new Picture();
        picture.addSeries(realValueSeries);
        picture.addSeries(predictedValueSeries);
        picture.plotLine("X", "Y", "C:\\Users\\K\\Desktop\\Bakalauras\\latex\\paveiksleliai\\predicted.png");

        return "SUCCESS";
    }

    private Double getPredictedValue(List<Double> valueList, Integer dimensionCount, Integer pointCountas, Integer optimalTimeDelay) throws GeneralException {
        Integer startingPosition = optimalTimeDelay * dimensionCount;
        List<List<Double>> matrixBNestedLists = new ArrayList<List<Double>>();
        List<List<Double>> matrixDNestedLists = new ArrayList<List<Double>>();
        List<List<Double>> matrixY2NestedLists = new ArrayList<List<Double>>();

        for (int i = 0; i < dimensionCount + 1; i++) {
            List<Double> row = new ArrayList<Double>();
            for (int j = 0; j < pointCountas; j++) {
                Double value;
                if (i == 0) {
                    value = 1.0;
                } else {
                    value = valueList.get(startingPosition - ((i - 1) * optimalTimeDelay) + j);
                }
                row.add(value);
                if (j == pointCountas - 1) {
                    matrixY2NestedLists.add(Collections.singletonList(value));
                }
            }
            matrixBNestedLists.add(row);

            if (i == 1) {
                List<Double> predictionRow = new ArrayList<Double>(row.subList(1, row.size()));
                predictionRow.add(valueList.get(startingPosition + pointCountas - 1));
                matrixDNestedLists.add(predictionRow);
            }
        }

        double[][] matrixB = nestedListsToMatrix(matrixBNestedLists);
        double[][] invertedMatrixB;
        try {
            invertedMatrixB = inverseMatrix(matrixB);
        } catch (Exception e) {
            throw new GeneralException("xx", e);
        }
        double[][] matrixD = nestedListsToMatrix(matrixDNestedLists);
        double[][] coefficientMartixA = multiplyMatrixes(matrixD, invertedMatrixB);
        double[][] matrixY2 = nestedListsToMatrix(matrixY2NestedLists);

        return multiplyMatrixes(coefficientMartixA, matrixY2)[0][0];
    }

    private double[][] nestedListsToMatrix(List<List<Double>> nestedLists) {
        double[][] matrix = new double[nestedLists.size()][];

        int i = 0;
        for (List<Double> list : nestedLists) {
            double[] row = new double[list.size()];
            int j = 0;
            for (Double value: list) {
                row[j++] = value.doubleValue();
            }
            matrix[i++] = row;
        }

        return matrix;
    }

    private double[][] inverseMatrix(double[][] matrix) {
        RealMatrix realMatrix = new BlockRealMatrix(matrix);
        RealMatrix inverse = new QRDecomposition(realMatrix).getSolver().getInverse();

        double[][] inverseData = inverse.getData();
        return inverseData;
    }

    private double[][] multiplyMatrixes(double[][] first, double[][] second) {
        RealMatrix firstMatrix = new BlockRealMatrix(first);
        RealMatrix secondMatrix = new BlockRealMatrix(second);

        return firstMatrix.multiply(secondMatrix).getData();
    }

    private String matrixToString(double[][] data) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                result.append("\t").append(data[i][j]);
            }
            result.append("\n");
        }
        return result.toString();
    }
}
