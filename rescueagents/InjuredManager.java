/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import rescueagents.RescueSimulator.Bundle;
import rescueagents.RescueSimulator.RobotBundle;
import rescueframework.RescueFramework;
import world.Cell;
import world.Injured;
import world.Path;

/**
 *
 * @author bduvi
 */
public class InjuredManager {

    private static LinkedList<InjuredInfo> discoveredInjureds = new LinkedList<InjuredInfo>();

    public static void update(LinkedList<RescueRobotControl> rescuerobots) {
        List<Injured> injureds = RobotTaskManager.perception.getDiscoveredInjureds();
        // azzal ne foglalkozzunk ami már megvan mentve
        for (Injured injured : injureds) {
            if (injured.isSaved()) {
                continue;
            }
            boolean registered = false;
            for (InjuredInfo info : discoveredInjureds) {
                if (info.injured == injured) {
                    registered = true;
                    break;
                }
            }
            if (registered) {
                continue;
            }
            InjuredInfo newinjured = new InjuredInfo();
            newinjured.injured = injured;
            discoveredInjureds.add(newinjured);
        }
    }

    public LinkedList<InjuredInfo> getDiscoveredInjured() {
        return discoveredInjureds;
    }

    public static void saveCriticalPatients() {
        for (InjuredInfo info : discoveredInjureds) {
            //RescueFramework.log("evaluating injured, health:" + info.injured.getHealth()
            //        + " carrytime:" + info.carrytime + " reachtime: " + info.reachtime + "being rescued:" + info.beingrescued);
            // ((info.carrytime  + 15 - info.reachtime)*2.3 > info.injured.getHealth()
            Path exitpath = null;
            int exitpathlength = -1;
            ArrayList<RescueRobotControl> allrescuerobots = RobotTaskManager.allrescuerobots;
            if (!info.beingrescued) {
                exitpath = RobotTaskManager.perception.getShortestExitPath(info.injured.getLocation());
                if (exitpath == null) {
                    for (RescueRobotControl robot : allrescuerobots) {
                        Path pathtoinjured = RobotTaskManager.perception.getPathToCell(robot.getLocation(), info.injured.getLocation());
                        Path pathtoexit = RobotTaskManager.perception.getShortestExitPath(robot.getLocation());
                        if (pathtoinjured != null && pathtoexit != null) {
                            exitpathlength = pathtoinjured.getLength() + pathtoexit.getLength();
                        }
                    }
                } else {
                    exitpathlength = exitpath.getLength();
                }
            }
            if (exitpathlength != -1) {
                ArrayList<RobotBundle> robotinfo = RescueSimulator.calculateRobotInfo(info.injured);
                Collections.sort(robotinfo, new RobotBundle.TotalTimeComparator());
                RobotBundle closestrobot = robotinfo.get(0);
                int injuredsavetime = closestrobot.finishtime + closestrobot.reachtime + exitpathlength + 2;
                info.savetime = injuredsavetime;
                // ha mar menthetetlen a beteg, ne foglalkozzunk vele
                if (injuredsavetime > info.injured.getHealth()) {
                    continue;
                }

                if (injuredsavetime + 40 > info.injured.getHealth()) {
                    //RescueFramework.log("enyhen surgos a beteg");
                    // ha enyhen surgos a szallitas
                    // nezzuk meg hogy van e nagyon kozeli robot
                    Collections.sort(robotinfo, new RobotBundle.ReachTimeComparator());
                    RobotBundle verycloserobot = null;
                    for (RobotBundle bundle : robotinfo) {
                        if (bundle.finishtime != 0) {
                            continue;
                        }
                        if (bundle.reachtime < 4) {
                            verycloserobot = bundle;
                        }
                    }
                    if (verycloserobot != null) {
                        dispatchRescue(info, (RescueRobotControl) verycloserobot.robot);
                        continue;
                    }
                }

                if (injuredsavetime + 25 > info.injured.getHealth()) {
                    //RescueFramework.log("surgos a beteg");
                    // ha kozepesen kritikus a betegmegnezzuk hogy hany robot tudja megmenteni
                    LinkedList<RobotBundle> fastenoughrobots = new LinkedList<>(robotinfo);
                    for (Iterator<RobotBundle> iterator = fastenoughrobots.iterator(); iterator.hasNext();) {
                        RobotBundle next = iterator.next();
                        if (next.finishtime + next.reachtime + exitpathlength + 12 > info.injured.getHealth()) {
                            iterator.remove();
                        }
                    }
                    if (!fastenoughrobots.isEmpty()) {
                        RobotBundle mostefficientrobot = Collections.min(fastenoughrobots, new RobotBundle.ReachTimeComparator());
                        dispatchRescue(info, (RescueRobotControl) mostefficientrobot.robot);
                    } else {
                        dispatchRescue(info, (RescueRobotControl) closestrobot.robot);
                    }
                }
            }
        }
    }

