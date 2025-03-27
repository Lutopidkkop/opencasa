/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import functions.Kinematics;
import functions.SignalProcessing;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author Carlos Alquezar
 *
 */
public class OscillatoryWalker extends Simulation {

  class SimulatedCell {

    float amplitude;
    float dist;
    double f;
    double phi;
    int sizex;
    int sizey;
    float t;
    double T;
    double w;
    float y;

    SimulatedCell() {
      sizex = 10;
      sizey = 8;
      t = 0;
      y = height / 2;
      amplitude = 100;
      T = 350;
      f = 1 / T;// 0.01;
      w = 2 * Math.PI * f;
      phi = 0;
    }

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    double distance(float x1, float y1, float x2, float y2) {
      return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * @param ip
     */
    void update(ImageProcessor ip) {

      float prevT = t;
      float prevY = y;
      Random rand = new Random();
      // double epsilon = 2*rand.nextGaussian();
      // Update variables
      t += 1;// T/width;
      // Sinusoidal function
      y = (float) (amplitude * Math.sin(w * t + phi)) + height / 2;
      // float x = (float) (amplitude*Math.cos(w*t+phi)+epsilon)+height/2;
      // Triangular function
      // y=(float)
      // ((2*amplitude/Math.PI)*Math.asin(Math.sin(2*Math.PI*t/T)))+height/2;
      dist += distance(prevT, prevY, t, y);
      // Draw SimulatedCell
      ip.fillOval((int) t, (int) y, sizex, sizey);
      // Save position
      Cell p = new Cell();
      p.x = t;
      p.y = y;
      track.add(p);
    }
  }

  /**   */
  int cellCount = 1;
  /**   */
  int height = 800;
  /**   */
  int SIMLENGTH = 700;
  /**   */
  SimulatedCell[] sperm = new SimulatedCell[cellCount];
  // Point[][] tracks = new Point[cellCount][SIMLENGTH];
  /**   */
  List<Cell> track = new ArrayList<Cell>();

  /**   */
  int width = 800;

  /**   */
  public OscillatoryWalker() {
    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x] = new SimulatedCell();
    }
  }

  /**
   * @return
   */
  public ImagePlus createSimulation() {
    ImageStack imStack = new ImageStack(width, height);
    for (int i = 0; i < SIMLENGTH; i++) {
      ImageProcessor ip = new ByteProcessor(width, height);
      draw(ip);
      imStack.addSlice(ip);
    }
    Kinematics kinematics = new Kinematics();
    SignalProcessing sp = new SignalProcessing();
    for (int x = cellCount - 1; x >= 0; x--) {
      // System.out.println("Distance: "+sperm[x].dist);
      // System.out.println("Time: "+sperm[x].t);
      double vsl = track.get(0).distance(track.get(track.size() - 1)) / track.size();
      double vcl = sperm[x].dist / sperm[x].t;
      List<Cell> avgTrack = sp.movingAverage(track);
      double vap = kinematics.vcl(avgTrack);
      double lin = vsl / vcl;
      double wob = vap / vcl;
      System.out.println("VSL: " + vsl);
      System.out.println("VCL: " + vcl);
      System.out.println("VAP: " + vap);
      System.out.println("LIN: " + lin);
      System.out.println("WOB: " + wob);
    }
    return new ImagePlus("Simulation", imStack);
  }

  /**
   * @param ImageProcessor
   *          ip
   */
  void draw(ImageProcessor ip) {
    ip.setColor(Color.black);
    ip.fill();
    ip.setColor(Color.white);
    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x].update(ip);
    }
  }

  /**
   * 
   */
  public void run() {
    createSimulation().show();
  }

}
