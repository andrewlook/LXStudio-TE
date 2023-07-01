package titanicsend.pattern.look;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import titanicsend.pattern.TEPerformancePattern;
import titanicsend.pattern.yoffa.framework.TEShaderView;

import static com.jogamp.opengl.math.FloatUtil.sqrt;
import static heronarts.lx.color.LXColor.add;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static titanicsend.util.TEMath.clamp;

// TODO(look): more appropriate category
@LXCategory("Native Shaders Panels")
public class PlaneSingle extends TEPerformancePattern {

    public final SinLFO aLFO;
    public final SinLFO bLFO;
    public final SinLFO cLFO;
    public final SinLFO dLFO;

    public final float minX;
    public final float maxX;
    public final float minY;
    public final float maxY;
    public final float minZ;
    public final float maxZ;

    public final float xRange;
    public final float yRange;
    public final float zRange;


    final CompoundParameter a = new CompoundParameter("a", 0.3, -1.0, 1.0);
    final CompoundParameter b = new CompoundParameter("b", 0.2, -1.0, 1.0);
    final CompoundParameter c = new CompoundParameter("c", 0.5, -1.0, 1.0);
    final CompoundParameter d = new CompoundParameter("d", 0.5, -1.0, 1.0);

    final CompoundParameter thickness = new CompoundParameter("thickness", 0.2);

    public PlaneSingle(LX lx) {
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

        aLFO = new SinLFO(minX, maxX, 5000);
        bLFO = new SinLFO(minY, maxY, 6000);
        cLFO = new SinLFO(minZ, maxZ, 7000);
        dLFO = new SinLFO(minZ, maxZ, 7000);
        addModulator(aLFO).trigger();
        addModulator(bLFO).trigger();
        addModulator(cLFO).trigger();
        addModulator(dLFO).trigger();
    }

    protected void addParams() {
        addParameter("a", a);
        addParameter("b", b);
        addParameter("c", c);
        addParameter("d", d);
        addParameter("thickness", thickness);
    }

    public void runTEAudioPattern(double deltaMs) {
        clearPixels();  // Sets all pixels to transparent for starters

        float a = this.a.getValuef();
        float b = this.b.getValuef();
        float c = this.c.getValuef();
        float d = this.d.getValuef();
        float denom = sqrt(a*a + b*b + c*c);

        float hue = LXColor.h(LXColor.BLUE);
        float thicknessf = thickness.getValuef();

        for (LXPoint p : model.points) {

            float xNorm = p.x / xRange;
            float yNorm = p.y / yRange;
            float zNorm = p.z / zRange;

            float distToPlane = abs(a * xNorm + b * yNorm + c * zNorm + d) / denom;


//            System.out.printf("Math.abs(p.y - maxY) / yRange = %f\n", Math.abs(p.y - maxY) / yRange);
//            System.out.printf("Math.abs(p.x - maxX) / xRange = %f\n", Math.abs(p.x - maxX) / xRange);
//            System.out.printf("Math.abs(p.z - maxZ) / zRange = %f\n", Math.abs(p.z - maxZ) / zRange);
//                System.out.printf("xlv=%f, xwv=%f, p.x=%f, xv=%f, Math.abs(p.x-xv)=%f\n", xlv, xwv, p.x, xv, Math.abs(p.x-xv) / maxX);
//                System.out.printf("xlv - xwv * Math.abs(p.x - xv) = %f, max(0, xlv - xwv * Math.abs(p.x - xv)) = %f\n\n", xlv - xwv * Math.abs(p.x - xv) / maxX, max(0, xlv - xwv * Math.abs(p.x - xv) / maxX));
            int col = 0;
            col = add(col, LXColor.hsb(
                    hue + distToPlane * 100f, // p.x / (10 * xRange) + p.y / (3 * yRange),
                    clamp(140 - 110.0f * abs(p.y - maxY) / yRange, 0, 100),
                    distToPlane < thicknessf ? 100f : 0f
                    //max(0, (distToPlane - thicknessf) * 100f)
            ));
            colors[p.index] = col;
        }
    }
}
