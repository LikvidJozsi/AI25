/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.awt.Color;
import java.util.Random;
import rescueframework.RescueFramework;
import world.AStarSearch;
import world.Cell;
import world.Injured;
import world.Path;

/**
 *
 * @author bduvi
 */
public class HealingTask extends Task {

    private Injured injured;
    int targethealth;
    private Path lastpath;
    private int lastpathprogress;

    public HealingTask(Injured injured, int targethealth) {
        this.injured = injured;
        this.targethealth = targethealth;
    }

    public void setInjured(Injured injured) {
        this.injured = injured;
    }

    @Override
    public Cell getEndLocation(boolean hasstartedtask) {
        return injured.getLocation();
    }

    @Override
    public int getEndTime(Cell cell, boolean hasstartedtask) {
        int endtime = 0;
        if (cell != injured.getLocation()) {
            Path path = AStarSearch.search(cell, injured.getLocation(), -1);
            if (path != null) {
                endtime += path.getLength();
            } else {
                if (lastpath != null) {
                    endtime += lastpath.getLength() - lastpathprogress;
                } else {
                    endtime += getAirDistance(robot.getLocation(), injured.getLocation()) * 1.5;
                }
            }
        }
        endtime += (int) (Math.max(injured.getHealth() - targethealth, 0) / 50) + 1;
        return endtime;
    }

    @Override
    public int getEndTime() {
        return getEndTime(robot.getLocation(),true);
    }

    @Override
    public Integer internalstep() {

        RescueFramework.log("executing healing task");

        if (robot.getLocation() == injured.getLocation()) {
            if (injured.getHealth() >= targethealth - 50 || !injured.isAlive()) {
                robotcontrol.taskFinished();
                InjuredManager.injuredHealed(injured);
            }
            return Action.heal();
        } else {
            Integer direction;
            Path path = AStarSearch.search(robot.getLocation(), injured.getLocation(), -1);
            if (path != null) {
                lastpath = path;
                lastpathprogress = 1;
                direction = path.getFirstCell().directionFrom(robot.getLocation());
            } else {
                if (lastpath != null) {
                    direction = lastpath.getPath().get(lastpathprogress++).directionFrom(robot.getLocation());
                } else {
                    direction = RobotTaskManager.perception.getDirectionTowards(robot.getLocation(), injured.getLocation());
                }
            }
            if (directionHasObstacle(direction)) {
                return getEscapeDirection();
            }
            return direction;
        }

    }
}
