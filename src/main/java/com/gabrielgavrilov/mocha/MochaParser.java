package com.gabrielgavrilov.mocha;

import java.util.HashMap;

public class MochaParser {

    private HashMap<String, String> parserData = new HashMap<>();
    private String format;
    private String parsedFormat;

    /**
     * Constructor for the MochaParser class. Used to parse template routes into a HashMap.
     *
     * @param template Template text.
     * @param text Text to be parsed.
     */
    public MochaParser(String template, String text) {
        try {
            String[] templateSplit = template.split("/");
            String[] textSplit = text.split("/");

            for (int i = 0; i < templateSplit.length; i++) {
                if (templateSplit[i].contains("{") && templateSplit[i].contains("}")) {
                    String formatVariable = templateSplit[i].substring(1, templateSplit[i].length() - 1);
                    parserData.put(formatVariable, textSplit[i]);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {}
    }

    /**
     * Checks to see if the current template has been parsed or not.
     * (If the current format is not parsed, then it's not parsable).
     *
     * @return Boolean
     */
    public boolean isParsable()
    {
        if(parserData.isEmpty())
            return false;
        return true;
    }

    /**
     * Since MochaParser parses the text in the constructor, this function
     * just returns the private parserData HashMap.
     *
     * @return HashMap<String, String>
     */
    public HashMap<String, String> parse()
    {
        return this.parserData;
    }
}
