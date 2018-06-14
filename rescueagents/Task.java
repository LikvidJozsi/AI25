/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.Random;
import world.Cell;
import world.Robot;

/**
 *
 * @author bduvi
 */
public abstract class Task {

    Robot robot;
    RobotControl robotcontrol;
    Cell lastcell;
    int oscillationnum = 0;

    protected abstract Integer internalstep();

    public void setRobot(Robot robot, RobotControl robotcontrol) {
        this.robot = robot;
        this.robotcontrol = robotcontrol;
    }

    protected boolean directionHasObstacle(Integer direction) {
        Cell neighbour = robot.getLocation().getAccessibleNeigbour(direction);
        if (neighbour != null) {
            return neighbour.hasRobot();
        } else {
            return false;
        }
    }

    protected int getAirDistance(Cell one, Cell other) {
        return Math.abs(one.getX() - other.getX()) + Math.abs(one.getY() - other.getY());
    }

    protected Integer getEscapeDirection() {
        for (int i = 0; i < 4; i++) {
            Cell neighbour = robot.getLocation().getAccessibleNeigbour(i);
            if (neighbour != null && !neighbour.hasRobot()) {
                return neighbour.directionFrom(robot.getLocation());
            }
        }
        return null;
    }

    public Integer step() {
        Integer result = internalstep();
        if (result != null && result < 4) {
            Cell destination = robot.getLocation().getAccessibleNeigbour(result);
            if (lastcell != null && destination != null) {
                if (lastcell == destination) {
                    oscillationnum++;
                    if (oscillationnum > 1) {
                        if (new Random().nextInt() % 2 == 0) {
                            lastcell = robot.getLocation();
                            return null;
                        }
                    }
                } else {
                    oscillationnum = 0;
                }
            }
            lastcell = robot.getLocation();
        }
        return result;
    }

    public abstract int getEndTime();

    public abstract int getEndTime(Cell startcell, boolean hasstartedtask);

    public abstract Cell getEndLocation(boolean hasstartedtask);
}
