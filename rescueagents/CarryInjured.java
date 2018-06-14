/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
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
class CarryInjured extends Task {

    private Injured injured;
    Path previousexitpath;
    Path previousinjuredpath;
    int exitpathprogress;
    int injuredpathprogress;
    int finishtime;
    int finishcalctime = -1;

    public void setInjured(Injured injured) {
        this.injured = injured;
    }

    protected Integer internalstep() {
        Path path;

        RescueFramework.log("executing rescue task, target halth:" + injured.getHealth());

        if (robot.hasInjured()) {
            // ha kijaratnal vagyunk letesszuk a beteget es ertesitjuk a rendszert hogy feladatunkat befejeztuk
            if (robot.getLocation().isExit()) {
                robotcontrol.taskFinished();
                InjuredManager.injuredSaved(injured);
                return Action.toggleCarry();
            }
            //kijarathoz megyunk
            path = RobotTaskManager.perception.getShortestExitPath(robot.getLocation());
            if (path != null) {
                // ha van a kijarat fele ut
                // feljegyezzuk hogy ez egy valid ut
                previousexitpath = path;
                exitpathprogress = 1;
                // llenorizzuk hogy blokkolva van-e az ut 
                Integer direction = path.getFirstCell().directionFrom(robot.getLocation());
                if (directionHasObstacle(direction)) {
                    // ha igen kiterunk
                    return getEscapeDirection();
                }
                // ha nem megyunk az uton
                return direction;
            } else {
                // ha nincs ut 
                if (previousexitpath != null) {
                    // de ezelett volt megnezzunk hogy szabad-e
                    Integer direction = previousexitpath.getPath().get(exitpathprogress++).directionFrom(robot.getLocation());
                    if (directionHasObstacle(direction)) {
                        // ha nem szabad kiterunk
                        return getEscapeDirection();
                    }
                    return direction;
                }
            }
        } else {
            // ha a betegnel vagyunk felvesszuk
            if (robot.getLocation() == injured.getLocation()) {
                return Action.toggleCarry();
            }
            // a beteg fele megyunk
            path = AStarSearch.search(robot.getLocation(), injured.getLocation(), -1);
            if (path != null) {
                // ha van ut
                // beirjuk previouspathba
                previousinjuredpath = path;
                injuredpathprogress = 1;
                // majd megnezzuk hogy szabad-e
                Integer direction = path.getFirstCell().directionFrom(robot.getLocation());
                if (directionHasObstacle(direction)) {
                    // ha nem szabad kiterunk
                    return getEscapeDirection();
                }
                return direction;
            } else {
                // ha nincs ut megnezzuk hogy ezelott volt-e
                if (previousexitpath != null) {
                    // ha van lekérjuk a kovetkezo elemet
                    Integer direction = previousinjuredpath.getPath().get(injuredpathprogress++).directionFrom(robot.getLocation());
                    if (directionHasObstacle(direction)) {
                        // ha nem szabad kiterunk
                        return getEscapeDirection();
                    }
                    return direction;
                } else {
                    // ha ezelott sem volt akkor teszunk egy lepest a cel iranyaba
                    Integer direction = RobotTaskManager.perception.getDirectionTowards(robot.getLocation(), injured.getLocation());
                    if (directionHasObstacle(direction)) {
                        return getEscapeDirection();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getEndTime(Cell start, boolean hasstartedtask) {
        if (finishcalctime == RobotTaskManager.perception.getTime()) {
            return finishtime;
        } else {
            int endtime = 0;
            if (!robot.hasInjured() || !hasstartedtask) {
                Path path = AStarSearch.search(start, injured.getLocation(), -1);
                if (path != null) {
                    endtime += path.getLength();
                } else {
                    if (previousinjuredpath != null) {
                        endtime += previousinjuredpath.getLength() - injuredpathprogress;
                    } else {
                        endtime += getAirDistance(robot.getLocation(), injured.getLocation()) * 1.5;
                    }
                }
                Path exitpath = RobotTaskManager.perception.getShortestExitPath(injured.getLocation());
                if (exitpath != null) {
                    endtime += exitpath.getLength();
                } else {
                    endtime += RescueSimulator.estimateAirDistance(injured.getLocation(), RobotTaskManager.perception.getExitCells());
                }
                finishtime = endtime + 2;
                finishcalctime = RobotTaskManager.perception.getTime();
                return endtime + 2;
            } else {
                Path exitpath = RobotTaskManager.perception.getShortestExitPath(start);
                if (exitpath != null) {
                    endtime += exitpath.getLength();
                } else {
                    endtime += RescueSimulator.estimateAirDistance(start, RobotTaskManager.perception.getExitCells());
                }
                finishtime = endtime + 1;
                finishcalctime = RobotTaskManager.perception.getTime();
                return endtime + 1;
            }
        }
    }

    @Override
    public int getEndTime() {
        return getEndTime(robot.getLocation(), true);
    }

    @Override
    public Cell getEndLocation(boolean hasstartedtask) {
        if (!robot.hasInjured() || !hasstartedtask) {
            Path exit = RobotTaskManager.perception.getShortestExitPath(injured.getLocation());
            if (exit != null) {
                return exit.getPath().get(exit.getPath().size() - 1);
            } else {
                return RescueSimulator.getClosestAirDistanceCell(injured.getLocation(), RobotTaskManager.perception.getExitCells());
            }
        } else {
            Path exit = RobotTaskManager.perception.getShortestExitPath(robot.getLocation());
            if (exit != null) {
                return exit.getPath().get(exit.getPath().size() - 1);
            } else {
                return RescueSimulator.getClosestAirDistanceCell(robot.getLocation(), RobotTaskManager.perception.getExitCells());
            }
        }
    }
}
