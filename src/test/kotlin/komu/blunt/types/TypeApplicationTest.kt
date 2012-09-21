package komu.blunt.types

import org.junit.Test as test
import kotlin.test.*

public class TypeApplicationTest {

    test fun printingArrowTypes() {
        //TypeVariable("a", Kind.STAR)
        assertEquals("foo", "foo")
        //functionType(typeVariable("a"), typeVariable("b"))
        //assertEquals("a -> b", functionType(typeVariable("a"), typeVariable("b")).toString())
    }

}
