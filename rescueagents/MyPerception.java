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
import world.Map;
import world.Path;
import world.Robot;
import world.RobotPercepcion;

/**
 *
 * @author bduvid
 */
public class MyPerception {

    RobotPercepcion base;

    public Integer getDirectionToCell(Cell start, Cell destination) {
        Path path = AStarSearch.search(start, destination, -1);

        if (path == null) {
            RescueFramework.log("robot has path");
            return getDirectionTowards(start, destination);
        } else {
            RescueFramework.log("robot has no path");
        }
        return path.getFirstCell().directionFrom(start);
    }

    public Path getPathToCell(Cell start, Cell destination) {
        return AStarSearch.search(start, destination, -1);
    }

    public boolean isAdjacent(Cell one, Cell other) {
        return (Math.abs(one.getX() - other.getX()) + Math.abs(one.getY() - other.getY())) == 1;
    }

    public Integer getDirectionTowards(Cell start, Cell destination) {
        int rnd = new Random().nextInt();
        if (rnd % 2 == 0) {
            Integer Xresult = getXDirectionTowards(start, destination);
            if (Xresult != null) {
                return Xresult;
            }
            return getYDirectionTowards(start, destination);
        } else {
            Integer Yresult = getYDirectionTowards(start, destination);
            if (Yresult != null) {
                return Yresult;
            }
            return getXDirectionTowards(start, destination);
        }
    }

    public Integer getXDirectionTowards(Cell start, Cell destination) {
        if (start.getX() > destination.getX()) {
            // balra megyünk
            Cell neighbour = start.getAccessibleNeigbour(Action.left());
            if (neighbour != null) {
                return Action.left();
            }
        } else if (start.getX() != destination.getX()) {
            // jobbra megyünk
            Cell neighbour = start.getAccessibleNeigbour(Action.right());
            if (neighbour != null) {
                return Action.right();
            }
        }
        return null;
    }

    public Integer getYDirectionTowards(Cell start, Cell destination) {
        if (start.getY() > destination.getY()) {
            // felfele megyunk
            Cell neighbour = start.getAccessibleNeigbour(Action.up());
            if (neighbour != null) {
                return Action.up();
            }
        } else if (start.getY() != destination.getY()) {
            // lefele
            Cell neighbour = start.getAccessibleNeigbour(Action.right());
            if (neighbour != null) {
                return Action.down();
            }
        }
        return null;
    }

    public MyPerception(RobotPercepcion perception) {
        base = perception;
    }

    /**
     * Return the simulation time
     */
    public int getTime() {
        return base.getTime();
    }

    /**
     * Return all exit cells
     */
    public ArrayList<Cell> getExitCells() {
        return base.getExitCells();
    }

    /**
     * Return all unknown cells
     */
    public ArrayList<Cell> getUnknownCells() {
        return base.getUnknownCells();
    }

    /**
     * Return all discovered injured
     */
    public ArrayList<Injured> getDiscoveredInjureds() {
        return base.getDiscoveredInjureds();
    }

    /**
     * Return all discovered injured in the health range specified
     */
    public ArrayList<Injured> getDiscoveredInjureds(int maxHealth, int minHealth) {
        return base.getDiscoveredInjureds(maxHealth, minHealth);
    }

    /**
     * Return all robots
     */
    public ArrayList<Robot> getRobots() {
        return base.getRobots();
    }

    /**
     * Returns the shortest path to an exit cell
     */
    public Path getShortestExitPath(Cell start) {
        return base.getShortestExitPath(start);
    }

    /**
     * Returns the shortest path to an unknown cell
     */
    public Path getShortestUnknownPath(Cell start) {
        return base.getShortestUnknownPath(start);
    }

    /**
     * Returns the shortest path to an injured
     */
    public Path getShortestInjuredPath(Cell start) {
        return base.getShortestInjuredPath(start);

    }

    /**
     * Returns the shortest path to an injured below a given health limit
     */
    public Path getShortestInjuredPath(Cell start, int maxHealth, int minHealth) {
        return base.getShortestInjuredPath(start, maxHealth, minHealth);
    }

}
