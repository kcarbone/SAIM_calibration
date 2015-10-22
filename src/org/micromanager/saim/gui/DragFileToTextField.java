/*
 * Copyright (c) 2015
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.micromanager.saim.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import javax.swing.text.JTextComponent;

/**
 *
 * @author nico
 */
public class DragFileToTextField implements DropTargetListener {
   final JTextComponent theTextField_;
   final boolean dirOnly_;
   
   public DragFileToTextField(JTextComponent theTextField, boolean dirOnly) {
      theTextField_ = theTextField;
      dirOnly_ = dirOnly;
   }
   
   @Override
   public void dragEnter(DropTargetDragEvent dtde) {
      // throw new UnsupportedOperationException("Not supported yet."); 
   }

   @Override
   public void dragOver(DropTargetDragEvent dtde) {
      //throw new UnsupportedOperationException("Not supported yet."); 
   }

   @Override
   public void dropActionChanged(DropTargetDragEvent dtde) {
       //throw new UnsupportedOperationException("Not supported yet."); 
   }

   @Override
   public void dragExit(DropTargetEvent dte) {
       //throw new UnsupportedOperationException("Not supported yet."); 
   }

   @Override
   public void drop(DropTargetDropEvent dtde) {
      try {
         Transferable tr = dtde.getTransferable();
         DataFlavor[] flavors = tr.getTransferDataFlavors();
         for (DataFlavor flavor : flavors) {
            if (flavor.isFlavorJavaFileListType()) {
               dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
               java.util.List list = (java.util.List) tr.getTransferData(flavor);
               for (Object list1 : list) {
                  File f = (File) list1;
                  if ( (f.isFile()  && !dirOnly_) || (f.isDirectory() && dirOnly_) ) {
                     theTextField_.setText(f.getPath());
                  }
                  if ( (f.isFile() && dirOnly_) || (f.isDirectory() && !dirOnly_) ) {
                     theTextField_.setText(f.getParent());
                  }
                  dtde.dropComplete(true);
                  return;
               }
            }
         }
      } catch (UnsupportedFlavorException ex) {
      } catch (IOException ex) {
      }
   }
   
}

