package titanicsend.pattern.look;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import titanicsend.model.TEEdgeModel;
import titanicsend.pattern.TEPattern;

import java.util.ArrayList;
import java.util.List;

@LXCategory("Look Java Patterns")
public class EdgeOrientation extends TEPattern {
    public final BooleanParameter flatMode =
            new BooleanParameter("flat", true)
                    .setDescription("Enable flat Edges");
    public final BooleanParameter leftwardMode =
            new BooleanParameter("leftward", true)
                    .setDescription("Enable leftward Edges");
    public final BooleanParameter rightwardMode =
            new BooleanParameter("rightward", true)
                    .setDescription("Enable rightward Edges");

    // Collection of edges that should be on based on parameters
    List<TEEdgeModel> flatEdges = new ArrayList<>();
    List<TEEdgeModel> leftwardEdges = new ArrayList<>();
    List<TEEdgeModel> rightwardEdges = new ArrayList<>();

    public EdgeOrientation(LX lx) {
        super(lx);
        addParameter("flat", flatMode);
        addParameter("leftward", leftwardMode);
        addParameter("rightward", rightwardMode);

        selectEdges();
    }

    final float EPSILON = 500000f;

    protected void selectEdges() {
        for (TEEdgeModel edge : modelTE.edgesById.values()) {
            float v0_y = edge.v0.y;
            float v0_z = edge.v0.z;
            float v1_y = edge.v1.y;
            float v1_z = edge.v1.z;

            System.out.printf("Edge %s -- v0 (y = %f, z = %f) v1 (y = %f, z = %f)\n", edge.getId(), v0_y, v0_z, v1_y, v1_z);
            if (v0_y == v1_y) {
                System.out.printf("Edge %s -- flat\n", edge.getId());
                flatEdges.add(edge);
            } else if (Math.abs(v0_y - v1_y) < EPSILON) {
                System.out.printf("Edge %s -- flat-ISH\n -- Math.abs(v0_y - v1_y) = %f", edge.getId(), Math.abs(v0_y - v1_y));
                flatEdges.add(edge);
            } else if (v0_y > v1_y) {
                if (v0_z > v1_z) {
                    System.out.printf("Edge %s -- leftward (v0_y > v1_y) && (v0_z > v1_z)\n", edge.getId());
                    leftwardEdges.add(edge);
                } else {
                    System.out.printf("Edge %s -- rightward (v0_y > v1_y) && NOT (v0_z > v1_z)\n", edge.getId());
                    rightwardEdges.add(edge);
                }
            } else if (v1_y > v0_y) {
                if (v1_z > v0_z) {
                    System.out.printf("Edge %s -- (v1_y > v0_y) && (v1_z > v0_z)\n", edge.getId());
                    leftwardEdges.add(edge);
                } else {
                    System.out.printf("Edge %s -- (v1_y > v0_y) && NOT (v1_z > v0_z)\n", edge.getId());
                    rightwardEdges.add(edge);
                }
            }
        }
    }

    public void run(double deltaMs) {
        clearPixels();

        int color = LXColor.RED;
//        int color = colorParam.getColor();
//        if (getChannel() != null) {
//            if (getChannel().blendMode.getObject().getClass().equals(MultiplyBlend.class)) {
//                // Operate in Mask mode
//                setEdges(LXColor.BLACK);
//                color = LXColor.WHITE;
//            } else {
//                clearEdges();
//            }
//        }

        if (flatMode.getValueb()) {
            for (TEEdgeModel edge : flatEdges) {
                for (TEEdgeModel.Point point : edge.points) {
                    colors[point.index] = LXColor.RED;
                }
            }
        }

        if (leftwardMode.getValueb()) {
            for (TEEdgeModel edge : leftwardEdges) {
                for (TEEdgeModel.Point point : edge.points) {
                    colors[point.index] = LXColor.GREEN;
                }
            }
        }

        if (rightwardMode.getValueb()) {
            for (TEEdgeModel edge : rightwardEdges) {
                for (TEEdgeModel.Point point : edge.points) {
                    colors[point.index] = LXColor.BLUE;
                }
            }
        }

    }

//    @Override
//    public void onParameterChanged(LXParameter parameter) {
//        super.onParameterChanged(parameter);
//
//        if (Arrays.<LXParameter>asList(energy, fracFromZCenter, height).contains(parameter)) {
//            selectEdges();
//        }
//    }
}
