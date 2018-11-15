package libs.libCore.modules;

import org.apache.commons.lang.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

@SuppressWarnings("unchecked")
public class Macro {

    private Context scenarioCtx;

    public Macro() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
    }


    /**
     * calculates macros
     *
     * helper function used to calculate macro values based on data defined in the Macro
     * configuration storage
     *
     * It is used internally by eval method.
     *
     * @param macro Map, storage that contains macro definitions
     */
    private HashMap mcr(Map<String, Object> macro){

        HashMap <String, String> result = scenarioCtx.get("CalculatedMacros", HashMap.class);
        if ( result != null ) {
            return result;
        }

        result = new HashMap<>();

        // Get the current date and time
        LocalDateTime currentTime = LocalDateTime.now();

        //for each macro calculate new value and store it in a result map as string
        for (HashMap.Entry<String, Object> entry : macro.entrySet())
        {
            //Log.debug("Found macro " + entry.getKey());

            //set default values for particular macro
            String calculatedValue = null;
            String type = null;
            String format = "MM/dd/yyyy HH:mm:ss S";
            Integer length = 10;
            Boolean useNumbers = true;
            Boolean useLetters = true;
            String suffix = "";
            String prefix = "";
            String sZoneId = ZoneId.systemDefault().getId();
            Integer addDays = 0;
            Integer addWeeks = 0;
            Integer addMonths = 0;
            Integer addYears = 0;
            Integer addHours = 0;
            Integer addMinutes = 0;
            Integer addSeconds = 0;
            Integer addNanos = 0;

            //read user defined values for particular macro
            if (entry.getValue().getClass().getName().contains("HashMap")) {
                HashMap<String, Object> tMacro = (HashMap<String, Object>) entry.getValue();
                if(tMacro.containsKey("type")){
                    type = tMacro.get("type").toString();
                }else{
                    Log.error("Macro type has to be provided for " + entry.getKey());
                }
                if(tMacro.containsKey("format")){
                    format = tMacro.get("format").toString();
                }
                if(tMacro.containsKey("length")){
                    length = Integer.parseInt(tMacro.get("length").toString());
                }
                if ( length <= 0 ){
                    Log.error("Length value shall be above 0!");
                }
                if(tMacro.containsKey("useNumbers")){
                    useNumbers = Boolean.parseBoolean(tMacro.get("useNumbers").toString());
                }
                if(tMacro.containsKey("useLetters")){
                    useLetters = Boolean.parseBoolean(tMacro.get("useLetters").toString());
                }
                if(tMacro.containsKey("suffix")){
                    suffix = tMacro.get("suffix").toString();
                }
                if(tMacro.containsKey("prefix")){
                    prefix = tMacro.get("prefix").toString();
                }
                if(tMacro.containsKey("zoneId")){
                    sZoneId = tMacro.get("zoneId").toString();
                }
                if(tMacro.containsKey("addYears")){
                    addYears = Integer.parseInt(tMacro.get("addYears").toString());
                }
                if(tMacro.containsKey("addMonths")){
                    addMonths = Integer.parseInt(tMacro.get("addMonths").toString());
                }
                if(tMacro.containsKey("addWeeks")){
                    addWeeks = Integer.parseInt(tMacro.get("addWeeks").toString());
                }
                if(tMacro.containsKey("addDays")){
                    addDays = Integer.parseInt(tMacro.get("addDays").toString());
                }
                if(tMacro.containsKey("addHours")){
                    addHours = Integer.parseInt(tMacro.get("addHours").toString());
                }
                if(tMacro.containsKey("addMinutes")){
                    addMinutes = Integer.parseInt(tMacro.get("addMinutes").toString());
                }
                if(tMacro.containsKey("addSeconds")){
                    addSeconds = Integer.parseInt(tMacro.get("addSeconds").toString());
                }
                if(tMacro.containsKey("addNanos")){
                    addNanos = Integer.parseInt(tMacro.get("addNanos").toString());
                }
            }

            //check if values are correct
            ArrayList<String> availableTypes = new ArrayList<>();
            availableTypes.add("date");
            availableTypes.add("timestamp");
            availableTypes.add("random");
            availableTypes.add("startOfWeek");
            availableTypes.add("endOfWeek");
            availableTypes.add("startOfMonth");
            availableTypes.add("endOfMonth");

            if(!availableTypes.contains(type)){
                Log.warn("Available macro types are as follows:");
                for(String item : availableTypes){
                    Log.debug(item);
                }
                Log.error("Wrong type defined for macro " + entry.getKey());
            }

            Set<String> availableZones = ZoneId.getAvailableZoneIds();
            if(!availableZones.contains(sZoneId)) {
                Log.warn("Available macro zoneIds are as follows:");
                for (String s : availableZones) {
                    Log.info(s);
                }
                Log.error("Wrong zoneId defined for macro " + entry.getKey());
            }

            //calculate new macro value
            ZoneId zoneId = ZoneId.of(sZoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            ZonedDateTime macroTime = currentTime.atZone(zoneId).plusYears(addYears)
                    .plusMonths(addMonths)
                    .plusWeeks(addWeeks)
                    .plusDays(addDays)
                    .plusHours(addHours)
                    .plusMinutes(addMinutes)
                    .plusSeconds(addSeconds)
                    .plusNanos(addNanos);

            long epoch = macroTime.toEpochSecond();

            if(type.equals("startOfWeek")){
                ZonedDateTime firstOfWeek = macroTime.with(ChronoField.DAY_OF_WEEK, 1); //ISO8601, Monday is first day of week
                calculatedValue = prefix + firstOfWeek.format(formatter) + suffix;
            }
            if(type.equals("endOfWeek")){
                ZonedDateTime endOfWeek = macroTime.with(ChronoField.DAY_OF_WEEK, 7); //ISO8601, Sunday is last day of week
                calculatedValue = prefix + endOfWeek.format(formatter) + suffix;
            }
            if(type.equals("startOfMonth")){
                ZonedDateTime firstOfMonth = macroTime.with(ChronoField.DAY_OF_MONTH, 1);
                calculatedValue = prefix + firstOfMonth.format(formatter) + suffix;
            }
            if(type.equals("endOfMonth")){
                Integer lengthOfMonth = macroTime.toLocalDate().lengthOfMonth();
                ZonedDateTime endOfMonth = macroTime.with(ChronoField.DAY_OF_MONTH, lengthOfMonth);
                calculatedValue = prefix + endOfMonth.format(formatter) + suffix;
            }
            if(type.equals("date")) {
                calculatedValue = prefix + macroTime.format(formatter) + suffix;
            }
            if(type.equals("timestamp")){
                calculatedValue = prefix + epoch + suffix;
            }
            if(type.equals("random")){
                calculatedValue = prefix + RandomStringUtils.random(length, useLetters, useNumbers) + suffix;
            }

            //store calculated macro
            result.put(entry.getKey(),calculatedValue);
            Log.debug("Macro " + entry.getKey() + " is " + calculatedValue);
        }

        scenarioCtx.put("CalculatedMacros", HashMap.class, result);

        return result;
    }


    /**
     * evaluates macros
     * This method looks for a variable that starts with "mcr." in the configuration Storage entities
     * and tries to substitute such variables with calculated macro values.
     *
     * @param input String, name of the storage where macro variables subsitution shall be done
     */
    public void eval(String input) {
        HashMap<String, Object> mapToEval = scenarioCtx.get(input,HashMap.class);
        if ( mapToEval != null ) {
            HashMap<String, Object> macros = scenarioCtx.get("Macro", HashMap.class);
            HashMap<String, String> macrosAfterEvaluation = mcr(macros);

            //evaluate macros
            handleEvaluation(mapToEval, macrosAfterEvaluation);
            scenarioCtx.put(input, HashMap.class, mapToEval);
       }
    }


    /**
     * helper method to handle nested macros in maps and lists.
     * Recursion is used to check every entity of the input map.
     * Nested lists are not supported
     *
     * @param map HashMap, input to be checked for macro presence
     * @param macrosAfterEvaluation HashMap, contains macros name and calculated value mapping
     */
    private void handleEvaluation (HashMap<String, Object> map, HashMap<String, String> macrosAfterEvaluation) {
        for (HashMap.Entry<String, Object> entry : map.entrySet()){

            if ( entry.getValue() instanceof HashMap ) {
                handleEvaluation((HashMap) entry.getValue(), macrosAfterEvaluation);
            } else if ( entry.getValue() instanceof ArrayList ) {
                for (int i=0; i < ((ArrayList) entry.getValue()).size(); i++){
                    if ( ((ArrayList) entry.getValue()).get(i).getClass().getName().contains("String")) {
                        for (HashMap.Entry<String, String> macroEntry : macrosAfterEvaluation.entrySet()) {
                            if (((ArrayList) entry.getValue()).get(i).equals("mcr." + macroEntry.getKey())) {
                                ((ArrayList) entry.getValue()).set(i,macroEntry.getValue());
                            }
                        }
                    } else if ( ((ArrayList) entry.getValue()).get(i) instanceof HashMap) {
                        handleEvaluation((HashMap<String, Object>) ((ArrayList) entry.getValue()).get(i), macrosAfterEvaluation);
                    }
                }
            } else {
                if (entry.getValue().getClass().getName().contains("String")) {
                    for (HashMap.Entry<String, String> macroEntry : macrosAfterEvaluation.entrySet()) {
                        if (entry.getValue().equals("mcr." + macroEntry.getKey())) {
                            map.put(entry.getKey(), macroEntry.getValue());
                        }
                    }
                }
            }
        }
    }


}