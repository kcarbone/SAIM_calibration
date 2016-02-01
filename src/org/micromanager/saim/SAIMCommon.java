/**
 * Nico Stuurman and Kate Carbone 2015
 * Copyright Regents of the University of California
 *  
 * LICENSE:      This file is distributed under the BSD license.
 *               License text is included with the source distribution.
 *
 *               This file is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty
 *               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 */

package org.micromanager.saim;

import java.util.prefs.Preferences;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.exceptions.SAIMException;

/**
 * Functions that are used in multiple panels
 */
public class SAIMCommon {

   /**
    * Utility to convert angle to tirf positions using our polynomial equation
    * @param prefs - Java prefs used to store our coefficients
    * @param gui
    * @param angle Desired angle
    * @return Tirf motor position
    */
   public static int tirfPosFromAngle(Preferences prefs, final ScriptInterface gui, double angle) {
      // TirfPosition = slope * angle plus Offset
      // Output motor position must be an integer to be interpreted by TITIRF

      double tempPos = (Double.parseDouble(PrefUtils.parseCal(3, prefs, gui)) * Math.pow(angle, 3)
              + Double.parseDouble(PrefUtils.parseCal(2, prefs, gui)) * Math.pow(angle, 2)
              + Double.parseDouble(PrefUtils.parseCal(1, prefs, gui)) * angle
              + Double.parseDouble(PrefUtils.parseCal(0, prefs, gui)));
      int pos = Math.round((float) tempPos);
      return pos;
   }

   /**
    * This code runs the actual acquisition while flat-fielding and when executing
    * an acquisition.  
    * 
    * @param gui MMScriptInterface
    * @param prefs Java Preferences used to store all our data
    * @param rootDir where to save this acquisition (if desired)
    * @param acqName
    * @param show whether or not to show this acquisition
    * @param save whether or not to sava this acquisition
    * @return
    * @throws Exception 
    */
   public static String runAcquisition(final ScriptInterface gui,
           final Preferences prefs, final String rootDir, final String acqName, 
           final boolean show, final boolean save) 
           throws Exception {
      
      CMMCore core = gui.getMMCore();
      double startAngle = Double.parseDouble(prefs.get(PrefUtils.STARTANGLE, "0.0"));
      if (startAngle > 0) {
         throw new SAIMException ("Start angle should be <= 0");
      }
      double angleStepSize = prefs.getDouble(PrefUtils.ANGLESTEPSIZE, 0);
      boolean doubleZero = Boolean.parseBoolean(prefs.get(PrefUtils.DOUBLEZERO, ""));
      if (startAngle % angleStepSize != 0) {
         throw new SAIMException("Start angle is not divisible by the angle step size");
      }
      
      // Set these variables to the correct values and leave
      final String deviceName = prefs.get(PrefUtils.TIRFDEVICE, "");
      final String propName = prefs.get(PrefUtils.TIRFPROP, "");

      // Usually no need to edit below this line
      double tempnrAngles = Math.abs(startAngle) * 2 / angleStepSize;
      int nrAngles = (Integer) Math.round((float) tempnrAngles);

      //gui_.closeAllAcquisitions();
      String acq = gui.getUniqueAcquisitionName(acqName);

      int nrFrames = nrAngles + 1;
      if (doubleZero) {
         nrFrames = nrAngles + 2;
      }

      gui.openAcquisition(acq, rootDir, 1, 1, nrFrames, 1, show, save); 

      boolean doubled = false;
      int frameNr = 0;
      for (double angle = startAngle; angle <= -startAngle; angle += angleStepSize) {
         double pos = SAIMCommon.tirfPosFromAngle(prefs, gui, angle);
         gui.message("Angle: " + Double.toString(angle) + ", position: " + Double.toString(pos));
         core.setProperty(deviceName, propName, pos);
         core.waitForDevice(deviceName);
         //gui.sleep(250);
         core.snapImage();
         TaggedImage taggedImg = core.getTaggedImage();
         taggedImg.tags.put("Angle", angle);
         gui.addImageToAcquisition(acq, 0, 0, frameNr, 0, taggedImg);
         frameNr++;
         if (doubleZero && !doubled && (angle == 0) ) {
            angle = -angleStepSize;
            doubled = true;
         }
      }
      
      return acq;
   }
}
