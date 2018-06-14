/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import rescueframework.RescueFramework;
import world.Injured;
import world.Robot;

/**
 *
 * @author bduvid
 */
public class RobotTaskManager {

    public static int IDcounter = 0;

    public static MyPerception perception;

    private static int lastTurn = -1;

    public static ArrayList<RescueRobotControl> allrescuerobots = new ArrayList<>();
    public static ArrayList<MedicalRobotControl> allmedicalrobots = new ArrayList<>();

    private static LinkedList<MedicalRobotControl> idleMedicalRobots = new LinkedList<MedicalRobotControl>();
    private static LinkedList<RescueRobotControl> idleRescueRobots = new LinkedList<RescueRobotControl>();

    private static LinkedList<RescueRobotControl> scoutingRescueRobots = new LinkedList<RescueRobotControl>();
    private static LinkedList<MedicalRobotControl> scoutingMedicalRobots = new LinkedList<MedicalRobotControl>();

    public static void registerRobot(RescueRobotControl robot) {
        idleRescueRobots.add(robot);
        allrescuerobots.add(robot);
    }

    public static void registerRobot(MedicalRobotControl robot) {
        idleMedicalRobots.add(robot);
        allmedicalrobots.add(robot);
    }

    public static LinkedList<RescueRobotControl> getRescueRobots() {
        LinkedList<RescueRobotControl> rescue = new LinkedList<>(idleRescueRobots);
        rescue.addAll(scoutingRescueRobots);
        return rescue;
    }

    public static LinkedList<MedicalRobotControl> getMedicalRobots() {
        LinkedList<MedicalRobotControl> rescue = new LinkedList<>(idleMedicalRobots);
        rescue.addAll(scoutingMedicalRobots);
        return rescue;
    }

    public static LinkedList<MedicalRobotControl> getIdleMedicalRobots() {
        return new LinkedList<MedicalRobotControl>(idleMedicalRobots);
    }

    public static LinkedList<RescueRobotControl> getIdleRescueRobots() {
        return new LinkedList<RescueRobotControl>(idleRescueRobots);
    }

    public static LinkedList<RobotControl> getIdleRobots() {
        LinkedList<RobotControl> list = new LinkedList<>(idleMedicalRobots);
        list.addAll(idleRescueRobots);
        return list;
    }

    public static void stopAllScouting() {
        for (Iterator<MedicalRobotControl> iterator = scoutingMedicalRobots.iterator(); iterator.hasNext();) {
            MedicalRobotControl next = iterator.next();
            next.deleteAllTasks();
            idleMedicalRobots.add(next);
            iterator.remove();
        }
        for (Iterator<RescueRobotControl> iterator = scoutingRescueRobots.iterator(); iterator.hasNext();) {
            RescueRobotControl next = iterator.next();
            next.deleteAllTasks();
            idleRescueRobots.add(next);
            iterator.remove();
        }
    }

    public static void robotBecameIdle(RescueRobotControl robot) {
        if (scoutingRescueRobots.contains(robot)) {
            scoutingRescueRobots.remove(robot);
        }
        idleRescueRobots.add(robot);
    }

    public static void robotRecievedTask(RescueRobotControl robot, Task task) {
        idleRescueRobots.remove(robot);
        scoutingRescueRobots.remove(robot);
        if (task instanceof ScoutingTask) {
            scoutingRescueRobots.add(robot);
        }
    }

    public static void robotRecievedTask(MedicalRobotControl robot, Task task) {
        idleMedicalRobots.remove(robot);
        scoutingMedicalRobots.remove(robot);
        if (task instanceof ScoutingTask) {
            scoutingMedicalRobots.add(robot);
        }
    }

    public static void robotBecameIdle(MedicalRobotControl robot) {
        if (scoutingMedicalRobots.contains(robot)) {
            scoutingMedicalRobots.remove(robot);
        }
        idleMedicalRobots.add(robot);
    }

    public static void robotStartedScouting(MedicalRobotControl robot) {
        idleMedicalRobots.remove(robot);
        scoutingMedicalRobots.add(robot);
    }

    public static void robotStartedScouting(RescueRobotControl robot) {
        idleRescueRobots.remove(robot);
        scoutingRescueRobots.add(robot);
    }

    public static void update() {
        if (perception.getTime() == lastTurn) {
            return;
        } else {
            lastTurn = perception.getTime();
        }
        if (perception.getTime() == 1) {
            ArrayList<Robot> robots = perception.getRobots();
            for (Iterator<RescueRobotControl> iterator = idleRescueRobots.iterator(); iterator.hasNext();) {
                RescueRobotControl next = iterator.next();
                if (!robots.contains(next.getRobot())) {
                    iterator.remove();
                }
            }
            for (Iterator<MedicalRobotControl> iterator = idleMedicalRobots.iterator(); iterator.hasNext();) {
                MedicalRobotControl next = iterator.next();
                if (!robots.contains(next.getRobot())) {
                    iterator.remove();
                }
            }
            for (Iterator<RescueRobotControl> iterator = allrescuerobots.iterator(); iterator.hasNext();) {
                RobotControl next = iterator.next();
                if (!robots.contains(next.getRobot())) {
                    iterator.remove();
                }
            }
            for (Iterator<MedicalRobotControl> iterator = allmedicalrobots.iterator(); iterator.hasNext();) {
                RobotControl next = iterator.next();
                if (!robots.contains(next.getRobot())) {
                    iterator.remove();
                }
            }
            ScoutingManager.reset();
        }
        LinkedList<RescueRobotControl> rescuerobots = getRescueRobots();
        InjuredManager.update(rescuerobots);
        InjuredManager.saveCriticalPatients();
        ScoutingManager.update();
        InjuredManager.utilizeLeftOverCapacity(idleRescueRobots, idleMedicalRobots);
        RescueFramework.log("numofrescuebots" + allrescuerobots.size());
    }

}
