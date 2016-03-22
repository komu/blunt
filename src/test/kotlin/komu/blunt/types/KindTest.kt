package komu.blunt.types

import komu.blunt.types.Kind.Arrow
import komu.blunt.types.Kind.Star
import org.junit.Test
import kotlin.test.assertEquals

class KindTest {

    @Test
    fun stringRepresentationOfKinds() {
        assertEquals("*", Star.toString());
        assertEquals("* -> *", Arrow(Star, Star).toString());
        assertEquals("* -> * -> *", Arrow(Star, Arrow(Star, Star)).toString());
        assertEquals("* -> * -> * -> *", Arrow(Star, Arrow(Star, Arrow(Star, Star))).toString());
        assertEquals("(* -> *) -> *", Arrow(Arrow(Star, Star), Star).toString());
    }
}
