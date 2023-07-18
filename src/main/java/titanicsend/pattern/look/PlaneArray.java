package titanicsend.pattern.look;

import com.jogamp.opengl.math.FloatUtil;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import titanicsend.pattern.TEPerformancePattern;
import titanicsend.pattern.yoffa.framework.TEShaderView;

import static heronarts.lx.color.LXColor.add;
import static java.lang.Float.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static titanicsend.util.TEMath.clamp;

@LXCategory("Look Java Patterns")
public class PlaneArray extends TEPerformancePattern {

//    public final SinLFO aLFO;
//    public final SinLFO bLFO;
//    public final SinLFO cLFO;
//    public final SinLFO dLFO;

    public final float minX;
    public final float maxX;
    public final float minY;
    public final float maxY;
    public final float minZ;
    public final float maxZ;

    public final float xRange;
    public final float yRange;
    public final float zRange;


//    final CompoundParameter a = new CompoundParameter("a", 0.3, -1.0, 1.0);
//    final CompoundParameter b = new CompoundParameter("b", 0.2, 0.0, 1.0);
//    final CompoundParameter c = new CompoundParameter("c", 0.5, -1.0, 1.0);
//    final CompoundParameter d = new CompoundParameter("d", 0.5, -1.0, 1.0);

    final CompoundParameter thickness = new CompoundParameter("thickness", 0.2);
    DiscreteParameter numPlanes = new DiscreteParameter("Number", new String[]{"3", "2", "1"});

    PlaneAskew.Plane[] planes;

    public PlaneArray(LX lx) {
        super(lx, TEShaderView.ALL_POINTS);

        minX = modelTE.boundaryPoints.minXBoundaryPoint.x;
        maxX = modelTE.boundaryPoints.maxXBoundaryPoint.x;
        minY = modelTE.boundaryPoints.minYBoundaryPoint.y;
        maxY = modelTE.boundaryPoints.maxYBoundaryPoint.y;
        minZ = modelTE.boundaryPoints.minZBoundaryPoint.z;
        maxZ = modelTE.boundaryPoints.maxZBoundaryPoint.z;
        xRange = (maxX - minX);
        yRange = (maxY - minY);
        zRange = (maxZ - minZ);

        addParams();
        //        addCommonControls();

//        aLFO = new SinLFO(minX, maxX, 5000);
//        bLFO = new SinLFO(minY, maxY, 6000);
//        cLFO = new SinLFO(minZ, maxZ, 7000);
//        dLFO = new SinLFO(minZ, maxZ, 7000);
//        addModulator(aLFO).trigger();
//        addModulator(bLFO).trigger();
//        addModulator(cLFO).trigger();
//        addModulator(dLFO).trigger();

        planes = new PlaneAskew.Plane[3];
        for (int i = 0; i < planes.length; ++i) {
            planes[i] = new PlaneAskew.Plane(this, i);
        }
//        planes = new PlaneAskew.Plane(this, 0);
    }

    protected void addParams() {
//        addParameter("a", a);
//        addParameter("b", b);
//        addParameter("c", c);
//        addParameter("d", d);
        addParameter("thickness", thickness);
        addParameter("numPlanes", numPlanes);
    }

    public void runTEAudioPattern(double deltaMs) {
        clearPixels();  // Sets all pixels to transparent for starters

//        float a = this.a.getValuef();
//        float b = this.b.getValuef();
//        float c = this.c.getValuef();
//        float d = this.d.getValuef();
//
//        a = aLFO.getValuef();
//        b = bLFO.getValuef();
//        c = cLFO.getValuef();
//        d = dLFO.getValuef();

//        float denom = sqrt(a*a + b*b + c*c);

        planes[0].run(deltaMs);
        planes[1].run(deltaMs);
        planes[2].run(deltaMs);

        float hue = LXColor.h(LXColor.BLUE);
        float thicknessf = thickness.getValuef();
        float brightnessf = (float) getBrightness();

        float[] mins = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxs = new float[]{Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};


        for (LXPoint p : model.points) {

            float xNorm = p.x / maxX;
            float yNorm = p.y / maxY;
            float zNorm = p.z / maxZ;

            if (xNorm < mins[0]) {
                mins[0] = xNorm;
            } else if (xNorm > maxs[0]) {
                maxs[0] = xNorm;
            }
            if (yNorm < mins[1]) {
                mins[1] = yNorm;
            } else if (yNorm > maxs[1]) {
                maxs[1] = yNorm;
            }
            if (zNorm < mins[2]) {
                mins[2] = zNorm;
            } else if (zNorm > maxs[2]) {
                maxs[2] = zNorm;
            }

            float fromCenter = FloatUtil.sqrt(xNorm * xNorm + yNorm * yNorm + zNorm * zNorm);
//            System.out.printf("fromCenter: %f", fromCenter);
            if (fromCenter < mins[3]) {
                mins[3] = fromCenter;
            } else if (fromCenter > maxs[3]) {
                maxs[3] = fromCenter;
            }

            float d = Float.MAX_VALUE;
            for (int i = 0; i < 3; ++i) {
                float distToPlane = planes[i].dist(zNorm, yNorm);
                d = min(d, distToPlane);
            }
//            float distToPlane = plane.dist(zNorm, yNorm);
//            float distToPlane = abs(a * xNorm + b * yNorm + c * zNorm + d) / denom;

            int col = 0;
            float brightnessTerm = d < (thicknessf * fromCenter) ? 100f : 0f;
            col = add(col, LXColor.hsb(
                    hue /*+ d * 100f*/ + fromCenter * 150f, // p.x / (10 * xRange) + p.y / (3 * yRange),
                    clamp(140 - 110.0f * abs(p.y - maxY) / yRange, 0, 100),
                    brightnessf * brightnessTerm
            ));
            colors[p.index] = col;
        }
//        System.out.printf("x (%f, %f), y (%f, %f), z (%f, %f), fromCenter (%f, %f)\n",
//                mins[0], maxs[0],
//                mins[1], maxs[1],
//                mins[2], maxs[2],
//                mins[3], maxs[3]
//        );
    }
}
