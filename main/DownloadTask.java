package main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * @author vision
 * 下载线程，负责下载一部分字节
 */
public class DownloadTask implements Runnable{

    /**
     * finishedLength 该线程已下载完成字节数
     * url 下载 http 地址
     * file 下载文件存放地址
     * start 开始下载首字节位置
     * end 下载结束字节位置
     * retry 超时异常重连次数
     */
    private long finishedLength = 0;
    private URL url;
    private String file;
    private long start;
    private long end;
    private int retry = 50;

    public DownloadTask(URL url, String file, long start, long end) {
        this.url = url;
        this.file = file;
        this.start = start;
        this.end = end;
    }

    private void download() throws IOException {
        // 重连次数统计
        if (retry == 0) {
            System.out.println("下载失败!");
            System.exit(1);
        }
        retry--;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 重连下载首字节位置
        long s = start + finishedLength;
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Range", "bytes=" + s + "-" + end);

        // 设置超时
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(s);

        BufferedInputStream buffer = null;
        // 写文件
        byte[] b = new byte[1024];
        int len = 0;
        try {
            buffer = new BufferedInputStream(conn.getInputStream());
            while ((len = buffer.read(b)) != -1) {
                raf.write(b, 0, len);
                finishedLength = finishedLength + len;
            }
        } catch(SocketTimeoutException | SocketException e) {
            raf.close();
            if (buffer != null) {
                buffer.close();
            }

            // 超时重连
            download();
        }
        raf.close();
    }

    @Override
    public void run() {
        try {
            download();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("下载失败!");
            System.exit(1);
        }
    }

    public long getFinishedLength() {
        return finishedLength;
    }

}
