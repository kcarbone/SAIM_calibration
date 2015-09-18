 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          FlatFieldPanel
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

package org.micromanager.saim;

import java.util.prefs.Preferences;
import javax.swing.JPanel;
import mmcorej.CMMCore;
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;

/**
 *
 * @author nico
 */
public class FlatFieldPanel extends JPanel {

   ScriptInterface gui_;
   CMMCore core_;
   Preferences prefs_;

   public FlatFieldPanel(ScriptInterface gui, Preferences prefs) {
      super(new MigLayout(
              "",
              ""));
      gui_ = gui;
      core_ = gui_.getMMCore();
      prefs_ = prefs;
      this.setName("FlatField");
   }
}
