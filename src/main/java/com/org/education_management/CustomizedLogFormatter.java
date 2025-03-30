package com.org.education_management;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomizedLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        // Get the timestamp in the default time zone
        ZonedDateTime zdt = ZonedDateTime.ofInstant(
                record.getInstant(), ZoneId.systemDefault());

        // Determine the source (class and method) for the log message
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }

        // Get the log message (formatted string)
        String message = formatMessage(record);

        // Handle any exceptions (if thrown)
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        // Get the thread name and thread ID
        String threadName = Thread.currentThread().getName();
        long threadId = Thread.currentThread().getId();

        // Now include thread info within the formatted log message
        // Define your custom format string with placeholders for thread info
        String format = "%1$tF %1$tT - %2$s - %3$s - %4$s - %5$s - %6$d - %7$s%8$s \n";
        return String.format(format,
                zdt,               // Timestamp
                source,            // Source (class/method)
                record.getLoggerName(), // Logger name
                record.getLevel(), // Log level (INFO, SEVERE, etc.)
                threadName,        // Thread name
                threadId,          // Thread ID
                message,           // Log message
                throwable          // Exception stack trace (if any)
        );
    }
}