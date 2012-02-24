package komu.blunt.stdlib

import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorNames

object BasicValues {

    val FALSE = TypeConstructorValue(0, ConstructorNames.FALSE.sure())
    val TRUE  = TypeConstructorValue(1, ConstructorNames.TRUE.sure())
    val LT    = TypeConstructorValue(0, "LT")
    val EQ    = TypeConstructorValue(1, "EQ")
    val GT    = TypeConstructorValue(2, "GT")
    val UNIT  = TypeConstructorValue(0, ConstructorNames.UNIT.sure())



    /*

    public static Object convertToJava(TypeConstructorValue value) {
        switch (value.name) {
            case "True":    return true;
            case "False":   return false;
            default:        return value;
        }
    }
    */
}
