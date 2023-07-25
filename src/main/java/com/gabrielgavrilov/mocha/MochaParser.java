package com.gabrielgavrilov.mocha;

import java.util.HashMap;

public class MochaParser {

    private HashMap<String, String> parserData = new HashMap<>();
    private String format;
    private String parsedFormat;

    public MochaParser(String format, String text) {
        try {
            String[] formatSplit = format.split("/");
            String[] textSplit = text.split("/");

            for (int i = 0; i < formatSplit.length; i++) {
                if (formatSplit[i].contains("{") && formatSplit[i].contains("}")) {
                    String formatVariable = formatSplit[i].substring(1, formatSplit[i].length() - 1);
                    parserData.put(formatVariable, textSplit[i]);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {}

    }

    public boolean isParsable()
    {
        if(parserData.isEmpty())
            return false;
        return true;
    }

    public HashMap<String, String> parse()
    {
        return this.parserData;
    }

}
