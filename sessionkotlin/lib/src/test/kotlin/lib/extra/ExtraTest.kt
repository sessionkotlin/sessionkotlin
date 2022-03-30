package lib.extra

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtraTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `liveliness roleprog 1`() {
        // scribble-java bad.liveness.roleprog.Test01
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                choice(a) {
                    case("1") {
                        val t = miu("X")
                        send<Unit>(a, b)
                        goto(t)
                    }
                    case("2") {
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
            globalProtocol {
                choice(a) {
                    case("1") {
                        val t = miu("X")
                        choice(a) {
                            case("1.1") {
                                send<Unit>(a, b)
                                goto(t)
                            }
                        }
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        goto(t)
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        choice(a) {
                            case("1.1") {
                                send<Unit>(a, c)
                                goto(t)
                            }
                            case("1.2") {
                                send<Unit>(a, c)
                            }
                        }
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        goto(t)
                    }
                    case("2") {
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
            globalProtocol {
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                        goto(t)
                    }
                    case("2") {
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
            globalProtocol {
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        send<Unit>(b, c)
                        send<Unit>(c, b)
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                        send<Unit>(c, a)
                        goto(t)
                    }
                    case("2") {
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
        val g = globalProtocol {
            choice(a) {
                case("1") {
                    send<Unit>(a, b)

                    choice(b) {
                        case("1.1") {
                            send<Unit>(b, c)
                        }
                    }
                }
            }
        }
        val lC = LocalTypeExternalChoice(
            b,
            mapOf("1" to LocalTypeExternalChoice(
                b,
                mapOf("1.1" to LocalTypeReceive(b, Unit::class.java, LocalTypeEnd))
            ))
        )
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `collapsable recs`() {
        lateinit var t: RecursionTag
        val g = globalProtocol {
            t = miu("X")
            send<Unit>(a, b)

            choice(a) {
                case("1") {
                    goto(t)
                }
                case("2") {
                    goto(t)
                }
            }
        }
        val lB = LocalTypeRecursionDefinition(t,
            LocalTypeReceive(a, Unit::class.java,
                LocalTypeRecursion(t)
            )
        )
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `unguarded recursion`() {
        // scribble-java good.efsm.gcontinue.choiceunguarded.Test01c
        globalProtocol {
            val t = miu("X")
            send<Unit>(a, b)

            choice(a) {
                case("1") {
                    goto(t)
                }
            }
        }
    }
}