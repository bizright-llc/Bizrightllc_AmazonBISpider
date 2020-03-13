package com.spider.amazon.remote.api;

/**
 * @ClassName BaZhuaYuAPI
 * @Description 八爪鱼API路径类
 */
public class BaZhuaYuAPI {

    public static final String TOKEN = "/token";
    public static final String REFRASE_TOKEN = "/token";
    public static final String GET_TASKGROUP="/api/TaskGroup";
    public static final String GET_TASK="/api/Task?taskGroupId={taskGroupId}";
    public static final String GET_TASKSTATUS_BY_IDLIST="/api/task/GetTaskStatusByIdList";
    public static final String GET_TASKRULEPROPERTY_BY_NAME="/api/task/GetTaskRulePropertyByName?taskId={taskId}&name={name}";
    public static final String UPDATE_TASKRULE="/api/task/UpdateTaskRule";
    public static final String ADD_URLORTEXT_TO_TASK="/api/task/AddUrlOrTextToTask";
    public static final String START_TASK="/api/task/StartTask?taskId={taskId}";
    public static final String STOP_TASK="/api/task/StopTask?taskId={taskId}";
    public static final String REMOVE_DATA_BY_TASKID="/api/task/RemoveDataByTaskId?taskId={taskId}";
    public static final String DATA_GET_TOP="/api/notexportdata/gettop?taskId={taskId}&size={size}";
    public static final String GET_UPDATE="/api/notexportdata/update?taskId={taskId}";
    public static final String GET_DATAOFTASK_BY_OFFSET="/api/alldata/GetDataOfTaskByOffset?taskId={taskId}&offset={offset}&size={size}";

}
