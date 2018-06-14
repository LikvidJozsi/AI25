/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.LinkedList;
import world.Cell;
import world.Robot;

/**
 *
 * @author bduvi
 */
public interface RobotControl {
    public Cell getLocation();
    public void taskFinished();
    public void addTask(Task task);
    public void newTask(Task task);
    public Robot getRobot();
    public int getID();
    public LinkedList<Task> getTasks();
}
