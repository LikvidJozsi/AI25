/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
import java.util.Random;
import rescueframework.RescueFramework;
import world.Cell;
import world.Path;

/**
 *
 * @author bduvid
 */
public class ScoutingTask extends Task {

    Path previouspath;
    int pathprogress;

    @Override
    protected Integer internalstep() {
        Path path = RobotTaskManager.perception.getShortestUnknownPath(robot.getLocation());
        if (path != null) {
            previouspath = path;
            pathprogress = 1;
            Integer direction = path.getFirstCell().directionFrom(robot.getLocation());
            if (directionHasObstacle(direction)) {
                return getEscapeDirection();
            }
            return direction;
        } else {
            if (previouspath != null && pathprogress < previouspath.getPath().size()) {
                Integer direction = previouspath.getPath().get(pathprogress++).directionFrom(robot.getLocation());
                if (directionHasObstacle(direction)) {
                    return getEscapeDirection();
                }
                return direction;
            }
            return null;
        }
    }

    @Override
    public int getEndTime(Cell startcell, boolean hasstartedtask) {
        return 0;
    }

    @Override
    public Cell getEndLocation(boolean hasstartedtask) {
        return robot.getLocation();
    }

    @Override
    public int getEndTime() {
        return 0;
    }

}
