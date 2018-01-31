/**************************************************************************************
 * dandelion_tree
 * Copyright (c) 2014-2017 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 **************************************************************************************/

package remixlab.processing;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import remixlab.core.Node;
import remixlab.primitives.Frame;

public class Shape extends Node {
  Scene _scene;
  PShape _frontShape, _backShape;

  public enum Highlighting {
    NONE, FRONT, FRONT_BACK, BACK
  }

  Highlighting _highlight;

  public Shape(Scene scene) {
    //this(scene, (PShape)null);
    super(scene);
    _scene = scene;
    if (_scene.frontBuffer() instanceof PGraphicsOpenGL)
      setPrecision(Precision.EXACT);
    setHighlighting(Highlighting.FRONT);
  }

  public Shape(Shape reference) {
    super(reference);
    _scene = reference.scene();

    if (_scene.frontBuffer() instanceof PGraphicsOpenGL)
      setPrecision(Precision.EXACT);
    setHighlighting(Highlighting.FRONT);
  }

  public Shape(Scene scene, PShape shape) {
    this(scene);
    set(shape);
  }

  public Shape(Shape reference, PShape shape) {
    this(reference);
    set(shape);
  }

  protected Shape(Scene otherGraph, Shape otherShape) {
    super(otherGraph, otherShape);
    this._frontShape = otherShape._frontShape;
    this._backShape = otherShape._backShape;
  }

  @Override
  public Shape get() {
    return new Shape(scene(), this);
  }

  public Scene scene() {
    return _scene;
  }

  /**
   * Enables highlighting of the frame visual representation when picking takes place
   * ({@link #grabsInput()} returns {@code true}) according to {@code mode} as follows:
   * <p>
   * <ol>
   * <li>{@link Highlighting#NONE}: no highlighting
   * takes place.</li>
   * <li>{@link Highlighting#FRONT}: the
   * front-shape (see {@link #setFront(PShape)}) is scaled by a {@code 1.15}
   * factor.</li>
   * <li>{@link Highlighting#BACK}: the
   * picking-shape (see {@link #setBack(PShape)} is displayed instead of the
   * front-shape.</li>
   * <li>{@link Highlighting#FRONT_BACK}:
   * both, the front and the picking shapes are displayed.</li>
   * </ol>
   * <p>
   * Default is {@link Highlighting#FRONT}.
   *
   * @see #highlighting()
   */
  public void setHighlighting(Highlighting mode) {
    _highlight = mode;
  }

  /**
   * Returns the highlighting mode.
   *
   * @see #setHighlighting(Highlighting)
   */
  public Highlighting highlighting() {
    return _highlight;
  }

  @Override
  public void setPrecision(Precision precision) {
    if (precision == Precision.EXACT)
      if (scene()._bb == null) {
        System.out.println("Warning: EXACT picking precision is not enabled by your PGraphics.");
        return;
      }
    this._Precision = precision;
    // enables or disables the grabbing buffer
    if (precision() == Precision.EXACT) {
      scene()._bbEnabled = true;
      return;
    }
    for (Node node : scene().nodes())
      if (node instanceof Shape)
        if (node.precision() == Precision.EXACT) {
          scene()._bbEnabled = true;
          return;
        }
    scene()._bbEnabled = false;
  }

  /**
   * Same as {@code draw(scene.pg())}.
   *
   * @see remixlab.processing.Scene#traverse(PGraphics)
   */
  public void draw() {
    draw(scene().frontBuffer());
  }

  /**
   * Draw the visual representation of the node into the given PGraphics using the
   * current point of view (see
   * {@link remixlab.processing.Scene#applyTransformation(PGraphics, Frame)}).
   * <p>
   * This method is internally called by {@link Scene#traverse(PGraphics)} to draw
   * the node into the {@link Scene#backBuffer()} and by {@link #draw()} to draw
   * the node into the scene main {@link Scene#frontBuffer()}.
   */
  public boolean draw(PGraphics pGraphics) {
    pGraphics.pushMatrix();
    Scene.applyWorldTransformation(pGraphics, this);
    visit(pGraphics);
    pGraphics.popMatrix();
    return true;
  }

