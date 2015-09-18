
package org.micromanager.saim.gui;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

/**
 *
 * @author nico
 */
public class GuiUtils {
   /**
    * makes border with centered title text
    * @param title
    * @return
    */
   public static TitledBorder makeTitledBorder(String title) {
      TitledBorder myBorder = BorderFactory.createTitledBorder(
              BorderFactory.createLineBorder(Color.BLACK), title);
      myBorder.setTitleJustification(TitledBorder.CENTER);
      return myBorder;
   }
   
}
