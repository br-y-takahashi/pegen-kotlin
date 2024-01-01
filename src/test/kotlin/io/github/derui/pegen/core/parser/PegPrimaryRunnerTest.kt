package io.github.derui.pegen.core.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.derui.pegen.core.lang.PegClassPrimary
import io.github.derui.pegen.core.lang.PegDefinition
import io.github.derui.pegen.core.lang.PegDotPrimary
import io.github.derui.pegen.core.lang.PegExpression
import io.github.derui.pegen.core.lang.PegGroupPrimary
import io.github.derui.pegen.core.lang.PegIdentifierPrimary
import io.github.derui.pegen.core.lang.PegLiteralPrimary
import io.github.derui.pegen.core.lang.PegNakedPrefix
import io.github.derui.pegen.core.lang.PegNakedSuffix
import io.github.derui.pegen.core.lang.PegSequence
import io.github.derui.pegen.core.support.get
import io.github.derui.pegen.core.support.getOrNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class PegPrimaryRunnerTest {
    private enum class TagType

    @Nested
    inner class Dot {
        @Test
        fun `parse dot primary`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")

            // Act
            val actual = PegPrimaryRunner.run(PegDotPrimary(UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.rawOf("t", ParserSource.newWith("est")))
        }

        @Test
        fun `fail if empty`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("")

            // Act
            val actual = PegPrimaryRunner.run(PegDotPrimary(UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.getOrNull()).isNull()
        }
    }

    @Nested
    inner class Literal {
        @Test
        fun `parse literal primary`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")

            // Act
            val actual = PegPrimaryRunner.run(PegLiteralPrimary("te", UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.rawOf("te", ParserSource.newWith("st")))
        }

        @Test
        fun `empty literal is always valid`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")

            // Act
            val actual = PegPrimaryRunner.run(PegLiteralPrimary("", UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.rawOf("", ParserSource.newWith("test")))
        }

        @Test
        fun `fail if literal is not match`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("fail")

            // Act
            val actual = PegPrimaryRunner.run(PegLiteralPrimary("te", UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.getOrNull()).isNull()
        }
    }

    @Nested
    inner class CharacterClass {
        @Test
        fun `parse class primary`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")

            // Act
            val actual = PegPrimaryRunner.run(PegClassPrimary(setOf('t', 'e'), UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.rawOf("t", ParserSource.newWith("est")))
        }

        @Test
        fun `fail if character class is not match`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("fail")
            val source = ParserSource.newWith("fail")

            // Act
            val actual = PegPrimaryRunner.run(PegClassPrimary(setOf('a'), UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.getOrNull()).isNull()
        }
    }

    @Nested
    inner class Group {
        @Test
        fun `parse group primary`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")
            val suffix = PegNakedSuffix<Unit, TagType>(PegDotPrimary(UUID.randomUUID()), UUID.randomUUID())
            val prefix = PegNakedPrefix(suffix, UUID.randomUUID())
            val seq = PegSequence(listOf(prefix), UUID.randomUUID())
            val expr = PegExpression(listOf(seq), UUID.randomUUID())

            // Act
            val actual = PegPrimaryRunner.run(PegGroupPrimary(expr, UUID.randomUUID()), source, context)

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.rawOf("t", ParserSource.newWith("est")))
        }
    }

    @Nested
    inner class Identifier {
        @Test
        fun `parse definition`() {
            // Arrange
            val context = ParserContext.new<Unit, TagType>("test")
            val source = ParserSource.newWith("test")
            val suffix = PegNakedSuffix<Unit, TagType>(PegDotPrimary(UUID.randomUUID()), UUID.randomUUID())
            val prefix = PegNakedPrefix(suffix, UUID.randomUUID())
            val seq = PegSequence(listOf(prefix), UUID.randomUUID())
            val expr = PegExpression(listOf(seq), UUID.randomUUID())

            // Act
            val actual =
                PegPrimaryRunner.run(
                    PegIdentifierPrimary(PegDefinition(UUID.randomUUID(), expr, {}), UUID.randomUUID()),
                    source,
                    context,
                )

            // Assert
            assertThat(actual.get()).isEqualTo(ParsingResult.constructedAs(Unit, ParserSource.newWith("est")))
        }
    }
}
