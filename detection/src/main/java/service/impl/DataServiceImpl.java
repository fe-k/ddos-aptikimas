package service.impl;


import dao.PacketDao;
import dto.PacketsInfo;
import dto.Picture;
import dto.StorageByDestinationInTimeDomain;
import dto.ValueInTimeInterval;
import dto.mutualInformation.MutualInformationTable;
import dto.neighboarPoints.Checker;
import dto.neighboarPoints.KDTree;
import entities.Packet;
import exceptions.GeneralException;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.linear.*;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.DataService;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service("dataService")
public class DataServiceImpl implements DataService {

    @Autowired
    private PacketDao packetDao;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DecimalFormat decimalFormat = new DecimalFormat("0.0000");
    private String pictureDirectory = "C:\\Users\\K\\Desktop\\Bakalauras\\latex\\paveiksleliai\\";

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

    @Override
    @Transactional
    public void insertDDoSAttack(Timestamp start, Timestamp end, String destination) throws GeneralException {
        long step = 50; //miliseconds
        int id = packetDao.getMaxPacketId() + 1;
        List<String> sources = packetDao.getPacketSources(100);

        Timestamp iterator = start;
        while (iterator.getTime() < end.getTime()) {
            List<Packet> packetsToInsert = new ArrayList<Packet>();
            for (String source : sources) {
                Packet packet = new Packet(id++, iterator, source, destination, "DDOS");
                packetsToInsert.add(packet);
                iterator = new Timestamp(iterator.getTime() + step);
            }
            packetDao.insertPackets(packetsToInsert);
        }

    }

