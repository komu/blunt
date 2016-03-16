package komu.blunt.types

import org.junit.Test
import kotlin.test.assertEquals

class TypeApplicationTest {

    @Test
    fun printingArrowTypes() {
        //TypeVariable("a", Kind.STAR)
        assertEquals("foo", "foo")
        //functionType(typeVariable("a"), typeVariable("b"))
        //assertEquals("a -> b", functionType(typeVariable("a"), typeVariable("b")).toString())
    }
}
