// Bailey JT Brown
// 2023
// QException.java

package QDraw;

public final class QException extends RuntimeException {
    public enum PointOfError {
        NullParameter,
        InvalidParameter,
        MalformedData,
        InvalidData,
        BadState
    }
    
    public QException(PointOfError poi, String remarks) {
        super(String.format(
            "%s: %s", poi.toString(), remarks
        ));
    }
}
