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
    */
   public SAIMFrame(ScriptInterface gui) {
      gui_ = gui;
      core_ = gui_.getMMCore();
      loadAndRestorePosition(100, 100, 200, 200);
      prefs_ = Preferences.userNodeForPackage(this.getClass());

      this.setLayout(new MigLayout("flowx, fill, insets 8"));
      this.setTitle("SAIM calibration");

      tabbedPane_ = new JTabbedPane();

      AcquisitionPanel acqPanel = new AcquisitionPanel(gui_, prefs_);
      FlatFieldPanel ffPanel = new FlatFieldPanel(gui_, prefs_);
      CalibrationPanel calPanel = new CalibrationPanel(gui_, prefs_);
      
      calPanel.addCalibrationObserver(acqPanel);
      calPanel.addCalibrationObserver(ffPanel);
      
      tabbedPane_.add(calPanel);
      tabbedPane_.add(ffPanel);
      tabbedPane_.add(acqPanel);

      this.add(tabbedPane_);
      this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      this.setResizable(false);
      this.setVisible(true);
      this.pack();
   }

}
