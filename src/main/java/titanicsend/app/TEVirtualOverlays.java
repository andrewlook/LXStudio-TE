package titanicsend.app;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter.Units;

public class TEVirtualOverlays extends LXComponent {

  public final BooleanParameter speakersVisible =
          new BooleanParameter("Speakers")
                  .setDescription("Toggle whether speakers are visible")
                  .setValue(false);

  public final BooleanParameter vertexLabelsVisible =
          new BooleanParameter("Vertex Labels")
                  .setDescription("Toggle whether vertex labels are visible")
                  .setValue(false);

  public final BooleanParameter panelLabelsVisible =
          new BooleanParameter("Panel Labels")
                  .setDescription("Toggle whether panel labels are visible")
                  .setValue(false);

  public final BooleanParameter unknownPanelsVisible =
          new BooleanParameter("Unk Panels")
                  .setDescription("Toggle whether unknown panels are visible")
                  .setValue(true);

  public final BooleanParameter opaqueBackPanelsVisible =
          new BooleanParameter("Backings")
                  .setDescription("Toggle whether to render the back of lit panels as opaque")
                  .setValue(true);

  public final CompoundParameter backingOpacity =
          new CompoundParameter("Backing Opacity", 1, 0, 1)
                  .setUnits(Units.PERCENT_NORMALIZED)
                  .setDescription("Sets the opacity of the panel backings, when On");

  public final BooleanParameter powerBoxesVisible =
          new BooleanParameter("Power boxes")
                  .setDescription("Toggle whether to show power boxes")
                  .setValue(false);

  public final BooleanParameter lasersVisible =
      new BooleanParameter("Lasers Visible")
              .setDescription("Toggle whether lasers are visible")
              .setValue(false);

  public final BoundedParameter laserBoxSize =
      new BoundedParameter("Laser Box Size", 400000, 100000, 755000)
      .setDescription("Size of sound objects");

/* SAVED FOR MIGRATION TO CHROMATIK

  private static class POV {
    LXVector v;
    int rgb;

    POV(LXVector v, int rgb) {
      this.v = v;
      this.rgb = rgb;
    }
  }
  private static final int numPOVs = 10;

  private final LXVector groundNormal = new LXVector(0,1,0);
  private final LXVector groundMountainPoint = new LXVector(-20e6F, 0, 0);
  private final LXVector mountainNormal = new LXVector(-1, 0, 0);
  private final List<List<POV>> laserPOV;

  private HashMap<TEVertex, Integer> vertex2Powerboxes;
*/

