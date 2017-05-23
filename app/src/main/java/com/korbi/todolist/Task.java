package com.korbi.todolist;

/**
 * Created by korbi on 12.03.17.
 */

public class Task
{
    int id;
    String taskname;
    int isDone;

    public Task(int id, String taskname, int isDone)
    {
        this.id = id;
        this.taskname = taskname;
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

}
