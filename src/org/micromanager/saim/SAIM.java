/**
 *
 * Nico Stuurman, 2012, Kate Carbone 2015
 * copyright Regents of the University of California
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

import mmcorej.CMMCore;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;


public class SAIM implements MMPlugin {
   public static String menuName = "SAIM";
   public static String tooltipDescription = "A plugin to control the special tool that measures the angle of light coming out of an objective. "
		   + "Not very useful unless you have that nice little gadget.";
   private ScriptInterface gui_;
   private SAIMFrame myFrame_;

   @Override
   public void setApp(ScriptInterface app) {
      gui_ = app;  
      if (myFrame_ == null)
         myFrame_ = new SAIMFrame(gui_);
      myFrame_.setVisible(true);
      
      // Used to change the background layout of the form.  Does not work on Windows
      gui_.addMMBackgroundListener(myFrame_);
   }

   @Override
   public void dispose() {
      // nothing todo:
   }

   @Override
   public void show() {
   }

   public void configurationChanged() {
   }

   @Override
   public String getInfo () {
      return "Saim plugin";
   }

   @Override
   public String getDescription() {
      return tooltipDescription;
   }
   
   @Override
   public String getVersion() {
      return "1.0";
   }
   
   @Override
   public String getCopyright() {
      return "Regents of the University of California, 2015";
   }
}
