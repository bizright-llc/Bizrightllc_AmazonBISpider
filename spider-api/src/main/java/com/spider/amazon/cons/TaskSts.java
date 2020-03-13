package com.spider.amazon.cons;

public class TaskSts {
    // O-初始录入 U-已上传文件 P-任务启动 W-任务执行中 S-任务执行成功 F-任务执行失败 D-逻辑删除 SC-数据抓取中 SS-数据抓取成功
    public final static String ORIGINAL_INS="O";
    public final static String UPLOAD_FILE="U";
    public final static String TASK_OPEN="P";
    public final static String TASK_WAITING="W";
    public final static String TASK_SUCCESS="S";
    public final static String TASK_FAILD="F";
    public final static String TASK_DELETE="D";
    public final static String TASK_SCRAPY="SC";
    public final static String TASK_SCRAPYSUCC="SS";
}
