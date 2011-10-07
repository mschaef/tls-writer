package com.ksmpartners.tlswriter;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App
{
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static Thread startWriter(final int id,
                                     final PrintStream out)
    {
        Thread th = new Thread() {
                public void run() {
                    for(int ii = 0; ii < 1000; ii++)
                        out.println(id);
                };
            };

        th.start();

        return th;
    }

    public static void main( String[] args )
    {
        try {
            startWriter(1, System.out).join();
        } catch(Throwable th) {
            log.error("Uncaught error.", th);
        }

        log.info("end run.");
    }
}
