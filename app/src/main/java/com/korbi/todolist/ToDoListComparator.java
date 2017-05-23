package com.korbi.todolist;

import java.util.Comparator;

/**
 * Created by korbi on 5/23/17.
 */

public class ToDoListComparator implements Comparator<Task> 
{
    
    public ToDoListComparator(){}

    public int compare(Task t1, Task t2) {
        return t1.getState() - t2.getState();
    }
    
}
