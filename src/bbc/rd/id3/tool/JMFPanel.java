/**
 * Copyright 2005 British Broadcasting Corporation
 *
 * This file is part of the BBC R&D ID3v2 Chapter Tool application.
 *
 * The BBC R&D ID3v2 Chapter Tool application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 *
 * The BBC R&D ID3v2 Chapter Tool application is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with 
 * the BBC R&D ID3v2 Chapter Tool application; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package bbc.rd.id3.tool;

import javax.media.*;
import javax.media.protocol.DataSource;
import javax.media.protocol.URLDataSource;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//=================================================================================================
public class JMFPanel extends JPanel implements ControllerListener {

  private static final long serialVersionUID = 0;
  protected Player player;
  private Component	control = null;
  private Component gainControl = null;
  public boolean playerRunning = false;
  private double time = 0;
  // Bug fix: correct JMF media time errors due to ID3v2 tag data.
  private double duration; 
  private double offset = 0;

//-------------------------------------------------------------------------------------------------
  public JMFPanel() {
	this.setLayout(new BorderLayout());
	this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Media Player "));
	this.setToolTipText("Media player window. Use 'File:Open' to select audio file.");
    this.setPreferredSize(new Dimension(250,50));
    this.setMinimumSize(new Dimension(250,50));
  } 
//-------------------------------------------------------------------------------------------------
  public void setPlayer(Player player) {
     this.player = player;
     addControllerListener(this);
     this.player.realize();
  }
//-------------------------------------------------------------------------------------------------
  public void closePlayer() {
    if(player != null) {
      player.stop();
      player.close();
      player = null;
    }
    if(control != null) {
      this.remove(control);
      control = null;
    }
  }
//-------------------------------------------------------------------------------------------------
 /*
  * Creates a player from an audio file.
  */
  public void makePlayer(String filename) {
    URL url = null;
    try {
      if ((url = new URL("file:" + filename)) == null) {
        System.err.println("Can't build URL for " + filename);
      }
      try {
        Player player = Manager.createPlayer(url);
        setPlayer(player);
       
        // Bug fix: correct JMF media time errors due to ID3v2 tag data.
        player.realize();
        while(player.getDuration() == Duration.DURATION_UNKNOWN) { 
          try {
            Thread.sleep(100L);
          } catch (InterruptedException e) {
          }
        }
        duration = player.getDuration().getSeconds();
        offset = getTimeOffset(filename);
        setTime(0);
        // End bux fix.
      } catch (NoPlayerException e) {
        System.err.println("Error: " + e);
      }
    } catch (MalformedURLException e) {
      System.out.println("MalformedURLException "+ url);
      System.err.println("Error: " + e);
    } catch (IOException e) {
      System.err.println("Error: " + e);
    }
  }
//-------------------------------------------------------------------------------------------------
  /*
   * Creates a player from an audio file.
   */
   public void makePlayer(String filename, double time) {
     this.time = time;
     makePlayer(filename);
   }
//-------------------------------------------------------------------------------------------------
  public void controllerUpdate(ControllerEvent ce) {
    // System.out.println("ControllerEvent = " + ce);
    if(ce instanceof RealizeCompleteEvent) {
      player.prefetch();
      player.setMediaTime(new Time((double)time));
    } else if (ce instanceof PrefetchCompleteEvent) {
      if((control = player.getControlPanelComponent()) != null) {
        this.add("Center", control);
        control.setVisible(true);
      }
      validate();
    }
  }
