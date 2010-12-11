/* 
 * Copyright 2009 James C. Jones, licensed under the terms of the GNU GPL v2 
 * See the COPYING file for details. 
 */
package swing.components;

import java.awt.MenuItem;
import java.util.HashMap;

public class MultiMenuItem extends MenuItem {
    private HashMap<Object, MultiMenuSetting> settings = new HashMap<Object, MultiMenuSetting>();
    
    public void setActionCommand(String command) {
        // TODO Auto-generated method stub
        super.setActionCommand(command);
    }
    
    private static class MultiMenuSetting {
        
    }
}
