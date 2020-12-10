package core.bytecode.attachApi;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {
    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        System.out.println("agentmain called");
        instrumentation.addTransformer(new MyClassFileTransformer(), true);
        Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
        for (int i = 0; i < allLoadedClasses.length; i++) {
            if ("MyTestMain".equals(allLoadedClasses[i].getName())) {
                System.out.println("Reloading: "+allLoadedClasses[i].getName());
                instrumentation.retransformClasses(allLoadedClasses[i]);
                break;
            }
        }
    }
}
