/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import rescueframework.AbstractRobotControl;
import rescueframework.RescueFramework;
import world.AStarSearch;
import world.Cell;
import world.Injured;
import world.Path;
import world.Robot;

/**
 *
 * @author bduvi
 */
public class RescueSimulator {

    public static int getEstimatedRescueTime(List<RescueRobotControl> robots, Injured injured) {
        Bundle closest = getClosestRobot(robots, injured);
        int time = closest.shortestdistance + 2;
        time += RobotTaskManager.perception.getShortestExitPath(injured.getLocation()).getLength();
        return time;
    }

    public static int getEstimatedCarryTime(Injured injured) {
        Path path = RobotTaskManager.perception.getShortestExitPath(injured.getLocation());
        if (path != null) {
            return path.getLength() + 1;
        }
        // mivel nincs ut nezzuk meg milyn okbol

        ArrayList<Cell> exitpoints = RobotTaskManager.perception.getExitCells();
        return estimateAirDistance(injured.getLocation(), exitpoints);
    }

    public static int estimateAirDistance(Cell start, ArrayList<Cell> destinations) {
        int bestdistanceestimation = getAirDistance(start, destinations.get(0));
        for (int i = 1; i < destinations.size(); i++) {
            int distanceestimation = getAirDistance(start, destinations.get(i));
            if (distanceestimation < bestdistanceestimation) {
                bestdistanceestimation = distanceestimation;
            }
        }
        return (int) (bestdistanceestimation * 1.5);
    }

    public static Cell getClosestAirDistanceCell(Cell start, ArrayList<Cell> destinations) {
        Cell bestcell = destinations.get(0);
        int bestdistanceestimation = getAirDistance(start, destinations.get(0));
        for (int i = 1; i < destinations.size(); i++) {
            int distanceestimation = getAirDistance(start, destinations.get(i));
            if (distanceestimation < bestdistanceestimation) {
                bestdistanceestimation = distanceestimation;
                bestcell = destinations.get(i);
            }
        }
        return bestcell;
    }

    public static int getAirDistance(Cell start, Cell end) {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY());
    }

    @Deprecated
    public static int getClosestRobotDistance(List< ? extends RobotControl> robots, Injured injured) {

        RobotControl closest = robots.get(0);
        int bestDistance = getDistance(robots.get(0), injured, -1);
        for (RobotControl robot : robots) {
            int distance = getDistance(robot, injured, bestDistance);
            if (bestDistance > distance) {
                bestDistance = distance;
                closest = robot;
            }
        }
        return bestDistance;
    }

    public static ArrayList<RobotBundle> calculateRobotInfo(Injured injured) {
        ArrayList<RescueRobotControl> rescuerobots = RobotTaskManager.allrescuerobots;
        ArrayList<RobotBundle> robotinfo = new ArrayList<>(rescuerobots.size());
        for (RescueRobotControl robot : rescuerobots) {
            RobotBundle next = new RobotBundle();
            next.robot = robot;
            setRobotBundleInfo(next, injured);
            robotinfo.add(next);
        }
        return robotinfo;
    }

    public static void setRobotBundleInfo(RobotBundle bundle, Injured injured) {
        LinkedList<Task> tasks = bundle.robot.getTasks();
        if (tasks.isEmpty()) {
            bundle.finishtime = 0;
            bundle.reachtime = getDistance(bundle.robot.getLocation(), injured.getLocation(), -1);
        } else {
            int timesum = 0;
            timesum += tasks.get(0).getEndTime();
            Cell startcell = tasks.get(0).getEndLocation(true);
            for (int i = 1; i < tasks.size(); i++) {
                timesum += tasks.get(i).getEndTime(startcell, false);
                startcell = tasks.get(i).getEndLocation(false);
            }
            bundle.finishtime = timesum;
            bundle.reachtime = getDistance(startcell, injured.getLocation(), -1);
        }
    }

    public static Bundle getClosestRobot(List< ? extends RobotControl> robots, Injured injured) {
        RobotControl closest = robots.get(0);
        int bestDistance = getDistance(robots.get(0), injured, -1);
        for (RobotControl robot : robots) {
            int distance = getDistance(robot, injured, bestDistance);
            if (bestDistance > distance) {
                bestDistance = distance;
                closest = robot;
            }
        }
        return new Bundle(bestDistance, closest);
    }

    public static int getDistance(Cell start, Cell end, int maxlength) {
        Path path = AStarSearch.search(start, end, maxlength);
        if (path != null) {
            return path.getLength();
        }
        return (int) (getAirDistance(start, end) * 1.5);
    }

    public static int getDistance(RobotControl robot, Injured injured, int maxlength) {
        Path path = AStarSearch.search(robot.getLocation(), injured.getLocation(), maxlength);
        if (path != null) {
            return path.getLength();
        }
        return getAirDistance(robot.getLocation(), injured.getLocation());
    }

    public static class Bundle {

        public int shortestdistance;
        public RobotControl closestrobot;

        public Bundle(int shortestdistance, RobotControl robot) {
            this.shortestdistance = shortestdistance;
            closestrobot = robot;
        }
    }

    static class RobotBundle {

        public RobotControl robot;
        public int reachtime;
        public int finishtime;

        public static class TotalTimeComparator implements Comparator<RobotBundle> {

            @Override
            public int compare(RobotBundle o1, RobotBundle o2) {
                int totaltime = o1.finishtime + o1.reachtime;
                int othertotaltime = o2.finishtime + o2.reachtime;
                if (totaltime == othertotaltime) {
                    return 0;
                }
                if (totaltime < othertotaltime) {
                    return -1;
                } else {
                    return 1;
                }
            }

        }

        public static class ReachTimeComparator implements Comparator<RobotBundle> {

            @Override
            public int compare(RobotBundle o1, RobotBundle o2) {
                if (o1.reachtime == o2.reachtime) {
                    return 0;
                }
                if (o1.reachtime < o2.reachtime) {
                    return -1;
                } else {
                    return 1;
                }
            }

        }
    }
}
