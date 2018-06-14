package rescueagents;

import java.util.Iterator;
import java.util.LinkedList;
import rescueframework.AbstractRobotControl;
import rescueframework.RescueFramework;
import world.RobotPercepcion;
import world.Injured;
import world.Cell;
import world.Path;
import world.Robot;

public class RescueRobotControl extends AbstractRobotControl implements RobotControl {

    private LinkedList<Task> tasks;
    public int ID;

    public RescueRobotControl(Robot robot, RobotPercepcion percepcion) {
        super(robot, percepcion);
        RobotTaskManager.perception = new MyPerception(percepcion);
        RobotTaskManager.registerRobot(this);
        tasks = new LinkedList<>();
        ID = RobotTaskManager.IDcounter++;
        ScoutingManager.init();
    }

    @Override
    public boolean equals(Object other) {
        return this.ID == ((RescueRobotControl) other).ID;
    }

    public Robot getRobot() {
        return robot;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void addTask(Task task) {
        task.setRobot(robot, this);
        tasks.add(task);
        RobotTaskManager.robotRecievedTask(this, task);
    }

    public void newTask(Task task) {
        for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
            Task next = iterator.next();
            if(next instanceof ScoutingTask)
                iterator.remove();
        }
        addTask(task);
    }
    
    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void taskFinished() {
        tasks.pop();
        if (tasks.isEmpty()) {
            RobotTaskManager.robotBecameIdle(this);
        }
    }

    /**
     * Custom step strategy of the robot, implement your robot control here!
     *
     * @return Return NULL for staying in place, 0 = step up, 1 = step right, 2
     * = step down, 3 = step left, 5 = pick up / put down injured
     */
    @Override
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
    public Cell getLocation() {
        return robot.getLocation();
    }

    @Override
    public LinkedList<Task> getTasks() {
        return tasks;
    }
}