    @Override
    @Transactional
    public void removeDDoSAttacks() {
        packetDao.deleteDDoSPackets();
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
        int[] increments = new int[]{250, 500, 1000};
        int[] windowWidths = new int[]{10, 15, 20, 25, 30};
        for (int inc = 0; inc < increments.length; inc++) {
            for (int wid = 0; wid < windowWidths.length; wid++) {
                increment = increments[inc];
                windowWidth = windowWidths[wid];

                List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
                StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);

        /* Pasiemame entropijos reikšmes */
                List<ValueInTimeInterval> valuesInTimeIntervals = storage.getListOfEntropies();
                XYSeries entropySeries = new XYSeries(valuesInTimeIntervals.size());
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < valuesInTimeIntervals.size(); i++) {
                    ValueInTimeInterval v = valuesInTimeIntervals.get(i);
                    result.append(v.getTime()).append("\t").append(v.getValue()).append("\n");
                    entropySeries.add(i, v.getValue());
                }

                new Picture()
                        .addSeries(entropySeries)
                        .setRange(new double[]{0, valuesInTimeIntervals.size()}, new double[]{0, 1.0})
                        .plotLine("Laikas", "H", pictureDirectory + "information_entropy_in_time_" + windowWidth + "_" + increment + ".png");

//            return result.toString();
            }
        }
        return "SUCCESS";
    }

    @Override
    public String getOptimalTimeDelay(Timestamp start, Timestamp end, Integer increment
            , Integer windowWidth, String type, List<Integer> pointCountList) throws GeneralException {
        List<Double> valueList = getCurrentValues(start, end, increment, windowWidth, type);

        StringBuilder result = new StringBuilder();
        List<Integer> localMinimumList = new ArrayList<Integer>();
        int pointsToCalculate = 40;
        Picture mutualEntropyGraph = new Picture();
        for (int i = 0; i < pointCountList.size(); i++) {
            XYSeries series = getSeries(pointCountList.get(i), pointsToCalculate, valueList);
            XYSeries fittedSeries = getFittedCurve(series.getItems());

            mutualEntropyGraph.addSeries(series);
            //mutualEntropyGraph.addSeries(fittedSeries);

            Double firstLocalMinimum = getFirstLocalMinimum(fittedSeries.getItems());
            localMinimumList.add(firstLocalMinimum.intValue());
            result.append(getLine("\t", String.valueOf(pointCountList.get(i)), String.valueOf(firstLocalMinimum))).append("\n");
        }

        mutualEntropyGraph.plotLine("Laiko postūmis", "I", pictureDirectory + "mutual_information.png");

        for (int i = 0; i < pointCountList.size(); i++) {
            Picture phaseSpace = new Picture();
            XYSeries phaseSpaceSeries = new XYSeries(pointCountList.get(i));
            for (int j = 0; j < pointCountList.get(i); j++) {
                Double x = valueList.get(j);
                Double y = valueList.get(j + localMinimumList.get(i));
                phaseSpaceSeries.add(x, y);
            }
            phaseSpace.addSeries(phaseSpaceSeries);
            phaseSpace.plotScatter("X", "Y", pictureDirectory + "phase_space_" + pointCountList.get(i) + ".png");
        }

        return result.toString();
    }

    private List<Double> convertToDoubleList(List<ValueInTimeInterval> valueInTimeIntervalList) {
        List<Double> doubleList = new ArrayList<Double>();
        for (ValueInTimeInterval valueInTimeInterval : valueInTimeIntervalList) {
            doubleList.add(valueInTimeInterval.getValue());
        }
        return doubleList;
    }

    private XYSeries getFittedCurve(List<XYDataItem> dataItemList) {
        int size = dataItemList.size();
        final WeightedObservedPoints weightedPoints = new WeightedObservedPoints();

        for (int i = 0; i < size; i++) {
            weightedPoints.add(dataItemList.get(i).getXValue(), dataItemList.get(i).getYValue());
        }

        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(9);
        //final HarmonicCurveFitter harmonicFitter = HarmonicCurveFitter.create();

        //final double[] harmonicParameters = harmonicFitter.fit(weightedPoints.toList());
        final double[] parameters = fitter.fit(weightedPoints.toList());

        XYSeries fittedSeries = new XYSeries(size);
        for (int i = 0; i < size; i++) {
            double value = parameters[0];
            for (int j = 1; j < parameters.length; j++) {
                value += Math.pow(i, j) * parameters[j];
            }
            fittedSeries.add(i, value);
        }
        return fittedSeries;
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
    public String predict(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, String type
            , Integer dimensionCount, Integer optimalTimeDelay) throws GeneralException {

        List<Double> valueList = getCurrentValues(start, end, increment, windowWidth, type);

        int pointCountas = 3400;
        int predictedPointCount = 0;

        XYSeries realValueSeries = new XYSeries(valueList.size());
        XYSeries predictedValueSeries = new XYSeries(valueList.size());

        double[] valueArray = getValuesBeforePrediction(pointCountas, valueList);
        addValuesToSeries(realValueSeries, predictedValueSeries, valueArray, 0.9853, pointCountas);

        StringBuilder result = new StringBuilder();
        for (int i = (int) (pointCountas * 0.9853); i < pointCountas; i++) {
            result.append(getLine("\t"
                    , String.valueOf(i)
                    , String.valueOf(valueArray[i])
                    , String.valueOf(valueArray[i])
            )).append("\n");
        }
        try {
            while (true) {
                int i = predictedPointCount;
                double[][] coeficientMatrix = getCoeficientMatrix(valueArray, dimensionCount, optimalTimeDelay);
                double[][] Y2Matrix = getY2Matrix(valueArray, dimensionCount, optimalTimeDelay);
                double[][] predictedMatrix = multiplyMatrixes(coeficientMatrix, Y2Matrix);

                double predictedValue = predictedMatrix[0][0];
                double nextRealValue = valueList.get(pointCountas + i);

                if (Math.abs(predictedValue - nextRealValue) > 0.25) {
                    break;
                }

                realValueSeries.add(i + pointCountas, nextRealValue);
                predictedValueSeries.add(i + pointCountas, predictedValue);
                result.append(getLine("\t"
                        , String.valueOf(i + pointCountas)
                        , String.valueOf(nextRealValue)
                        , String.valueOf(predictedValue)
                )).append("\n");

                valueArray = modifyValueArray(valueArray, predictedValue);
                predictedPointCount++;

                //result.append("Coffs\n").append(matrixToString(coeficientMatrix)).append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.append("FAILED\n");
        }

        plotRealAndPredictedValueGraph(realValueSeries, predictedValueSeries);
        return result.append(String.format("SUCCESS - PREDICTED POINTS = %d", predictedPointCount)).toString();
    }

    private void plotRealAndPredictedValueGraph(XYSeries realValueSeries, XYSeries predictedValueSeries) throws GeneralException {
        new Picture()
                .addSeries(realValueSeries)
                .plotLine("Laikas", "Re", pictureDirectory + "generated_real_values.png");
        new Picture()
                .addSeries(predictedValueSeries)
                .plotLine("Laikas", "Re", pictureDirectory + "generated_predicted_values.png");
    }

    private double[] modifyValueArray(double[] valueArray, double valueToAdd) {
        System.arraycopy(valueArray, 1, valueArray, 0, valueArray.length - 1);
        valueArray[valueArray.length - 1] = valueToAdd;
        return valueArray;
    }

    private double[][] getY2Matrix(double[] valueArray, int dimensionCount, int optimalTimeDelay) {
        double[][] Y2Matrix = new double[dimensionCount + 1][1];

        double[] vector = getVector(valueArray, dimensionCount, optimalTimeDelay, valueArray.length - 1);
        Y2Matrix[0][0] = 1.0;
        for (int k = 0; k < vector.length; k++) {
            Y2Matrix[k + 1][0] = vector[k];
        }

        return Y2Matrix;
    }

    private void addValuesToSeries(XYSeries realValueSeries, XYSeries predictedValueSeries, double[] valueArray, double startAt, Integer pointCountas) {
        for (int i = (int) (pointCountas * startAt); i < pointCountas; i++) {
            realValueSeries.add(i, valueArray[i]);
            predictedValueSeries.add(i, valueArray[i]);
        }
    }

    private double[] getValuesBeforePrediction(Integer pointCountas, List<Double> allValues) {
        double[] valueArray = new double[pointCountas];
        for (int index = 0; index < pointCountas; index++) {
            valueArray[index] = allValues.get(index);
        }
        return valueArray;
    }

    private List<Double> getCurrentValues(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, String type) {
        List<Double> valueList = new ArrayList<Double>();
        if (type.equals("sin")) {
            valueList = getSinusoide();
        } else if (type.equals("sinWithErr")) {
            valueList = getSinusoideWithError();
        } else if (type.equals("realValues")) {
            List<PacketsInfo> packetsInfo = packetDao.findPacketCounts(start, end, increment);
            StorageByDestinationInTimeDomain storage = getStorageWithCalculatedEntropy(packetsInfo, windowWidth);
            valueList = convertToDoubleList(storage.getListOfEntropies());
        }
        return valueList;
    }

    private List<Double> getSinusoide() {
        List<Double> sinusoide = new ArrayList<Double>();
        double step = 0.005;
        for (int i = 0; i < 1E5; i++) {
            Double value = Math.sin(step * i * Math.PI) + 2;
            sinusoide.add(value);
        }

        return sinusoide;
    }

    public List<Double> getSinusoideWithError() {
        List<Double> sinusoide = new ArrayList<Double>();
        double step = 0.005;
        for (int i = 0; i < 1E5; i++) {
            double error = Math.random() / 20;
            Double sin = Math.sin(step * i * Math.PI);
            Double value = sin + 2 + error;
            sinusoide.add(value);
        }

        return sinusoide;
    }

    private double[][] getCoeficientMatrix(double[] valueArray, Integer dimensionCount, Integer optimalTimeDelay) throws GeneralException {
        Integer pointCountas = dimensionCount + 1;
        double[][] matrixB = new double[pointCountas][pointCountas];
        double[][] matrixD = new double[1][pointCountas];

        List<Integer> neighbourList = getLastPointNeighbours(valueArray, dimensionCount, optimalTimeDelay);

        //sudarom B matricą
        for (int i = 0; i < dimensionCount + 1; i++) {
            for (int j = 0; j < neighbourList.size(); j++) {
                double[] column = new double[pointCountas];
                double[] vector = getVector(valueArray, dimensionCount, optimalTimeDelay, neighbourList.get(j));
                column[0] = 1;
                System.arraycopy(vector, 0, column, 1, vector.length);

                for (int k = 0; k < column.length; k++) {
                    matrixB[k][j] = column[k];
                }
            }
        }

        //sudarom D matricą
        for (int i = 0; i < neighbourList.size(); i++) {
            matrixD[0][i] = valueArray[neighbourList.get(i) + 1];
        }

        return getCoeficientMatrix(matrixB, matrixD);
    }

    private double[] getVector(double[] valueArray, int dimensionCount, int optimalTimeDelay, int index) {
        double[] key = new double[dimensionCount];
        for (int i = 0; i < dimensionCount; i++) {
            key[i] = valueArray[index - (i * optimalTimeDelay)];
        }
        return key;
    }

    private List<Integer> getLastPointNeighbours(double[] valueArray, int dimensionCount, final int optimalTimeDelay) throws GeneralException {
        try {
            //Susikonstruojam medį
            KDTree<Integer> kdTree = new KDTree<Integer>(dimensionCount * 5);
            int lastIndex = valueArray.length - 1;
            int neighbourPointCount = valueArray.length - (dimensionCount * optimalTimeDelay);
            for (int i = 1; i < neighbourPointCount; i++) {//Nes paskutinis taškas neturėtų būti kaimynas (tai pradedam nuo 1)
                double[] key = getKey(valueArray, dimensionCount, optimalTimeDelay, lastIndex - i);
                if (kdTree.search(key) == null) {
                    kdTree.insert(key, lastIndex - i);
                }
            }

            double[] lastKey = getKey(valueArray, dimensionCount, optimalTimeDelay, lastIndex);
            return kdTree.nearest(lastKey, dimensionCount + 1);
//            final List<Integer> legitNeighbours = new ArrayList<Integer>();
//            while (legitNeighbours.size() < dimensionCount + 1) {
//                Checker<Integer> checker = new Checker<Integer>() {
//                    @Override
//                    public boolean usable(Integer v) {
//                        return isLegit(v, legitNeighbours, optimalTimeDelay / 2);
//                    }
//                };
//                List<Integer> neighbours = kdTree.nearest(lastKey, dimensionCount + 1, checker);
//                supplementLegitNeighbours(neighbours, legitNeighbours, optimalTimeDelay / 2);
//            }
//            return legitNeighbours.subList(0, dimensionCount + 1);
        } catch (Exception e) {
            throw new GeneralException("KDTree failed!", e);
        }
    }

    private boolean isLegit(Integer neighbour, List<Integer> legitNeighbours, Integer distanceBetweenNeighbours) {
        boolean legit = true;
        for (Integer legitNeighbour : legitNeighbours) {
            if (Math.abs(neighbour - legitNeighbour) < distanceBetweenNeighbours) {
                legit = false;
            }
        }
        return legit;
    }

    private double[] getKey(double[] valueArray, int dimensionCount, int optimalTimeDelay, int index) {
        int vectorsToPutToKey = 5;
        double[] key = new double[dimensionCount * vectorsToPutToKey];
        for (int i = 0; i < vectorsToPutToKey; i++) {
            for (int j = 0; j < dimensionCount; j++) {
                key[(i * dimensionCount) + j] = valueArray[index - i - (j * optimalTimeDelay)];
            }
        }
        return key;
    }

    private double[][] getCoeficientMatrix(double[][] matrixB, double[][] matrixD) {
        QRDecomposition qrDecomposition = new QRDecomposition(
                new BlockRealMatrix(matrixB)
        );

        RealMatrix matrixQT = qrDecomposition.getQT();
        RealMatrix matrixR = qrDecomposition.getR();

        RealMatrix matrixRinverse = new QRDecomposition(matrixR).getSolver().getInverse();

        RealMatrix coeficientMatrix = new BlockRealMatrix(matrixD)
                .multiply(matrixRinverse)
                .multiply(matrixQT);


        return coeficientMatrix.getData();
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
