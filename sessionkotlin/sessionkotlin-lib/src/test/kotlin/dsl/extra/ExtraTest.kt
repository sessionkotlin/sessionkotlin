package dsl.extra

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import com.github.d_costa.sessionkotlin.dsl.exception.RoleNotEnabledException
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtraTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
    }

    @Test
    fun `liveliness roleprog 1`() {
        // scribble-java bad.liveness.roleprog.Test01
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        val t = mu()
                        send<Unit>(a, b)
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `liveliness roleprog 3a`() {
        // scribble-java bad.liveness.roleprog.Test03a
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        val t = mu()
                        choice(a) {
                            branch {
                                send<Unit>(a, b)
                                goto(t)
                            }
                        }
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `safety waitfor 3party 1`() {
        // scribble-java bad.safety.waitfor.threeparty.Test01
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `safety waitfor 3party 2`() {
        // scribble-java bad.safety.waitfor.threeparty.Test02
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        choice(a) {
                            branch {
                                send<Unit>(a, c)
                                goto(t)
                            }
                            branch {
                                send<Unit>(a, c)
                            }
                        }
                    }
                    branch {
                        send<Unit>(a, c)
                        send<Unit>(a, b)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent choice subject 4b`() {
        // scribble-java bad.syntax.consistentchoicesubj.Test04b
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        goto(t)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent choice subject 5a`() {
        // scribble-java bad.syntax.consistentchoicesubj.Test05a
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent choice subject 5b`() {
        // scribble-java bad.syntax.consistentchoicesubj.Test05b
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                        goto(t)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent choice subject 6a`() {
        // scribble-java bad.syntax.consistentchoicesubj.Test06a
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                    }
                    branch {
                        send<Unit>(a, c)
                        send<Unit>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent choice subject 7b`() {
        // scribble-java bad.syntax.consistentchoicesubj.Test07c
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(c, a)
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        send<Unit>(c, a)
                        goto(t)
                    }
                }
            }
        }
    }

    @Test
    fun `test enabled by`() {
        // scribble-java good.efsm.gchoice.Test11
        val g = globalProtocolInternal {
            choice(a) {
                branch {
                    send<Unit>(a, b)

                    choice(b) {
                        branch {
                            send<Unit>(b, c)
                        }
                    }
                }
            }
        }
        val lC = LocalTypeReceive(b, UnitClass, LocalTypeEnd)
        assertEquals(lC, g.project(c))
    }

    @Test
    fun `collapsable recs`() {
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = mu()
            send<Unit>(a, b)

            choice(a) {
                branch {
                    goto(t)
                }
                branch {
                    goto(t)
                }
            }
        }
        val lB = LocalTypeRecursionDefinition(
            t,
            LocalTypeReceive(
                a, UnitClass,
                LocalTypeRecursion(t)
            )
        )
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `unguarded recursion`() {
        // scribble-java good.efsm.gcontinue.choiceunguarded.Test01c
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = mu()
            send<Unit>(a, b)

            choice(a) {
                branch {
                    goto(t)
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            t,
            LocalTypeSend(
                b, UnitClass,
                LocalTypeInternalChoice(
                    listOf(LocalTypeRecursion(t))
                )
            )
        )
        val lB = LocalTypeRecursionDefinition(
            t,
            LocalTypeReceive(a, UnitClass, LocalTypeRecursion(t))
        )
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `infinite protocol`() {
        val g = globalProtocolInternal {
            val t = mu()
            send<Unit>(a, b)
            goto(t)
        }
    }
}