// -------------------------------------------------------------------------------------------------
  // Method to jump to a certain time (in seconds).
  public void setTime(double time) {
    // Bug fix: correct JMF media time errors due to ID3v2 tag data.
    time += offset;
    if(player != null) {
      if(player.getState() != Player.Unrealized || player.getState() != Player.Realizing) {
        player.setMediaTime(new Time((double)time));
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  // Method to stop player at a certain time (in seconds).
  public void setStopTime(double time) {
    // Bug fix: correct JMF media time errors due to ID3v2 tag data.
    time += offset;
    if(player != null) {
      if(player.getState() != Player.Unrealized || player.getState() != Player.Realizing) {
        player.setStopTime(new Time((double)time));
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  // Method to get the current media time (in seconds).
  public double getTime() {
    double currentTime;
    if(player != null) {
      currentTime = player.getMediaTime().getSeconds();
    } else {
      currentTime = -1;
    }
    // Bug fix: correct JMF media time errors due to ID3v2 tag data.
    currentTime = Math.max(0, currentTime - offset);
    return currentTime;
  }
//-------------------------------------------------------------------------------------------------
  // Method to get the media end time (in seconds).
  public double getMediaEndTime() {
    double mediaEndTime;
    if(player != null) {
      mediaEndTime = player.getDuration().getSeconds();
    } else {
      mediaEndTime = -1;
    }
    // Bug fix: correct JMF media time errors due to ID3v2 tag data.
    mediaEndTime = Math.max(0, mediaEndTime - offset);
    return mediaEndTime;
  }
//-------------------------------------------------------------------------------------------------
  // Method to toggle stop (pause) and start the player.
  public void pause() {
    if(playerRunning) {
      player.stop();
      playerRunning = false;
    } else {
      player.start();
      playerRunning = true;
    }
  }
//-------------------------------------------------------------------------------------------------
  // Method to stop (pause) the player.
  public void stop() {
    if(playerRunning) {
      player.stop();
      playerRunning = false;
    }
  }
//-------------------------------------------------------------------------------------------------
  // Method to start the player.
  public void start() {
    if(!playerRunning) {
      player.start();
      playerRunning = true;
    }
  }
//-------------------------------------------------------------------------------------------------
  // Bug fix: correct JMF media time errors due to ID3v2 tag data.
  protected double getTimeOffset(String filename) {
    File file = new File(filename);
    BufferedInputStream bis;
    try {
      bis = new BufferedInputStream(new FileInputStream(file));
      long file_length = file.length();

      byte[] header = new byte[10];
      bis.read(header, 0, 10);
      bis.close();

      if(header[0] == 0x49 && header[1] == 0x44 && header[2] == 0x33) {
        // System.out.println("header[6] = " + header[6]);
        // System.out.println("header[7] = " + header[7]);
        // System.out.println("header[8] = " + header[8]);
        // System.out.println("header[9] = " + header[9]);
        long tag_length = (((long)(header[6] & 0x7F)) << 21) + (((long)(header[7] & 0x7F)) << 14) + (((long)(header[8] & 0x7F)) << 7) + ((long)(header[9] & 0x7F)) + 10L;
        // System.out.println("tag_length = " + tag_length);
        // System.out.println("file_length = " + file_length);
        // System.out.println("duration = " + duration);
        offset = (duration * Double.longBitsToDouble(tag_length)) / Double.longBitsToDouble(file_length);
      } else {
        offset = 0;
      }
      // System.out.println("JMFPanel: offset = " + offset);
      return offset;
    } catch(Exception e) {
      System.out.println("Unable to read " + filename);
      return 0D;
    }
  }
//-------------------------------------------------------------------------------------------------
  public Dimension getMaximumSize() {
    Dimension size = getPreferredSize();
    size.width = Short.MAX_VALUE;
    return size;
  }
//-------------------------------------------------------------------------------------------------
  public void addControllerListener(ControllerListener listener) {
    player.addControllerListener(listener);
  }
//-------------------------------------------------------------------------------------------------
  public void removeControllerListener(ControllerListener listener) {
    player.removeControllerListener(listener);
  }
//-------------------------------------------------------------------------------------------------
//  public void paint(Graphics g) {
//    super.paint(g);
//    g.setColor(Color.DARK_GRAY);
//    g.drawRect(30, 29, 1, 3);
//  }
//-------------------------------------------------------------------------------------------------
}
//===================================================================================================
