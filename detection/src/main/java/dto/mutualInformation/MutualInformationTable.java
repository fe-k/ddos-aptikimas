package dto.mutualInformation;

import exceptions.GeneralException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MutualInformationTable {

    private Double mutualInformation = 0.0;

    private class OrderedValue {
        private int oldIndex;
        private double value;

        public OrderedValue() {
        }

        public OrderedValue(int oldIndex, double value) {
            this.oldIndex = oldIndex;
            this.value = value;
        }

        public int getOldIndex() {
            return oldIndex;
        }

        public void setOldIndex(int oldIndex) {
            this.oldIndex = oldIndex;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private class OrderedLists {
        List<OrderedValue> currentOrderedValues;
        List<OrderedValue> shiftedOrderedValues;

        private OrderedLists(List<Double> currentValues, List<Double> shiftedVales) {
            currentOrderedValues = new ArrayList<OrderedValue>();
            shiftedOrderedValues = new ArrayList<OrderedValue>();

            Comparator<OrderedValue> orderedValueComparator = new Comparator<OrderedValue>() {
                @Override
                public int compare(OrderedValue o1, OrderedValue o2) {
                    if (o1.getValue() < o2.getValue()) {
                        return -1;
                    } else if (o1.getValue() > o2.getValue()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };

            for (int i = 0; i < currentValues.size(); i++) {
                currentOrderedValues.add(new OrderedValue(i, currentValues.get(i)));
                shiftedOrderedValues.add(new OrderedValue(i, shiftedVales.get(i)));
            }

            Collections.sort(currentOrderedValues, orderedValueComparator);
            Collections.sort(shiftedOrderedValues, orderedValueComparator);
        }

        public List<OrderedValue> getCurrentOrderedValues() {
            return currentOrderedValues;
        }

        public List<OrderedValue> getShiftedOrderedValues() {
            return shiftedOrderedValues;
        }
    }

    public MutualInformationTable(List<Double> currentValues, List<Double> shiftedVales) throws GeneralException {
        OrderedLists orderedLists = new OrderedLists(currentValues, shiftedVales);

        List<OrderedValue> currentOrderedValues = orderedLists.getCurrentOrderedValues();
        List<OrderedValue> shiftedOrderedValues = orderedLists.getShiftedOrderedValues();

        int N = currentOrderedValues.size();
        int Ns = getAmountOfSegments(N);
        Ns = N % Ns == 0 ? Ns : Ns + 1; //Jeigu nesidalina, tai pridedam vienetą
        int Nv = getAmountOfValuesInASegment(N);

        Integer[][] probabilityMatrix = initializeProbabilityMatrix(Ns);

        for (int i = 0; i < N; i++) {
            int currentIndex = currentOrderedValues.get(i).getOldIndex() / Nv;
            int shiftedIndex = shiftedOrderedValues.get(i).getOldIndex() / Nv;
            probabilityMatrix[currentIndex][shiftedIndex]++;
        };

        int k = N / Nv;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int newN = Nv * k;
                int dotCount = probabilityMatrix[i][j];
                if (dotCount > 0) {
                    double firstSide = dotCount / (double) newN;
                    double secondSide = Math.log((dotCount * newN) / (double) (Nv * Nv)) / Math.log(2);
                    mutualInformation += firstSide * secondSide;
                }
            }
        }
    }

    private Integer[][] initializeProbabilityMatrix(int amountOfSegments) {
        Integer[][] probabilityMatrix = new Integer[amountOfSegments][amountOfSegments];
        for (int i = 0; i < amountOfSegments; i++) {
            for (int j = 0; j < amountOfSegments; j++) {
                probabilityMatrix[i][j] = 0;
            }
        }
        return probabilityMatrix;
    }

    private int getAmountOfValuesInASegment(int amountOfValues) {
        return (int) (amountOfValues / (double) getAmountOfSegments(amountOfValues));
    }

    private int getAmountOfSegments(int amountOfValues) {
        return (int) Math.sqrt(amountOfValues / (double) 5);
    }

    public Double getMutualInformation() {
        return mutualInformation;
    }
}
