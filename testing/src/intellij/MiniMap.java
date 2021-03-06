package intellij;

import frames.core.Frame;
import frames.core.Graph;
import frames.processing.Scene;
import frames.processing.Shape;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.event.MouseEvent;

public class MiniMap extends PApplet {
  Scene scene, minimap, focus;
  Shape[] models;
  Frame sceneEye;
  boolean displayMinimap = true;
  // whilst scene1 is either on-screen or not, the minimap is always off-screen
  // test both cases here:
  boolean onScreen = false;
  boolean interactiveEye;

  int w = 1200;
  int h = 1200;

  //Choose FX2D, JAVA2D, P2D or P3D
  String renderer = P3D;

  public void settings() {
    size(w, h, renderer);
  }

  public void setup() {
    scene = onScreen ? new Scene(this) : new Scene(this, renderer);
    scene.setRadius(1000);
    // set a detached eye frame
    scene.setEye(new Frame());
    if (scene.is2D())
      rectMode(CENTER);
    scene.fit(1);
    models = new Shape[6];
    for (int i = 0; i < models.length; i++) {
      if ((i & 1) == 0) {
        models[i] = new Shape(scene, shape());
      } else {
        models[i] = new Shape(scene) {
          int _faces = (int) MiniMap.this.random(3, 15), _color = color(MiniMap.this.random(255), MiniMap.this.random(255), MiniMap.this.random(255));

          @Override
          public void setGraphics(PGraphics pg) {
            pg.pushStyle();
            pg.fill(_color);
            scene.drawTorusSolenoid(pg, _faces, scene.radius() / 30);
            pg.popStyle();
          }
        };
      }
      scene.randomize(models[i]);
    }

    // Note that we pass the upper left corner coordinates where the scene1
    // is to be drawn (see drawing code below) to its constructor.
    minimap = new Scene(this, renderer, w / 2, h / 2, w / 2, h / 2);
    minimap.setRadius(2000);
    // set a detached eye frame
    //minimap.setEye(new Frame());
    // TODO bug
    if (renderer == P3D)
      minimap.setType(Graph.Type.ORTHOGRAPHIC);
    minimap.fit(1);
    // detached frame
    sceneEye = new Frame();
  }

  PShape shape() {
    PShape shape = renderer == P3D ? createShape(BOX, 60) : createShape(RECT, 0, 0, 80, 100);
    shape.setFill(color(random(0, 255), random(0, 255), random(0, 255)));
    return shape;
  }

  public void keyPressed() {
    if (key == ' ')
      displayMinimap = !displayMinimap;
    if (key == 'i') {
      interactiveEye = !interactiveEye;
      if (interactiveEye)
        minimap.setTrackedFrame(sceneEye);
      else
        minimap.resetTrackedFrame();
    }
    if (key == 'f')
      focus.fit(1);
    if (key == 't')
      if (renderer == P3D)
        if (focus.type() == Graph.Type.PERSPECTIVE)
          focus.setType(Graph.Type.ORTHOGRAPHIC);
        else
          focus.setType(Graph.Type.PERSPECTIVE);
  }

  @Override
  public void mouseMoved() {
    if (!interactiveEye || focus == scene)
      focus.cast();
  }

  @Override
  public void mouseDragged() {
    if (mouseButton == LEFT)
      focus.spin();
    else if (mouseButton == RIGHT)
      focus.translate();
    else
      focus.scale(focus.mouseDX());
  }

  @Override
  public void mouseWheel(MouseEvent event) {
    if (renderer == P3D)
      focus.moveForward(event.getCount() * 20);
    else
      focus.scale(event.getCount() * 50);
  }

  @Override
  public void mouseClicked(MouseEvent event) {
    if (event.getCount() == 2)
      if (event.getButton() == LEFT)
        focus.focus();
      else
        focus.align();
  }

  public void draw() {
    focus = displayMinimap ? (mouseX > w / 2 && mouseY > h / 2) ? minimap : scene : scene;
    if (interactiveEye)
      Frame.sync(scene.eye(), sceneEye);
    background(75, 25, 15);
    if (scene.isOffscreen()) {
      scene.beginDraw();
      scene.frontBuffer().background(75, 25, 15);
      scene.drawAxes();
      scene.traverse();
      scene.endDraw();
      scene.display();
    } else {
      scene.drawAxes();
      scene.traverse();
    }
    if (displayMinimap) {
      scene.shift(minimap);
      if (!scene.isOffscreen())
        scene.beginHUD();
      minimap.beginDraw();
      minimap.frontBuffer().background(125, 80, 90);
      minimap.drawAxes();
      minimap.traverse();
      // draw scene eye
      minimap.frontBuffer().fill(sceneEye.isTracked(minimap) ? 255 : 25, sceneEye.isTracked(minimap) ? 0 : 255, 125);
      minimap.frontBuffer().stroke(0, 0, 255);
      minimap.frontBuffer().strokeWeight(2);
      minimap.drawFrustum(scene);
      minimap.endDraw();
      minimap.display();
      if (!scene.isOffscreen())
        scene.endHUD();
      minimap.shift(scene);
    }
  }

  public static void main(String args[]) {
    PApplet.main(new String[]{"intellij.MiniMap"});
  }
}
