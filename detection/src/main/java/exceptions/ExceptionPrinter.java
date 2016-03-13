package exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionPrinter {

    private StringWriter sw;
    private PrintWriter printWriter;
    private Exception e;

    public ExceptionPrinter() {
        this.sw = new StringWriter();
        this.printWriter = new PrintWriter(sw);
    }

    public ExceptionPrinter setException(Exception e) {
        this.e = e;
        return this;
    }

    public String toString() {
        if (e == null) {
            return "";
        } else {
            e.printStackTrace(printWriter);
            return sw.toString();
        }
    }

}
