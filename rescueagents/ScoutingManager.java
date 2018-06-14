/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import rescueframework.RescueFramework;
import world.Cell;
import world.Path;
import world.Robot;

/**
 *
 * @author bduvi
 */
public class ScoutingManager {

    private static int mapsize;

    private static Stage currentstage;

    public static void init() {
        if (currentstage == null) {
            mapsize = RobotTaskManager.perception.getUnknownCells().size();
            currentstage = new EarlyStage();
        }
    }

    public static void update() {
        currentstage.update();
        RescueFramework.log("Scouting allas: " + currentstage.toString());
    }

    public static void targetScoutingFinished(Cell target) {
        currentstage.scoutingFinished(target);
    }

    private static abstract class Stage {

        public abstract void update();

        public void scoutingFinished(Cell cell) {
        }
    }
    
    
    public static void reset() {
        currentstage = new EarlyStage();
    }

    private static class EarlyStage extends Stage {

        public void update() {
            ArrayList<Robot> robots = RobotTaskManager.perception.getRobots();
            boolean scoutablecellexists = false;
            for (Robot robot : robots) {
                if (RobotTaskManager.perception.getShortestUnknownPath(robot.getLocation()) != null) {
                    scoutablecellexists = true;
                }
            }
            if (RobotTaskManager.perception.getUnknownCells().size() * 30 < mapsize || !scoutablecellexists) {
                currentstage = new EntirelyExploredStage();
                RobotTaskManager.stopAllScouting();
                currentstage.update();
                return;
            }

            LinkedList<RescueRobotControl> rescuerobots = RobotTaskManager.getIdleRescueRobots();
            for (RescueRobotControl robot : rescuerobots) {
                ScoutingTask task = new ScoutingTask();
                robot.addTask(task);
            }
            LinkedList<MedicalRobotControl> medicalrobots = RobotTaskManager.getIdleMedicalRobots();
            for (MedicalRobotControl robot : medicalrobots) {
                ScoutingTask task = new ScoutingTask();
                robot.addTask(task);
            }
        }
    }

    private static class EntirelyExploredStage extends Stage {

        public void update() {
            // nem csinal semmit, megakadajozza hogy a leomlasok utan szaladozzanak a robotok
            // TODO reaktivalni a scoutolást ha izolalta valik egy beteg/robot
        }
    }
}
