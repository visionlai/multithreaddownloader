package main;

/**
 * @author vision
 * 展示进度条，以及其他信息
 */
public class ProgressShower {

    /**
     * finishedLength 各个线程已下载完成总字节
     * totalLength 要下载文件总字节
     * downloadTasks 下载线程
     * fileName 文件位置
     * showEnd 动态显示文件有用
     */
    private long finishedLength = 0;
    private long totalLength;
    private DownloadTask[] downloadTasks;
    private String fileName;
    private int showEnd = 0;

    public ProgressShower(long totalLength, DownloadTask[] downloadTasks, String fileName) {
        this.totalLength = totalLength;
        this.downloadTasks = downloadTasks;
        this.fileName = fileName;
    }

    /**
     * 友好显示字节大小
     * @param bytes
     * @return
     */
    private String showBytesFriendly(long bytes) {
        long kb = bytes / 1024;
        if (kb == 0) {
            return bytes + "B";
        }

        float mb = (float) kb / 1024;
        if (mb < 1) {
            return kb + "KB";
        }

        float gb = mb / 1024;
        if (gb < 1) {
            return String.format("%.1f", mb) + "MB";
        }

        return String.format("%.1f", gb) + "GB";
    }

    /**
     * 友好显示时间
     * @param seconds
     * @return
     */
    private String showTimeFriendly(long seconds) {
        long m = seconds / 60;
        if (m == 0) {
            return seconds + "s";
        } else {
            return m + "m " + seconds % 60 + "s";
        }
    }

    /**
     * 根据文件名长度友好显示文件名
     * @return
     */
    private String showFileNameFriendly() {
        int showLength = 10;
        int fileLength = fileName.length();

        showEnd = (showEnd + 1) % (fileLength + showLength);

        // 文件长度不足显示长度，显示全名
        if (fileLength <= showLength) {
            return String.format("%-10s", fileName);
        } else {
            // 显示开始字符数小于显示字符数
            if (showEnd < showLength) {
                return String.format("%10s", fileName.substring(0, showEnd));
            } else if (showEnd < fileLength) {  // 要显示的字符不到文件名最后一个字符
                return fileName.substring(showEnd - showLength, showEnd);
            } else {  // 已显示到文件名最后一个字符
                return String.format("%-10s", fileName.substring(showEnd - showLength));
            }
        }
    }

    public void showProgress() throws InterruptedException {
        // 去除光标
        System.out.print("\033[?25l");

        // 进度条
        int full = 20;
        char[] progress = new char[full];
        for (int i = 0; i < full; i++) {
            progress[i] = ' ';
        }

        String totalFriendLength = showBytesFriendly(totalLength);

        // 消耗时间
        long seconds = 0;
        while (true) {
            // 计时
            seconds++;
            Thread.sleep(1000);

            // 输出文件名
            System.out.print(showFileNameFriendly());

            // 统计各个线程下载完成字节数
            long preFinishedLength = finishedLength;
            finishedLength = 0;
            for (DownloadTask downloadTask: downloadTasks) {
                finishedLength = finishedLength + downloadTask.getFinishedLength();
            }

            // 输出完成百分比
            int finished = (int) (finishedLength * 100 / totalLength);
            System.out.printf("  %3d%s",finished, "%[");

            // 输出进度条
            finished = finished * full / 100;
            for (int i = 0; i < finished; i++) {
                progress[i] = '=';
            }
            System.out.print(progress);

            // 输出完成大小
            System.out.printf("]  %15s", showBytesFriendly(finishedLength) + "/" + totalFriendLength);

            // 输出下载速度
            long speed = finishedLength - preFinishedLength;
            System.out.printf("  %6s/s", showBytesFriendly(speed));

            // 输出剩余时间
            if (speed == 0) {
                System.out.printf("%12s\r", "in ∞m ∞s");
            } else {
                System.out.printf("%12s\r", "in " + showTimeFriendly((totalLength - finishedLength) / speed));
            }

            // 下载完成退出
            if (finishedLength == totalLength) {
                break;
            }
        }

        System.out.print(fileName);

        // 输出进度条
        System.out.print("  100%[");
        for (int i = 0; i < full; i++) {
            progress[i] = '=';
        }
        System.out.print(progress);

        System.out.printf("]  %15s", totalFriendLength + "/" + totalFriendLength);

        // 输出平均速度
        System.out.printf("  %6s/s", showBytesFriendly(totalLength / seconds));

        // 输出总共消耗时间
        System.out.printf("  %12s\n", "costs " + showTimeFriendly(seconds));

        // 恢复光标
        System.out.print("\033[?25h");
    }
}
