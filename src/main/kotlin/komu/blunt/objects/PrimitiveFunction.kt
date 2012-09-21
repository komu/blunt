package komu.blunt.objects

class PrimitiveFunction(val func: (Any?) -> Any?) : PrimitiveProcedure {
    override fun apply(arg: Any?): Any? = func(arg)
//
//private final Method method;
//private final String name;
//private final boolean isStatic;
//
//public PrimitiveFunction(String name, Method method, boolean isStatic) {
//this.name = checkNotNull(name);
//this.method = checkNotNull(method);
//this.isStatic = isStatic;
//}
//
//@Override
//public Object apply(Object arg) {
//Object[] args = convertAllToJava(extract(arg));
//try {
//if (isStatic) {
//return convertResult(method.invoke(null, args));
//} else {
//return convertResult(method.invoke(args[0], Arrays.copyOfRange(args, 1, args.length)));
//}
//
//} catch (IllegalAccessException e) {
//throw new RuntimeException(e);
//} catch (InvocationTargetException e) {
//throw propagate(e.getTargetException());
//}
//}
//
//private static Object[] convertAllToJava(Object[] values) {
//Object[] result = new Object[values.length];
//
//for (int i = 0; i < values.length; i++)
//result[i] = convertToJava(values[i]);
//
//return result;
//}
//
//private static Object convertToJava(Object value) {
//if (value instanceof TypeConstructorValue) {
//return BasicValues.convertToJava((TypeConstructorValue) value);
//} else {
//return value;
//}
//}
//
//private static Object convertResult(Object result) {
//if (result instanceof Boolean) {
//return BasicValues.booleanToConstructor((Boolean) result);
//} else {
//return result;
//}
//}
//
//private Object[] extract(Object arg) {
//if (arg instanceof TypeConstructorValue) {
//TypeConstructorValue ctor = (TypeConstructorValue) arg;
//if (ctor.name.equals(ConstructorNames.UNIT))
//return new Object[0];
//else if (ctor.isTuple())
//return ctor.items;
//}
//return new Object[] { arg };
//}
//
//@Override
//public String toString() {
//return "<#primitive procedure " + name + ">";
//}

}

