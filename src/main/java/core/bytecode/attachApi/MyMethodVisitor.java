package core.bytecode.attachApi;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class MyMethodVisitor extends AdviceAdapter {
    private MethodVisitor mv;
    protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
        this.mv = methodVisitor;
    }

    @Override
    protected void onMethodEnter() {
//        super.onMethodEnter();
        mv.visitIntInsn(BIPUSH, 50);
        mv.visitInsn(IRETURN);
    }
}
