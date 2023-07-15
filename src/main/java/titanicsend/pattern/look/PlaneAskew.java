package titanicsend.pattern.look;

import com.jogamp.opengl.math.FloatUtil;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import titanicsend.pattern.TEPerformancePattern;

import static heronarts.lx.color.LXColor.add;
import static java.lang.Math.*;
import static titanicsend.util.TEMath.clamp;

@LXCategory("Look Java Patterns")
public class PlaneAskew extends TEPerformancePattern {
    float huev = 0;

    DiscreteParameter numPlanes = new DiscreteParameter("Number", new String[]{"3", "2", "1"});
    CompoundParameter thickness = new CompoundParameter("Thick", 0.2, 0.1, 0.9);
    class Plane {
        private final SinLFO a;
        private final SinLFO b;
        private final SinLFO c;
        float av = 1;
        float bv = 1;
        float cv = 1;
        float denom = 0.1f;

        Plane(int i) {
            addModulator(a = new SinLFO(-1, 1, 4000 + 1029 * i)).trigger();
            addModulator(b = new SinLFO(-1, 1, 11000 - 1104 * i)).trigger();
            addModulator(c = new SinLFO(-50, 50, 4000 + 1000 * i * ((i % 2 == 0) ? 1 : -1))).trigger();
        }

        void run(double deltaMs) {
            av = a.getValuef();
            bv = b.getValuef();
            cv = c.getValuef();
            denom = FloatUtil.sqrt(av * av + bv * bv);
        }
    }

    final Plane[] planes;
    final int NUM_PLANES = 3;

    public final float minX;
    public final float maxX;
    public final float minY;
    public final float maxY;
    public final float minZ;
    public final float maxZ;

    public final float xRange;
    public final float yRange;
    public final float zRange;

    public PlaneAskew(LX lx) {
        super(lx);

        minX = modelTE.boundaryPoints.minXBoundaryPoint.x;
        maxX = modelTE.boundaryPoints.maxXBoundaryPoint.x;
        minY = modelTE.boundaryPoints.minYBoundaryPoint.y;
        maxY = modelTE.boundaryPoints.maxYBoundaryPoint.y;
        minZ = modelTE.boundaryPoints.minZBoundaryPoint.z;
        maxZ = modelTE.boundaryPoints.maxZBoundaryPoint.z;
        xRange = (maxX - minX);
        yRange = (maxY - minY);
        zRange = (maxZ - minZ);

        planes = new Plane[NUM_PLANES];
        for (int i = 0; i < planes.length; ++i) {
            planes[i] = new Plane(i);
        }

        addParams();
//        pTransX.setValue(1);
//        removeParameter(pRotX);
//        removeParameter(pRotY);
//        removeParameter(pRotZ);
//        removeParameter(pRotX);
//        removeParameter(pSpin);
    }

    protected void addParams() {
        addParameter("thickness", thickness);
        addParameter("numPlanes", numPlanes);
    }

    public void runTEAudioPattern(double deltaMs) {
        clearPixels();  // Sets all pixels to transparent for starters

        //huev = palette.getHue();
        huev = LXColor.h(LXColor.BLACK);
        planes[0].run(deltaMs);
        planes[1].run(deltaMs);
        planes[2].run(deltaMs);

//        System.out.printf("planes[0] - av: %f, bv: %f, cv: %f, denom: %f\n", planes[0].av, planes[0].bv, planes[0].cv, planes[0].denom);
//        System.out.printf("planes[1] - av: %f, bv: %f, cv: %f, denom: %f\n", planes[1].av, planes[1].bv, planes[1].cv, planes[1].denom);
//        System.out.printf("planes[2] - av: %f, bv: %f, cv: %f, denom: %f\n\n", planes[2].av, planes[2].bv, planes[2].cv, planes[2].denom);

        for (LXPoint p : model.points) {
            float d = Float.MAX_VALUE;

            float normX = (p.x / xRange);
            float normY = (p.y / yRange);
            float normZ = (p.z / zRange);

            int i = 0;
            for (Plane plane : planes) {
                if (i++ <= numPlanes.getValuei() - 1) {
                    continue;
                }

                if (plane.denom != 0) {
//                    System.out.printf("norm(x,y,z) = (%f, %f, %f)\n", normX, normY, normZ);
                    float aTerm = plane.av * normX;
                    float bTerm = plane.bv * normY;
                    float abcDenom = abs(aTerm + bTerm + plane.cv) / plane.denom;
//                    System.out.printf("plane.av + normX = %f\n", aTerm);
//                    System.out.printf("plane.bv + normY = %f\n", bTerm);
//                    System.out.printf("abs(aTerm + bTerm + plane.cv) / plane.denom = %f\n", abcDenom);
                    d = min(d, abcDenom);
                }
            }
//            System.out.printf("d = %f\n", d);

            float hueTerm = huev + 100f * planes[0].av * abs(p.y / (yRange)) + 200f * planes[0].bv * abs(p.x / (xRange)); //+ abs(normX) * 30f + abs(normY) * 80f;
//            System.out.printf("huev = %f, abs(normX) * .3f = %f, abs(normY) * .8f = %f\n", d, abs(normX) * .3f, abs(normY) * .8f);
//            System.out.printf("hueTerm = %f\n", d);

//            System.out.println(hueTerm);

            float brightness = clamp(700f * thickness.getValuef() - 10f * d, 0f, 100f);
//            brightness = 100f;
            colors[p.index] = lx.hsb(
                    hueTerm, 100f, brightness
//                    max(0, 100 - .15f * abs(normX)),
//
            );;
        }
    }
}
