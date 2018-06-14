/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.LinkedList;
import rescueframework.AbstractRobotControl;
import rescueframework.RescueFramework;
import world.AStarSearch;
import world.Cell;
import world.Injured;
import world.Robot;
import world.RobotPercepcion;

/**
 * RobotControl class to MedicalRobot to implement custom robot control
 * strategies
 */
public class MedicalRobotControl extends AbstractRobotControl implements RobotControl {

    private int ID;

    private LinkedList<Task> tasks;

    /**
     * Default constructor saving world robot object and percepcion
     *
     * @param robot The robot object in the world
     * @param percepcion Percepcion of all robots
     */
    public MedicalRobotControl(Robot robot, RobotPercepcion percepcion) {
        super(robot, percepcion);
        RobotTaskManager.perception = new MyPerception(percepcion);
        RobotTaskManager.registerRobot(this);
        tasks = new LinkedList<>();
        this.ID = RobotTaskManager.IDcounter++;
        ScoutingManager.init();
    }

    @Override
    public boolean equals(Object other) {
        return this.ID == ((MedicalRobotControl) other).ID;
    }

    @Override
    public int getID() {
        return ID;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Robot getRobot() {
        return robot;
    }

    public void addTask(Task task) {
        task.setRobot(robot, this);
        tasks.add(task);
        RobotTaskManager.robotRecievedTask(this, task);
    }

    public void newTask(Task task) {
        tasks.clear();
        addTask(task);
    }

    public Cell getLocation() {
        return robot.getLocation();
    }

    /**
     * Custom step strategy of the robot, implement your robot control here!
     *
     * @return Return NULL for staying in place, 0 = step up, 1 = step right, 2
     * = step down, 3 = step left, 5 = pick up / put down inured, 6 = heal
     */
    public Integer step() {
       RobotTaskManager.update();
        if (!tasks.isEmpty()) {
            return tasks.peek().step();
        }else{
            if(robot.getLocation().isExit()){
                for(int i = 0; i<4;i++){
                    Cell neighbour = robot.getLocation().getAccessibleNeigbour(i);
                    if(neighbour != null && !neighbour.hasRobot() && !neighbour.isExit())
                        return neighbour.directionFrom(robot.getLocation());
                }
            }
        }
        return null;
    }

    @Override
    public void taskFinished() {
        tasks.pop();
        if (tasks.isEmpty()) {
            RobotTaskManager.robotBecameIdle(this);
        }
    }

    @Override
    public LinkedList<Task> getTasks() {
        return tasks;
    }

}