  @Override
  public void visit() {
    visit(scene()._targetPGraphics);
  }

  protected void visit(PGraphics pg) {
    if (scene().eye() == this)
      return;
    if (pg != scene().backBuffer()) {
      pg.pushStyle();
      pg.pushMatrix();
            /*
            if(_frontShape != null)
                pg.shape(_frontShape);
            set(pg);
            setFront(pg);
            //*/
      ///*
      //TODO needs more thinking
      switch (highlighting()) {
        case FRONT:
          if (grabsInput())
            pg.scale(1.15f);
        case NONE:
          if (_frontShape != null)
            pg.shape(_frontShape);
          else
            set(pg);
          break;
        case FRONT_BACK:
          if (_frontShape != null)
            pg.shape(_frontShape);
          else
            setFront(pg);
          if (grabsInput()) {
            if (_backShape != null)
              pg.shape(_backShape);
            else
              setBack(pg);
          }
          break;
        case BACK:
          if (grabsInput()) {
            if (_backShape != null)
              pg.shape(_backShape);
            else
              setBack(pg);
          } else {
            if (_frontShape != null)
              pg.shape(_frontShape);
            else
              setFront(pg);
          }
          break;
      }
      //*/
      pg.popStyle();
      pg.popMatrix();
    } else {
      if (precision() == Precision.EXACT) {
        float r = (float) (_id & 255) / 255.f;
        float g = (float) ((_id >> 8) & 255) / 255.f;
        float b = (float) ((_id >> 16) & 255) / 255.f;
        // funny, only safe way. Otherwise break things horribly when setting shapes
        // and there are more than one iFrame
        pg.shader(scene()._triangleShader);
        pg.shader(scene()._lineShader, PApplet.LINES);
        pg.shader(scene()._pointShader, PApplet.POINTS);

        scene()._triangleShader.set("id", new PVector(r, g, b));
        scene()._lineShader.set("id", new PVector(r, g, b));
        scene()._pointShader.set("id", new PVector(r, g, b));
        pg.pushStyle();
        pg.pushMatrix();
                /*
                if (_backShape != null)
                    pg.shape(_backShape);
                set(pg);
                setBack(pg);
                //*/
        ///*
        if (_frontShape != null)
          pg.shapeMode(scene().frontBuffer().shapeMode);
        if (_backShape != null)
          pg.shape(_backShape);
        else {
          set(pg);
          setBack(pg);
        }
        //*/
        pg.popStyle();
        pg.popMatrix();
      }
    }
  }

  protected void set(PGraphics pg) {
  }

  protected void setFront(PGraphics pg) {
  }

  protected void setBack(PGraphics pg) {
  }

  public void set(PShape shape) {
    setFront(shape);
    setBack(shape);
  }

  public void setFront(PShape shape) {
    _frontShape = shape;
  }

  public void setBack(PShape shape) {
    _backShape = shape;
  }

  /**
   * An interactive-frame may be picked using
   * <a href="http://schabby.de/picking-opengl-ray-tracing/">'ray-picking'</a> with a
   * color buffer (see {@link remixlab.processing.Scene#backBuffer()}). This method
   * compares the color of the {@link remixlab.processing.Scene#backBuffer()} at
   * {@code (x,y)} with {@link #_id()}. Returns true if both colors are the same, and false
   * otherwise.
   * <p>
   * This method is only meaningful when this node is not an eye.
   *
   * @see #setPrecision(Precision)
   */
  @Override
  public final boolean track(float x, float y) {
    if (this == scene().eye()) {
      Scene.showOnlyEyeWarning("checkIfGrabsInput", false);
      return false;
    }
    if (precision() != Precision.EXACT)
      return super.track(x, y);
    int index = (int) y * scene().width() + (int) x;
    if (scene().backBuffer().pixels != null)
      if ((0 <= index) && (index < scene().backBuffer().pixels.length))
        return scene().backBuffer().pixels[index] == _id();
    return false;
  }
}