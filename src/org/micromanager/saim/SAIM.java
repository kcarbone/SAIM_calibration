/**
 *
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

import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
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

      if (myFrame_!= null) {
         WindowEvent wev = new WindowEvent(myFrame_, WindowEvent.WINDOW_CLOSING);
         myFrame_.dispatchEvent(wev);
         myFrame_ = null;
      }

       try {
           myFrame_ = new SAIMFrame(gui_);
       } catch (Exception ex) {
           Logger.getLogger(SAIM.class.getName()).log(Level.SEVERE, null, ex);
       }
      myFrame_.setVisible(true);
   }

   @Override
   public void dispose() {
      // nothing todo:
   }

   @Override
   public void show() {
      if (myFrame_ == null) {
          try {
              myFrame_ = new SAIMFrame(gui_);
          } catch (Exception ex) {
              Logger.getLogger(SAIM.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
      myFrame_.setVisible(true);
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
