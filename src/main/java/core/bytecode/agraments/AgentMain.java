package core.bytecode.agraments;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {
    /**
     * 通过premain方法将MyClassFileTransformer 注入到Instrumentation中
     * @param agentArgs
     * @param instrumentation
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) throws ClassNotFoundException, UnmodifiableClassException {
        instrumentation.addTransformer(new MyClassFileTransformer(), true);
    }


}
