
 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          
 //PROJECT:       SAIM-calibration
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Nico Stuurman
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

import java.awt.Dimension;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;

/**
 *
 * @author nico
 */
public class CalibrationPanel extends JPanel {
   private final ScriptInterface gui_;
   private final Preferences prefs_;
   private final CMMCore core_;
   
   private final String SERIALPORT = "serialport";
   private final String TIRFDEVICE = "tirfdevice";
   
   public CalibrationPanel (ScriptInterface gui, Preferences prefs) {
      super(new MigLayout(
              "",
              "[left]15[center") );
      gui_ = gui;
      core_ = gui_.getMMCore();
      prefs_ = prefs;
      
      add(new JLabel("Select Serial Port"));
      JComboBox serialPortBox = new JComboBox();
      serialPortBox.setMaximumSize(new Dimension(200, 30));
      StrVector serialPorts = core_.getLoadedDevicesOfType(DeviceType.SerialDevice);
      for (int i = 0; i < serialPorts.size(); i++) {
         serialPortBox.addItem(serialPorts.get(i));
      }
      serialPortBox.setSelectedItem(prefs_.get(SERIALPORT, serialPorts.get(0)));
      add(serialPortBox, "wrap");
      
      add(new JLabel("Select TIRF motor device"));
      JComboBox tirfMotorBox = new JComboBox();
      tirfMotorBox.setMaximumSize(new Dimension(200, 30));
      StrVector genericPorts = core_.getLoadedDevicesOfType(DeviceType.GenericDevice);
      for (int i = 0; i < genericPorts.size(); i++) {
         tirfMotorBox.addItem(genericPorts.get(i));
      }
      StrVector stagePorts = core_.getLoadedDevicesOfType(DeviceType.StageDevice);
      for (int i = 0; i < stagePorts.size(); i++) {
         tirfMotorBox.addItem(stagePorts.get(i));
      }
      tirfMotorBox.setSelectedItem(prefs_.get(TIRFDEVICE, ""));
      add(tirfMotorBox, "wrap");

   }
           
}
