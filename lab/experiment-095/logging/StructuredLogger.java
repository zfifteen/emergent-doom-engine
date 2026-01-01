package lab.experiment095.logging;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.time.Duration;

/**
 * Structured JSON logging system for experiment observability.
 * 
 * PURPOSE:
 * Provides structured logging with per-step timing, error codes, and telemetry.
 * Enables debugging, performance analysis, and operational monitoring.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 5 (Ops/Observability): Structured logging (JSON logs)
 * - Per-step timing and telemetry
 * - Error codes and context
 * 
 * ARCHITECTURE:
 * Thread-safe logger that writes JSON-formatted log entries.
 * Supports hierarchical contexts for nested operations.
 * Integrates with standard SLF4J/Logback for compatibility.
 * 
 * DATA FLOW:
 * Log event → Format as JSON → Add context → Write to output → Flush
 * 
 * IMPLEMENTATION NOTE:
 * This is a simplified implementation. Production would use SLF4J + Logback with JSON encoder.
 */
public class StructuredLogger {
    
    /**
     * Logger name (typically class or component name).
     */
    private final String loggerName;
    
    /**
     * Current logging context (key-value pairs added to all log entries).
     */
    private final ThreadLocal<Map<String, Object>> context;
    
    /**
     * Log level threshold (DEBUG, INFO, WARN, ERROR).
     */
    private LogLevel minimumLevel;
    
    /**
     * Constructor for StructuredLogger.
     * 
     * PURPOSE:
     * Initializes logger with name and level threshold.
     * 
     * INPUTS:
     * @param loggerName Name of logger (component/class)
     * @param minimumLevel Minimum log level to output
     * 
     * IMPLEMENTATION:
     * Creates thread-local context map for contextual logging.
     */
    public StructuredLogger(String loggerName, LogLevel minimumLevel) {
        this.loggerName = loggerName;
        this.minimumLevel = minimumLevel;
        this.context = ThreadLocal.withInitial(HashMap::new);
    }
    
    /**
     * Log info-level message with structured fields.
     * 
     * PURPOSE:
     * Records informational event with structured data.
     * Useful for tracking normal operation and progress.
     * 
     * INPUTS:
     * @param message Human-readable message
     * @param fields Structured fields (key-value pairs)
     * 
     * DATA FLOW:
     * message + fields + context → Format JSON → Write log entry
     * 
     * IMPLEMENTATION:
     * Merges context with fields, formats as JSON, writes to stdout.
     * In production, would use SLF4J/Logback JSON encoder.
     * 
     * EXAMPLE:
     * logger.info("Feature extraction completed", Map.of(
     *     "window_count", 1000,
     *     "duration_ms", 245.2,
     *     "features_per_window", 28
     * ));
     */
    public void info(String message, Map<String, Object> fields) {
        if (minimumLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            writeLog(LogLevel.INFO, message, fields, null, null);
        }
    }
    
    /**
     * Log warning-level message.
     * 
     * PURPOSE:
     * Records warning event that may indicate potential issues.
     * 
     * INPUTS:
     * @param message Warning message
     * @param fields Structured fields
     * 
     * IMPLEMENTATION:
     * Same as info() but with WARN level.
     */
    public void warn(String message, Map<String, Object> fields) {
        if (minimumLevel.ordinal() <= LogLevel.WARN.ordinal()) {
            writeLog(LogLevel.WARN, message, fields, null, null);
        }
    }
    
    /**
     * Log error-level message with exception.
     * 
     * PURPOSE:
     * Records error event with exception details and context.
     * Includes stack trace and error code for debugging.
     * 
     * INPUTS:
     * @param message Error message
     * @param exception Exception that occurred
     * @param errorCode Error code for categorization
     * @param fields Additional structured fields
     * 
     * DATA FLOW:
     * message + exception + fields → Extract stack trace →
     * Format JSON with error details → Write log entry
     * 
     * IMPLEMENTATION:
     * Formats exception with stack trace, adds error code field.
     */
    public void error(String message, Throwable exception, String errorCode, Map<String, Object> fields) {
        if (minimumLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            writeLog(LogLevel.ERROR, message, fields, exception, errorCode);
        }
    }
    
    /**
     * Write formatted log entry.
     * 
     * PURPOSE:
     * Internal method to format and write JSON log entry.
     * Merges context, formats as JSON, writes to output.
     * 
     * INPUTS:
     * @param level Log level
     * @param message Log message
     * @param fields Structured fields
     * @param exception Optional exception
     * @param errorCode Optional error code
     * 
     * DATA FLOW:
     * Merge context + fields → Add standard fields → Format JSON → Write
     * 
     * IMPLEMENTATION:
     * Simple JSON formatting. Production would use Jackson or Gson.
     */
    private void writeLog(LogLevel level, String message, Map<String, Object> fields, 
                         Throwable exception, String errorCode) {
        // Build JSON log entry
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Standard fields
        json.append("\"timestamp\":\"").append(Instant.now().toString()).append("\",");
        json.append("\"level\":\"").append(level.name()).append("\",");
        json.append("\"logger\":\"").append(loggerName).append("\",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\"");
        
        // Add context fields
        Map<String, Object> contextMap = context.get();
        if (!contextMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
                json.append(",\"").append(escapeJson(entry.getKey())).append("\":").append(formatValue(entry.getValue()));
            }
        }
        
