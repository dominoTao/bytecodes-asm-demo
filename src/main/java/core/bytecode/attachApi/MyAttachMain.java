package core.bytecode.attachApi;


import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class MyAttachMain {
    public static void main(String[] args) throws IOException, AttachNotSupportedException {
        VirtualMachine vm = VirtualMachine.attach(args[0]);
        try {
            vm.loadAgent("D:\\agent.jar");
        } catch (AgentLoadException e) {
            e.printStackTrace();
        } catch (AgentInitializationException e) {
            e.printStackTrace();
        } finally {
            vm.detach();
        }
    }
}
