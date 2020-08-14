package pcao.model.data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;

import pcao.model.time.TimeUtil;

public class DataSet {

    private LinkedList<DataPoint> data = new LinkedList<>();

    public DataSet() {
    }

    public DataSet(DataPoint... points) {
        for (DataPoint point : points) {
            data.add(point);
        }
    }

    public void addData(DataPoint data_point) {
        data.add(data_point);
    }

    public void prettyPrint() {
        for (DataPoint point : data) {
            System.out.format("%s\t|\t$%.2f\n", TimeUtil.FORMAT.format(new Date(point.getTime())), point.getPrice());
        }
    }

    public void saveJSONToFile(String filename) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println("export const data = " + getJSONWithDatestamp());
        }
    }

    public String getJSONWithDatestamp() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        String seperator = "";

        for (DataPoint dataPoint : data) {
            Date date = new Date(dataPoint.getTime());
            String dateString = TimeUtil.FORMAT.format(date);
            builder
                .append(seperator)
                .append("{\"timestamp\":\"" + dateString + "\",\"price\":" + dataPoint.getPrice() + "}");
        
            seperator = ",";
        }

        builder.append("]");
        return builder.toString();
    }
}