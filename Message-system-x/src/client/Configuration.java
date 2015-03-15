package client;

import java.io.*;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Created by Fawk on 2015-03-13.
 */
public class Configuration {

    private final static int PROPERTY_LIMIT = 10;
    private final static String BUNDLE_FULL_NAME = "messengerBundle.properties";
    public final static String BUNDLE_NAME = "messengerBundle";

    private Thread writeThread;
    private PrintWriter pr;

    public Configuration() {}

    public void put(final String key, final Object value) {

        // Get stacktrace elements for safe calling
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callee_class = stackTrace[2].getClassName();
        String callee_method = stackTrace[2].getMethodName();
        final String callee = callee_class + callee_method;

        writeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Properties properties = new Properties();
                try {
                    properties.load(Configuration.class.getResourceAsStream("/" + BUNDLE_FULL_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Check if key exists
                if(!properties.containsKey(key)) {
                    pr.println(String.format("%s=%s", key, value));
                } else {

                    // Read contents, change value of key

                    // Init lines array and scanner
                    String[] lines = new String[PROPERTY_LIMIT];
                    Scanner scanner = new Scanner(Messenger.RESOURCE_FOLDER + BUNDLE_FULL_NAME);

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
                        PrintWriter npr = new PrintWriter(Messenger.RESOURCE_FOLDER + BUNDLE_FULL_NAME);

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
