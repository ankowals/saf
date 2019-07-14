package libs.libCore.modules;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.CaptureGroupTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import java.util.Locale;

import static java.util.Locale.ENGLISH;

@SuppressWarnings("unchecked")
public class StepParameterTypeTransformer implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineParameterType(new ParameterType<>(
                "testdata",  //this name can be used in the step method
                "(.*)", //regexp to match to a comma separated integer list which can contain negative numbers and whitespaces as well
                Object.class,  //the expected parameter type
                new CaptureGroupTransformer(){
                    @Override
                    public Object transform(String[] args) {
                        StepCore stepCore = new StepCore();
                        return stepCore.checkIfInputIsVariable(args[0]);
                    }
                })
        );
    }


    /*

    //anonymous parameters does not work yet with IDE plugin

    @Override
    public void configureTypeRegistry(TypeRegistry registry) {

        ParamTransformer paramTransformer = new ParamTransformer();
        registry.setDefaultParameterTransformer(paramTransformer);
    }

    @Override
    public Locale locale() {
        return Locale.ENGLISH;
    }

    private static final class ParamTransformer implements ParameterByTypeTransformer {

        @Override
        public Object transform(String s, Type type) {
            StepCore stepCore = new StepCore();
            return stepCore.checkIfInputIsVariable(s);
        }
    }
    */

}