package com.korbi.todolist.logic;

import java.util.Date;

/**
 * Created by korbi on 12.03.17.
 * See github for full sourcecode: github.com/korbi98/todolist
 */

public class Task //Task object with all its attributes
{
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int NO_DEADLINE = 0;
    public static final int JUST_DATE = 1;
    public static final int DATE_AND_TIME = 2;
    private int id;
    private String taskname;
    private int isDone;
    private Date deadline;
    private int priority;
    private int timeIsSet;
    private String category;

    public Task(int id, String taskname, Date deadline, int priority, int isDone, int timeIsSet, String category)
    {
        this.id = id;
        this.taskname = taskname;
        this.deadline = deadline;
        this.priority = priority;
        this.isDone = isDone;
        this.timeIsSet = timeIsSet;
        this.category = category;
    }

    public int getState()
    {
        return isDone;
    }

    public void setState(int state) {
        this.isDone = state;
    }

    public String getTaskname()
    {
        return taskname;
    }

    public void setTaskname(String taskname)
    {
        this.taskname = taskname;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Date getDeadline()
    {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getTimeIsSet()
    {
        return timeIsSet;
    }

    public void setTimeIsSet(int timeIsSet)
    {
        this.timeIsSet = timeIsSet;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Task)) return false;

        Task otherTask = (Task) otherObject;

        return this.getTaskname().equals(otherTask.getTaskname()) &&
                this.getDeadline().equals(otherTask.getDeadline()) &&
                this.getPriority() == otherTask.getPriority() &&
                this.getCategory().equals(otherTask.getCategory());
        // Checks if two tasks a equal, to determine, if a task should be imported or not. The state is not checked because it could easily change after the export of the tasks
    }
}
