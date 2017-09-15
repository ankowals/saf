package modules.core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Macro {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public Macro(SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * functions to handle macros go here
     *
     *
     */
    private HashMap mcr(Map<String, Object> macro){

        HashMap<String,String> result = new HashMap<>();

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
                    Log.fatal("Macro type has to be provided for " + entry.getKey());
                }
                if(tMacro.containsKey("format")){
                    format = tMacro.get("format").toString();
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

            if(!availableTypes.contains(type)){
                Log.debug("Available macro types are as follows:");
                for(String item : availableTypes){
                    Log.debug(item);
                }
                Log.fatal("Wrong type defined for macro " + entry.getKey());
            }

            Set<String> availiableZones = ZoneId.getAvailableZoneIds();
            if(!availiableZones.contains(sZoneId)) {
                Log.debug("Available macro zoneIds are as follows:");
                for (String s : availiableZones) {
                    Log.debug(s);
                }
                Log.fatal("Wrong zoneId defined for macro " + entry.getKey());
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

            if(type.equals("date")) {
                calculatedValue = prefix + macroTime.format(formatter) + suffix;
            }else{
                calculatedValue = prefix + epoch + suffix;
            }

            //store calculated macro
            result.put(entry.getKey(),calculatedValue);
            Log.debug("Macro " + entry.getKey() + " is " + calculatedValue);
        }

        return result;
    }

    public void eval(String input) {
        HashMap<String, Object> mapToEval = ctx.obj.get(input,HashMap.class);
        if ( mapToEval != null ) {
            HashMap<String, Object> macros = ctx.obj.get("Macro", HashMap.class);

            HashMap<String, String> macrosAfterEvaluation = mcr(macros);

            //evaluate macros
            for (HashMap.Entry<String, Object> entryToEval : mapToEval.entrySet()) {
                if (entryToEval.getValue().getClass().getName().contains("String")) {
                    for (HashMap.Entry<String, String> macroEntry : macrosAfterEvaluation.entrySet()) {
                        if (entryToEval.getValue().equals("mcr." + macroEntry.getKey())) {
                            mapToEval.put(entryToEval.getKey(), macroEntry.getValue());
                        }
                    }
                }
            }
            ctx.obj.put(input, HashMap.class, mapToEval);
       }
    }

}
