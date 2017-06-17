package com.korbi.todolist.logic;

import java.util.Date;

/**
 * Created by korbi on 12.03.17.
 */

public class Task
{
    private int id;
    private String taskname;
    private int isDone;
    private Date deadline;
    private int priority;

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;

    public Task(int id, String taskname, Date deadline, int priority, int isDone)
    {
        this.id = id;
        this.taskname = taskname;
        this.deadline = deadline;
        this.priority = priority;
        this.isDone = isDone;
    }

    public int getState()
    {
        return isDone;
    }

    public String getTaskname()
    {
        return taskname;
    }

    public void setTaskname(String taskname)
    {
        this.taskname = taskname;
    }

    public void setState(int state)
    {
        this.isDone = state;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setDeadline(Date deadline)
    {
        this.deadline = deadline;
    }

    public Date getDeadline()
    {
        return deadline;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

}
