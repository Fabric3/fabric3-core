package weblogic.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @version $Rev$ $Date$
 */
public class WLLogRecord extends LogRecord {
    private static final long serialVersionUID = -1674273644835603201L;


    public WLLogRecord(Level level, String msg) {
        super(level, msg);
    }


    public void setId(String id) {
    }

    public String getId() {
        return null;
    }

    public void setMachineName(String machineName) {
    }

    public String getMachineName() {
        return null;
    }

    public void setServerName(String serverName) {
    }

    public String getServerName() {
        return null;
    }

    public void setThreadName(String threadId) {
    }

    public String getThreadName() {
        return null;
    }

    public void setUserId(String ID) {
    }

    public String getUserId() {
        return null;
    }

    public void setTransactionId(String ID) {
    }

    public String getTransactionId() {
        return null;
    }

    public int getSeverity() {
        return 0;
    }

    public String getSubsystem() {
        return null;
    }

    public long getTimestamp() {
        return -1;
    }

    public Throwable getThrowable() {
        return null;
    }

    public String getLogMessage() {
        return null;
    }

    public String getDiagnosticContextId() {
        return null;
    }

    public void setDiagnosticContextId(String id) {
    }

    public static WLLogRecord normalizeLogRecord(LogRecord RECORD) {
        return null;
    }
}