    public static void utilizeLeftOverCapacity(LinkedList<RescueRobotControl> rescuerobots, LinkedList<MedicalRobotControl> medicalrobots) {
        if (rescuerobots.isEmpty() && medicalrobots.isEmpty()) {
            return;
        }
        Collections.sort(discoveredInjureds);
        for (InjuredInfo injured : discoveredInjureds) {
            if ((injured.isBeyondSaving(injured) && (injured.savetime) > 48)
                    || (!injured.beingrescued && RobotTaskManager.perception.getShortestExitPath(injured.injured.getLocation()) == null)) {
                continue;
            }
            if (!rescuerobots.isEmpty()) {
                if (!injured.beingrescued) {
                    injured.beingrescued = true;
                    dispatchRescue(injured.injured, rescuerobots);
                }
            } else {
                if (!medicalrobots.isEmpty() && !injured.beingrescued) {
                    injured.beingrescued = true;
                    dispatchHealer(injured.injured, medicalrobots);
                } else {
                    break;
                }
            }
        }
    }

    public static void dispatchRescue(InjuredInfo injured, RescueRobotControl robot) {
        injured.beingrescued = true;
        CarryInjured task = new CarryInjured();
        task.setInjured(injured.injured);
        robot.newTask(task);
    }

    public static void dispatchRescue(Injured injured, LinkedList<RescueRobotControl> robots) {
        RobotControl robot = RescueSimulator.getClosestRobot(robots, injured).closestrobot;
        CarryInjured task = new CarryInjured();
        task.setInjured(injured);
        robot.newTask(task);
        robots.remove((RescueRobotControl) robot);
    }

    public static boolean dispatchHealer(Injured injured) {
        LinkedList<MedicalRobotControl> medics = RobotTaskManager.getMedicalRobots();
        if (medics.isEmpty()) {
            return false;
        }
        RobotControl robot = RescueSimulator.getClosestRobot(medics, injured).closestrobot;
        HealingTask task = new HealingTask(injured, 150);
        robot.newTask(task);
        return true;
    }

    public static void dispatchHealer(Injured injured, LinkedList<MedicalRobotControl> medics) {
        if (medics.isEmpty()) {
            return;
        }
        RobotControl robot = RescueSimulator.getClosestRobot(medics, injured).closestrobot;
        HealingTask task = new HealingTask(injured, 150);

        robot.newTask(task);
        medics.remove((MedicalRobotControl) robot);
    }

    public static void injuredHealed(Injured injured) {
        for (Iterator<InjuredInfo> iterator = discoveredInjureds.iterator(); iterator.hasNext();) {
            InjuredInfo next = iterator.next();
            if (next.injured == injured) {
                next.beingrescued = false;
            }
        }
    }

    public static void injuredSaved(Injured injured) {
        for (Iterator<InjuredInfo> iterator = discoveredInjureds.iterator(); iterator.hasNext();) {
            InjuredInfo next = iterator.next();
            if (next.injured == injured) {
                iterator.remove();
            }
        }
    }

    static class InjuredInfo implements Comparable<InjuredInfo> {

        public int savetime;
        public Injured injured;
        boolean beingrescued = false;

        public InjuredInfo() {
        }

        public boolean isBeyondStaving() {
            return isBeyondSaving(this);
        }

        public void refreshReachTime() {

        }

        private boolean isBeyondSaving(InjuredInfo o) {
            return savetime > o.injured.getHealth();
        }

        @Override
        public int compareTo(InjuredInfo o) {
            boolean thisinjuredunsaveable = isBeyondSaving(this);
            boolean otherinjuredunsaveable = isBeyondSaving(o);
            if (thisinjuredunsaveable && otherinjuredunsaveable) {
                return 0;
            }
            if (thisinjuredunsaveable) {
                return 1;
            }
            if (otherinjuredunsaveable) {
                return -1;
            }
            if (injured.getHealth() == o.injured.getHealth()) {
                return 0;
            }
            if (injured.getHealth() > o.injured.getHealth()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
