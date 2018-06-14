/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rescueagents;

/**
 *
 * @author bduvid
 */
public class Action {
    public static int left(){
        return 3;
    }
    public static int up(){
        return 0;
    }
    public static int right(){
       return 1;
    }
    public static int down(){
        return 2;
    }
    public static int toggleCarry(){
        return 5;
    }
    public static int heal(){
        return 6;
    }
}
