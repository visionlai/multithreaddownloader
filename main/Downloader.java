package main;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author vision
 * 下载主类
 */
public class Downloader {

    /**
     * 线程池
     */
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    public void multiThreadDownload(URL url, String location, int threadNum) throws IOException, InterruptedException {
        DownloadTask[] tasks = new DownloadTask[threadNum];

        // 获取总下载字节
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        long totalLength = con.getContentLengthLong();

        // 各个线程负责下载的字节数
        long lengthPerThread = totalLength / threadNum;

        // 创建线程
        for (int i = 0; i < threadNum - 1; i++) {
            tasks[i] = new DownloadTask(url, location, i * lengthPerThread, (i + 1) * lengthPerThread - 1);
        }
        tasks[threadNum - 1] = new DownloadTask(url, location, (threadNum - 1) * lengthPerThread, totalLength - 1);

        // 添加到线程执行
        for (DownloadTask task: tasks) {
            pool.execute(task);
        }
        pool.shutdown();

        // 主线程显示进度条
        ProgressShower progressShower = new ProgressShower(totalLength, tasks, location);
        progressShower.showProgress();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2 && args.length != 3) {
            System.out.println("java -cp . main.Main url fileName [threadNum]");
        } else {
            URL url = new URL(args[0]);
            String fileName = args[1];
            Downloader downloader = new Downloader();
            if (args.length == 2) {
                downloader.multiThreadDownload(url, fileName, 100);
            } else {
                int threadNum = Integer.valueOf(args[2]);
                downloader.multiThreadDownload(url, fileName, threadNum);
            }
        }
    }
}
