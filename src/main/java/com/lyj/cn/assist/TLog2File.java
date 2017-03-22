package com.lyj.cn.assist;

import android.content.Context;
import com.lyj.cn.base.Adhibition;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public class TLog2File {
    public static boolean DEBUG;
    private static Calendar mCal;
    private static TLog2File.LoggerThread mThread;

    public TLog2File() {
    }

    static void log2File(String tag, String log) {
        if(DEBUG) {
            try {
                if(Adhibition.getInstance() != null) {
                    TLog2File.LoggerThread e = getThread(Adhibition.getInstance());
                    if(e != null) {
                        e.addLog(new TLog2File.Log(tag, log));
                    }
                }
            } catch (Throwable var3) {
                var3.printStackTrace();
            }

        }
    }

    static void log2File(String tag, Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        log2File(tag, sw.toString());
    }

    private static TLog2File.LoggerThread getThread(Context context) {
        if(mCal == null) {
            mCal = Calendar.getInstance();
        }

        String fileName = String.format("%s_%s_%s_%s.txt", new Object[]{Integer.valueOf(mCal.get(1)), Integer.valueOf(mCal.get(2) + 1), Integer.valueOf(mCal.get(5)), Integer.valueOf(mCal.get(11))});
        if(mThread == null || !mThread.fileName.equals(fileName)) {
            mThread = new TLog2File.LoggerThread(context, fileName);
            mThread.start();
        }

        return mThread;
    }

    static {
        DEBUG = TLog.DEBUG;
    }

    static class Log {
        String tag;
        String log;

        public Log(String tag, String log) {
            this.tag = tag;
            this.log = log;
        }
    }

    static class LoggerThread extends Thread {
        FileWriter fileWriter;
        String fileName;
        DateFormat formatter;
        LinkedBlockingQueue<TLog2File.Log> logsQueue;

        public LoggerThread(Context context, String fileName) {
            String filePath = context.getExternalFilesDir("logs").getAbsolutePath() + File.separator;
            this.fileName = fileName;
            File file = new File(filePath);
            if(!file.exists()) {
                file.mkdirs();
            }

            file = new File(filePath + File.separator + fileName);

            try {
                if(!file.exists()) {
                    file.createNewFile();
                }

                this.fileWriter = new FileWriter(file.getAbsolutePath(), true);
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            this.logsQueue = new LinkedBlockingQueue();
            this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }

        void addLog(TLog2File.Log log) {
            if(this.fileWriter != null && this.logsQueue != null) {
                this.logsQueue.add(log);
            }

        }

        public void run() {
            super.run();

            while(true) {
                try {
                    TLog2File.Log e = (TLog2File.Log)this.logsQueue.poll(30L, TimeUnit.SECONDS);
                    if(e != null && this.fileWriter != null) {
                        String line = this.formatter.format(TLog2File.mCal.getTime()) + "/" + e.tag + ":" + e.log;

                        try {
                            this.fileWriter.write(line + "\n\r");
                            this.fileWriter.flush();
                        } catch (IOException var5) {
                            var5.printStackTrace();
                        }
                    }
                } catch (InterruptedException var6) {
                    var6.printStackTrace();
                    if(this.fileWriter != null) {
                        try {
                            this.fileWriter.close();
                        } catch (IOException var4) {
                            var4.printStackTrace();
                        }
                    }

                    if(TLog2File.mThread == this) {
                        TLog2File.mThread = null;
                    }

                    return;
                }
            }
        }
    }
}
