 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          SAIMFrame.java
 //PROJECT:       SAIM-calibration
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Nico Stuurman, Kate Carbone
 //
 // COPYRIGHT:    University of California, San Francisco 2015
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.


/**
 * This is a Micro-Manager plugin for using a Scanning Angle Interference
 * Microscopy calibration device. This specific example uses the NetBeans GUI
 * builder. It is based on ExampleFrame.java
 *
 */
package org.micromanager.saim;

import mmcorej.CMMCore;
import java.util.prefs.Preferences;
import org.micromanager.api.ScriptInterface;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;
import org.micromanager.utils.MMFrame;

/**
 *
 * @author nico, kate
 */
public class SAIMFrame extends MMFrame {

    private final ScriptInterface gui_;
    private final CMMCore core_;
    private final Preferences prefs_;

    private final JTabbedPane tabbedPane_;

    /**
     * Constructor
     *
     * @param gui - Reference to MM script interface
    * @throws java.lang.Exception
    */
   public SAIMFrame(ScriptInterface gui) throws Exception {
      gui_ = gui;
      core_ = gui_.getMMCore();
      loadAndRestorePosition(100, 100, 200, 200);
      prefs_ = Preferences.userNodeForPackage(this.getClass());

      this.setLayout(new MigLayout("flowx, fill, insets 8"));
      this.setTitle("SAIM calibration");

      tabbedPane_ = new JTabbedPane();

      final AcquisitionPanel acqPanel = new AcquisitionPanel(gui_, prefs_);
      //final FlatFieldPanel ffPanel = new FlatFieldPanel(gui_, prefs_);
      final CalibrationPanel calPanel = new CalibrationPanel(gui_, prefs_);
      
      tabbedPane_.add(calPanel);
      //tabbedPane_.add(ffPanel);
      tabbedPane_.add(acqPanel);

      tabbedPane_.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent e) {
            // not very elegant.  Would be nice to make an interface for the Panels...
            if (tabbedPane_.getSelectedIndex() == 0) {
               calPanel.updateGUIFromPrefs();
            }
            //if (tabbedPane_.getSelectedIndex() == 2) {
            //   ffPanel.updateGUIFromPrefs();
            //}
            if (tabbedPane_.getSelectedIndex() == 1) {
              acqPanel.updateGUIFromPrefs();
            }
         }
      });

      this.add(tabbedPane_);
      this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      this.setResizable(false);
      this.setVisible(true);
      this.pack();
   }

}
