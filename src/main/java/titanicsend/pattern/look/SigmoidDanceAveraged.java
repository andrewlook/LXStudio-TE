package titanicsend.pattern.look;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import titanicsend.pattern.TEPerformancePattern;
import titanicsend.pattern.yoffa.effect.NativeShaderPatternEffect;
import titanicsend.pattern.yoffa.framework.PatternTarget;
import titanicsend.pattern.yoffa.framework.TEShaderView;
import titanicsend.pattern.yoffa.shader_engine.NativeShader;

@LXCategory("Look Shader Patterns")
public class SigmoidDanceAveraged extends TEPerformancePattern {
    NativeShaderPatternEffect effect;
    NativeShader shader;

    public SigmoidDanceAveraged(LX lx) {
        super(lx, TEShaderView.DOUBLE_LARGE);

//        controls.setRange(TEControlTag.SPEED, 0.6, -1, 1);
//        controls.setRange(TEControlTag.WOW1, 0, 0, 2.6);
//        controls.setRange(TEControlTag.QUANTITY, 0.2, 0.075, 0.3);
//        controls.setValue(TEControlTag.SPIN,0.125);

        // register common controls with LX
        addCommonControls();

        effect = new NativeShaderPatternEffect("sigmoid_dance.fs",
                new PatternTarget(this));
    }

    private float minLow = Float.POSITIVE_INFINITY;
    private float maxLow = Float.NEGATIVE_INFINITY;
    private float minHigh = Float.POSITIVE_INFINITY;
    private float maxHigh = Float.NEGATIVE_INFINITY;


    @Override
    public void runTEAudioPattern(double deltaMs) {
        int fullNBands = eq.getNumBands();
        int halfNBands = fullNBands / 2;

        // TODO: EMA?
        float avgLow = eq.getAveragef(0, halfNBands);

        float avgHigh = eq.getAveragef(halfNBands, fullNBands - halfNBands);

        // TODO: keep max/min on a moving window?
        if (avgLow < minLow) {
            minLow = avgLow;
        }
        if (avgLow > maxLow) {
            maxLow = avgLow;
        }
        if (avgHigh < minHigh) {
            minHigh = avgHigh;
        }
        if (avgHigh > maxHigh) {
            maxHigh = avgHigh;
        }

        System.out.printf("fullNBands = %s, halfNBands = %s\n", fullNBands, halfNBands);
        System.out.printf("avgLow = %s, avgHigh = %s\n", avgLow, avgHigh);

        float normLow = avgLow / (maxLow - minLow);
        float normHigh = avgHigh / (maxHigh - minHigh);
        System.out.printf("normLow = %s, normHigh = %s\n", normLow, normHigh);

        float scaledLow = normLow * 2 - 1;
        float scaledHigh = normHigh * 2 - 1;
        System.out.printf("scaledLow = %s, scaledHigh = %s\n", scaledLow, scaledHigh);

//        shader.setUniform("iWow1", avgLow);
//        shader.setUniform("iWow2", avgHigh);
//        shader.setUniform("iWow1", normLow);
//        shader.setUniform("iWow2", normHigh);
        shader.setUniform("iWow1", scaledLow);
        shader.setUniform("iWow2", scaledHigh);

        // run the shader
        effect.run(deltaMs);
    }

    @Override
    // THIS IS REQUIRED if you're not using ConstructedPattern!
    // Initialize the NativeShaderPatternEffect and retrieve the native shader object
    // from it when the pattern becomes active
    public void onActive() {
        super.onActive();
        effect.onActive();
        shader = effect.getNativeShader();
    }

}
