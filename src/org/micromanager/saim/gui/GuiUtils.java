
package org.micromanager.saim.gui;

import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

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
   
   
    /**
     * Utility function to tie a document listener to a textfield so that all
     * changes in the textfield are immediately stored in the preferences
     * @param prefs 
     * @param textComponent
     * @param prefKey 
     */
    public static void tieTextFieldToPrefs(final Preferences prefs, 
            final JTextComponent textComponent, final String prefKey){
      textComponent.getDocument().addDocumentListener(new DocumentListener() {

           @Override
           public void insertUpdate(DocumentEvent e) {
              prefs.put(prefKey, textComponent.getText()); 
           }

           @Override
           public void removeUpdate(DocumentEvent e) {
              prefs.put(prefKey, textComponent.getText());  
           }

           @Override
           public void changedUpdate(DocumentEvent e) {
              prefs.put(prefKey, textComponent.getText());  
           }
        });
    }
   
}