        // Add custom fields
        if (fields != null && !fields.isEmpty()) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                json.append(",\"").append(escapeJson(entry.getKey())).append("\":").append(formatValue(entry.getValue()));
            }
        }
        
        // Add error code if present
        if (errorCode != null) {
            json.append(",\"error_code\":\"").append(errorCode).append("\"");
        }
        
        // Add exception if present
        if (exception != null) {
            json.append(",\"exception\":\"").append(escapeJson(exception.getClass().getName())).append("\"");
            json.append(",\"exception_message\":\"").append(escapeJson(exception.getMessage())).append("\"");
            // In production, would include full stack trace
        }
        
        json.append("}");
        
        // Write to stdout (in production, would use SLF4J)
        System.err.println(json.toString());
    }
    
    /**
     * Format value for JSON output.
     * 
     * PURPOSE:
     * Converts Java object to JSON representation.
     * 
     * IMPLEMENTATION:
     * Simple formatting for common types. Production would use Jackson.
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
    }
    
    /**
     * Escape string for JSON.
     * 
     * PURPOSE:
     * Escapes special characters and control characters in JSON strings.
     * 
     * IMPLEMENTATION:
     * Escapes quotes, backslashes, newlines, tabs, and all control characters (U+0000 to U+001F).
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c >= 0x00 && c <= 0x1F) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
    
    /**
     * Start timing operation.
     * 
     * PURPOSE:
     * Begins timing a named operation for performance tracking.
     * Returns timer object that can be stopped to record duration.
     * 
     * INPUTS:
     * @param operationName Name of operation being timed
     * 
     * OUTPUTS:
     * @return OperationTimer for stopping and recording duration
     * 
     * DATA FLOW:
     * operationName → Record start time → Return timer
     * 
     * IMPLEMENTATION:
     * Creates new OperationTimer with current timestamp.
     * 
     * USAGE:
     * OperationTimer timer = logger.startTimer("wavelet_transform");
     * // ... do work ...
     * timer.stop(); // Logs duration automatically
     */
    public OperationTimer startTimer(String operationName) {
        return new OperationTimer(operationName, Instant.now(), this);
    }
    
    /**
     * Add context field to all subsequent log entries.
     * 
     * PURPOSE:
     * Sets contextual information that will be included in all logs
     * from this thread until cleared. Useful for tracking request IDs,
     * experiment IDs, etc.
     * 
     * INPUTS:
     * @param key Context field name
     * @param value Context field value
     * 
     * DATA FLOW:
     * key + value → Add to thread-local context → Applied to future logs
     * 
     * IMPLEMENTATION:
     * Adds to thread-local HashMap that persists across log calls.
     */
    public void addContext(String key, Object value) {
        context.get().put(key, value);
    }
    
    /**
     * Clear all context fields and remove thread-local entry.
     * 
     * PURPOSE:
     * Removes all context fields from current thread and cleans up ThreadLocal entry.
     * Should be called at end of operation to prevent context leakage and memory leaks.
     * 
     * DATA FLOW:
     * Remove thread-local entry so the context can be garbage collected.
     * 
     * IMPLEMENTATION:
     * Calls context.remove() to properly clean up ThreadLocal storage.
     */
    public void clearContext() {
        context.remove();
    }
    
    /**
     * Log levels for filtering.
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Timer for measuring operation duration.
     * 
     * PURPOSE:
     * Tracks operation start time and logs duration when stopped.
     * Automatically includes operation name and duration in log entry.
     */
    public static class OperationTimer {
        private final String operationName;
        private final Instant startTime;
        private final StructuredLogger logger;
        
        /**
         * Constructor for OperationTimer.
         * 
         * INPUTS:
         * @param operationName Name of operation
         * @param startTime Start time of operation
         * @param logger Logger to write completion event
         * 
         * IMPLEMENTATION:
         * Stores references for later use in stop().
         */
        public OperationTimer(String operationName, Instant startTime, StructuredLogger logger) {
            this.operationName = operationName;
            this.startTime = startTime;
            this.logger = logger;
        }
        
        /**
         * Stop timer and log duration.
         * 
         * PURPOSE:
         * Records end time, calculates duration, and logs completion event.
         * 
         * OUTPUTS:
         * @return Duration in milliseconds
         * 
         * DATA FLOW:
         * Record end time → Calculate duration → Log completion event → Return duration
         * 
         * IMPLEMENTATION:
         * Uses Duration.between() to calculate elapsed time.
         * Logs INFO event with operation_name and duration_ms fields.
         */
        public long stop() {
            Instant endTime = Instant.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();
            
            Map<String, Object> logFields = new HashMap<>();
            logFields.put("operation_name", operationName);
            logFields.put("duration_ms", durationMs);
            logger.info("Operation completed", logFields);
            
            return durationMs;
        }
    }
}
