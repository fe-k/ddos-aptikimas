package dto.neighboarPoints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by K on 5/15/2016.
 */
public class NeighbourPoints {

    List<double[]> axisList = new ArrayList<double[]>();



    public NeighbourPoints addAxis(double[] axis) {
        axisList.add(axis);
        return this;
    }

    class Pointas {
        private final int dimensionCount;
        private double[] coordinates;
        private int indexInTimeScale;

        public Pointas(int dimensionCount, int indexInTimeScale, double[] coordinates) {
            this.dimensionCount = dimensionCount;
            this.coordinates = coordinates;
            this.indexInTimeScale = indexInTimeScale;
        }

        private double getCoordinate(int index) {
            return coordinates[index];
        }

        public int getIndexInTimeScale() {
            return indexInTimeScale;
        }

        public int getDimensionCount() {
            return dimensionCount;
        }
    }

}
