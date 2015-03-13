package client;

import java.io.*;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Created by Fawk on 2015-03-13.
 */
public class Configuration {

    private final static int PROPERTY_LIMIT = 10;
    private final static String SAFE_CALLEE = "client.Configuration<init>";
    private final static String BUNDLE_NAME = "messengerBundle";
    private Thread writeThread;
    private PrintWriter pr;
    private ResourceBundle rb;

    public Configuration() {
        try {

            rb = ResourceBundle.getBundle(Messenger.RESOURCE_FOLDER + BUNDLE_NAME);

        } catch(MissingResourceException ex) {
            ex.printStackTrace();
            // Resource is missing, creating...
            put(null, null);
        }
    }

    public String get(String key) {
        return rb.getString(key);
    }

    public void put(final String key, final Object value) {

        // Get stacktrace elements for safe calling
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callee_class = stackTrace[2].getClassName();
        String callee_method = stackTrace[2].getMethodName();
        final String callee = callee_class + callee_method;

        writeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // Is the call safe? Only accepting call from Configuration constructor.
                    if(SAFE_CALLEE.equals(callee)) {
                        pr = new PrintWriter(new FileOutputStream(new File(Messenger.RESOURCE_FOLDER + BUNDLE_NAME), true));
                        rb = ResourceBundle.getBundle(BUNDLE_NAME);
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                }

                // Check if key exists
                if(!rb.containsKey(key)) {
                    pr.println(String.format("%s=%s", key, value));
                } else {

                    // Read contents, change value of key

                    // Init lines array and scanner
                    String[] lines = new String[PROPERTY_LIMIT];
                    Scanner scanner = new Scanner(Messenger.RESOURCE_FOLDER + BUNDLE_NAME);

                    // Init count variable i for sanity
                    int i = 0;

                    // id to index of key in lines array
                    int id = -1;

                    // Read lines
                    while(scanner.hasNext()) {
                        if(i == lines.length) {
                            // Exceeding property limit, breaking
                            break;
                        }
                        // Add to lines array
                        lines[i++] = scanner.nextLine();
                        if(lines[i].split("=")[0].equals(key))
                            id = i;
                    }

                    // Done reading, for good or bad

                    // Change the value of the found key
                    lines[id] = String.format("%s=%s", lines[id].split("=")[0], value);

                    // Proceed to write to file

                    try {
                        PrintWriter npr = new PrintWriter(Messenger.RESOURCE_FOLDER + BUNDLE_NAME);

                        // Clear the file
                        npr.write("");

                        // Write the lines
                        for(String line : lines) {
                            npr.println(line);
                        }

                        // Clean up
                        npr.close();

                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        writeThread.start();
    }
}