  public TEVirtualOverlays(LX lx) {
    super(lx);

    addParameter("vertexSpheresVisible", this.speakersVisible);
    addParameter("vertexLabelsVisible", this.vertexLabelsVisible);
    addParameter("panelLabelsVisible", this.panelLabelsVisible);
    addParameter("unknownPanelsVisible", this.unknownPanelsVisible);
    addParameter("opaqueBackPanelsVisible", this.opaqueBackPanelsVisible);
    addParameter("backingOpacity", this.backingOpacity);
    addParameter("powerBoxesVisible", this.powerBoxesVisible);
    addParameter("lasersVisible", this.lasersVisible);
  }

/* SAVED FOR MIGRATION TO CHROMATIK

    this.laserPOV = new ArrayList<>();
    for (int i = 0; i < numPOVs; i++) {
      this.laserPOV.add(new ArrayList<>());
    }

    vertex2Powerboxes = loadPowerboxes();
  }

  public HashMap<TEVertex, Integer> loadPowerboxes() {
    TEWholeModel model = TEApp.wholeModel;

    int i = 0;
    HashMap<TEVertex, Integer> v2p = new HashMap<>();

    //Disabling this until we work out a representation for the new power-box model
    Scanner s = model.loadFile("power_assignments.tsv");

    while (s.hasNextLine()) {
      String line = s.nextLine();

      // skip our header
      i++;
      if (i == 1) {
        TE.log("skipping header...");
        continue;
      }

      // parse the line!
      try {
        String[] tokens = line.split("\\t");
        assert tokens.length == 8;

        // parse fields
        String jbox = tokens[4].strip();
        int jboxVertex = Integer.parseInt(jbox.split("-")[0]);
        int jboxIdx = Integer.parseInt(jbox.split("-")[1]);

        // collect vertices that are powerboxes
        if (!v2p.containsKey(jboxVertex)) {
          v2p.put(model.vertexesById.get(jboxVertex), 1);
        }

        if (jboxIdx > 0) {
          v2p.put(model.vertexesById.get(jboxVertex), jboxIdx + 1);
        }

      } catch (Exception e) {
        TE.err(e.toString());
        e.printStackTrace();
        TE.err(line);
      }
    }

    return v2p;
  }

  // https://stackoverflow.com/questions/5666222/3d-line-plane-intersection
  private LXVector laserIntersection(LXVector planeNormal, LXVector planePoint,
                                     LXVector linePoint, LXVector lineDirection) {
    float numerator = planeNormal.dot(planePoint) - planeNormal.dot(linePoint);
    float denominator = planeNormal.dot(lineDirection.normalize());
    if (denominator == 0.0) return null;
    float t = numerator / denominator;
    return linePoint.copy().add(lineDirection.normalize().mult(t));
  }

  @Override
  public void onDraw(UI ui, VGraphics vg) {

    // if powerboxes are on, set opaque to false
    if (this.powerBoxesVisible.isOn())
      this.opaqueBackPanelsVisible.setValue(false);

    final int backingOpacity = (int) (this.backingOpacity.getNormalized() * 255);

    beginDraw(ui, vg);
    vg.noStroke();
    vg.textSize(40);
    for (Map.Entry<Integer, TEVertex> entry : model.vertexesById.entrySet()) {
      TEVertex v = entry.getValue();
      vg.pushMatrix();
      vg.translate(v.x, v.y, v.z);
      vg.ambientLight(255, 255, 255);
      vg.noLights();
      if (this.vertexLabelsVisible.getValueb()) {
        // Vertex labels are further outset past vertexSpheres by different percentages of x and z,
        // with hand-picked values to provide ovular clearance for the rotated labels below.
        vg.translate(v.x * .15f, 0, v.z * .02f);
        // Squashing z (the long fore-aft dimension) before rotating text to be normal to a radial
        vg.rotateY((float) (Math.atan2(v.x, v.z/5) + Math.PI));  // Face out
        vg.scale(10000, -10000);
        vg.fill(128, 128, 128);
        vg.text(entry.getKey().toString(), 0, 0, 0);
      }
      vg.popMatrix();

      // should we draw a powerbox (or 2) here?
      vg.pushMatrix();
      vg.translate(v.x, v.y, v.z);
      vg.ambientLight(255, 255, 255);
      vg.noLights();
      if (this.powerBoxesVisible.getValueb()) {
        if (vertex2Powerboxes.containsKey(v)) {  //HashMap<TEVertex, Integer>
          int boxCount = vertex2Powerboxes.get(v);
          if (boxCount == 1) {
            vg.translate(-1 * v.x * .2f, 0, -1 * v.z * .02f);
            vg.rotateY((float) (Math.atan2(v.x, v.z/5) + Math.PI));  // Face out
            vg.fill(255, 255, 0);
            vg.sphere(100000);
          } else {
            vg.translate(-1 * v.x * .2f, 0, -1 * v.z * .02f);
            vg.rotateY((float) (Math.atan2(v.x, v.z/5) + Math.PI));  // Face out
            vg.fill(255, 255, 0);
            vg.sphere(100000);
            vg.translate(2 * 100000, 0, 0);
            vg.fill(100, 100, 0);
            vg.sphere(100000);
          }
        }
      }
      vg.popMatrix();
    }

    for (Map.Entry<String, TEPanelModel> entry : model.panelsById.entrySet()) {
      TEPanelModel p = entry.getValue();
      if (p.virtualColor != null) {
        // respect unknown panel rendering ui toggle.
        if (p.panelType.equals(TEPanelModel.UNKNOWN) && !this.unknownPanelsVisible.isOn()) {
          continue;
        }
        vg.fill(p.virtualColor.rgb, p.virtualColor.alpha);
        vg.beginShape();
        vg.vertex(p.v0.x, p.v0.y, p.v0.z);
        vg.vertex(p.v1.x, p.v1.y, p.v1.z);
        vg.vertex(p.v2.x, p.v2.y, p.v2.z);
        vg.endShape();
      }

      if (this.opaqueBackPanelsVisible.isOn() && p.panelType.equals(TEPanelModel.LIT)) {
        LXVector[] inner = p.offsetTriangles.inner;
        vg.fill(LXColor.rgb(0,0,0), backingOpacity);
        vg.beginShape();
        vg.vertex(inner[0].x, inner[0].y, inner[0].z);
        vg.vertex(inner[1].x, inner[1].y, inner[1].z);
        vg.vertex(inner[2].x, inner[2].y, inner[2].z);
        vg.endShape();
      }

      // Label each panel
      if (this.panelLabelsVisible.getValueb()) {
        vg.pushMatrix();
        LXVector centroid = p.centroid;
        // Panel labels are outset from their centroid by different percentages of x and z,
        // with hand-picked values to provide ovular clearance for the rotated labels below.
        vg.translate(centroid.x * 1.15f, centroid.y, centroid.z * 1.02f);

        // Squashing z (the long fore-aft dimension) before rotating text to be normal to a radial
        vg.rotateY((float) (Math.atan2(centroid.x, centroid.z/5) + Math.PI));  // Face out
        //pg.rotateY((float) (-Math.PI / 2.0));  // Face port (non-show) side
        //pg.rotateY((float) (Math.PI / 2.0));  // Face starboard (show) side

        vg.scale(10000, -10000);
        vg.fill(255, 0, 0);
        vg.textAlign(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE);
        vg.text(entry.getKey(), 0, 0, -100000);
        vg.popMatrix();
      }
    }

    // should we draw the speakers?
    if (this.speakersVisible.getValueb()) {
      // draw speakers as rectangular prisms
      for (TEBox box : model.boxes) {
        vg.fill(LXColor.rgb(50, 60, 40), 255);
        for (List<LXVector> face : box.faces) {
          vg.beginShape();
          for (LXVector corner : face) {
            vg.vertex(corner.x, corner.y, corner.z);
          }
          vg.endShape();
        }
      }
    }

    for (List<POV> povs : this.laserPOV) {
      for (POV p : povs) {
        vg.pushMatrix();
        vg.stroke(p.rgb, 0xA0);
        vg.translate(p.v.x, p.v.y, p.v.z);
        vg.sphere(10000);
        vg.popMatrix();
      }
    }

    List<POV> newPOV = new ArrayList<>();
    for (TELaserModel laser : model.lasersById.values()) {
      if ((laser.color | LXColor.ALPHA_MASK) == LXColor.BLACK) continue;
      LXVector direction = laser.getDirection();
      if (direction == null) continue;
      LXVector groundSpot = laserIntersection(groundNormal, groundMountainPoint,
              laser.origin, direction);

      LXVector mountainSpot = laserIntersection(mountainNormal, groundMountainPoint,
              laser.origin, direction);

      // If the laser is pointed at a very steep upward angle, the math will
      // be so determined to find a spot where it hits the ground anyway that
      // it will conclude the laser must be capable of firing backward. Since
      // this is not a Darth Maul double-sided laser, ignore those "solutions".
      if (groundSpot != null && groundSpot.x > 0) groundSpot = null;
      if (mountainSpot != null && mountainSpot.x > 0) mountainSpot = null;

      LXVector laserSpot;
      if (groundSpot == null && mountainSpot == null) {
        continue;  // Laser never intersects ground or "mountain" plane
      } else if (groundSpot == null) {
        laserSpot = mountainSpot;
      } else if (mountainSpot == null) {
        laserSpot = groundSpot;
      } else if (laser.origin.dist(groundSpot) < laser.origin.dist(mountainSpot)) {
        laserSpot = groundSpot;
      } else {
        laserSpot = mountainSpot;
      }

      vg.stroke(laser.color, 0xA0);
      vg.line(laser.origin.x, laser.origin.y, laser.origin.z, laserSpot.x, laserSpot.y, laserSpot.z);
      vg.pushMatrix();
      vg.stroke(laser.color);
      vg.translate(laserSpot.x, laserSpot.y, laserSpot.z);
      newPOV.add(new POV(laserSpot, laser.color));
      vg.sphere(10000);
      vg.popMatrix();
    }

    this.laserPOV.remove(0);
    this.laserPOV.add(newPOV);

    endDraw(ui, vg);
  } */
}
