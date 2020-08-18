package com.github.hcsp.multithread;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.hcsp.multithread.Utils.*;

public class MultiThreadWordCount1 {
    // 使用threadNum个线程，并发统计文件中各单词的数量
    public static Map<String, Integer> count(int threadNum, List<File> files) throws IOException, InterruptedException {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        AtomicInteger restFileNumber = new AtomicInteger(files.size());
        for (File file : files) {
            new Thread(() -> {
                try {
                    Map<String, Integer> countResult = countFile(file);
                    synchronized (result) {
                        //这里使用merge到第一个result中，是因为runable没有返回值；
                        mergeIntoFirstMap(result, countResult);
                        if (restFileNumber.decrementAndGet() == 0) {
                            result.notify();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        synchronized (result) {
            while (restFileNumber.intValue() > 0) {
                result.wait();
            }
        }

        return result;
    }
}
//这里真正要解决的是协同的问题，要让主线程能够汇聚各个工人的手头完成的工作；
//一开始还卡了下思维，担心主线程因为意外卡了，后执行到result.wait();其实这不是正好吗，就是担心主线程执行的太快了，才有协同这个设置；
