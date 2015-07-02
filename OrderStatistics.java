import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OrderStatistics {
    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("Wrong number of input arguments.");
            System.exit(2);
        }
        Map<String, Double> result = null;
        try {
            final RankRatioTable table = RankRatioTable.loadFromRankRatioFiles(Arrays.asList(args));
            result = table.combine();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        for (final String ID : result.keySet()) {
            System.out.format("%s\t%.18e%n", ID, result.get(ID));
        }
    }

    /**
     * RankRatioTable with OrderStatistics operation.
     */
    public static final class RankRatioTable {
        private static final String SEPARATOR = "\t";

        private final Map<String, List<Double>> table;

        public static final RankRatioTable loadFromRankRatioFiles(final List<String> fileNames) throws RuntimeException {
            final RankRatioTable result = new OrderStatistics.RankRatioTable();
            for (final String fileName : fileNames) {
                appendFileToTable(fileName, result);
            }
            return result;
        }

        private static void appendFileToTable(final String fileName, final RankRatioTable toAppendTo) throws RuntimeException {
            BufferedReader input = null;
            try {
                final Set<String> prevIDs = new HashSet<String>();
                input = new BufferedReader(new FileReader(fileName));
                String line;
                while ((line = input.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    final String[] columns = line.trim().split(SEPARATOR);
                    final String ID = columns[0];
                    if (prevIDs.contains(ID)) {
                        throw new RuntimeException("ID \"" + ID + "\" is not unique.");
                    }
                    for (int idx = 1; idx < columns.length; idx++) {
                        final Double rankRatio = parseRankRatio(columns[idx]);
                        toAppendTo.addRankRatio(ID, rankRatio);
                    }
                    prevIDs.add(ID);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (input != null) input.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private static Double parseRankRatio(final String ratio) throws RuntimeException {
            if (ratio.equalsIgnoreCase("NAN") || ratio.equalsIgnoreCase("NA")) {
                return Double.NaN;
            }
            try {
                return new Double(ratio);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid rank ratio \"" + ratio + "\".");
            }
        }

        private RankRatioTable() {
            this.table = new HashMap<String, List<Double>>();
        }

        private void addRankRatio(final String ID, final Double rankRatio) {
            if (Double.isNaN(rankRatio)) {
                return;
            }
            if (this.table.keySet().contains(ID)) {
                this.table.get(ID).add(rankRatio);
            } else {
                final List<Double> rankRatios = new ArrayList<Double>();
                rankRatios.add(rankRatio);
                this.table.put(ID, rankRatios);
            }
        }

        public List<Double> getRankRatios(final String ID) {
            if (this.table.keySet().contains(ID)) {
                return this.table.get(ID);
            } else {
                return Collections.EMPTY_LIST;
            }
        }

        private Double applyOrderStatistics(final List<Double> rankRatios) {
            if (rankRatios.isEmpty()) {
                return 1.0d;
            } else {
                Collections.sort(rankRatios);
                final int size = rankRatios.size();
                final double[] w = new double[size + 1];
                w[0] = 1.0;
                w[1] = rankRatios.get(size - 1);
                for (int k = 2; k <= size; k++) {
                    w[k] = 0.0d;
                    double f = -1.0d;
                    for (int j = 0; j <= k - 1; j++) {
                        f = -(f * (k - j) * rankRatios.get(size - k)) / (j + 1.0d);
                        w[k] = w[k] + (w[k - j - 1] * f);
                    }
                }
                return w[size];
            }
        }

        public int size() {
            return this.table.keySet().size();
        }

        public boolean hasID(final String ID) {
            return this.table.keySet().contains(ID);
        }

        public Map<String, Double> combine() {
            final Map<String, Double> result = new HashMap<String, Double>();
            for (final String ID : this.table.keySet()) {
                result.put(ID, this.applyOrderStatistics(this.getRankRatios(ID)));
            }
            return result;
        }
    }
}
