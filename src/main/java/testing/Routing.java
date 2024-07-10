package testing;

import com.gabrielgavrilov.mocha.MochaRequest;
import com.gabrielgavrilov.mocha.MochaResponse;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class Routing extends HashMap<String, BiConsumer<MochaRequest, MochaResponse>>
{

}
