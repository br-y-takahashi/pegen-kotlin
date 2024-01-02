package io.github.derui.pegen.core

import io.github.derui.pegen.core.debug.DebuggingInfoRecorderImpl
import io.github.derui.pegen.core.debug.NullDebuggingInfoRecorder
import io.github.derui.pegen.core.dsl.PegDsl
import io.github.derui.pegen.core.dsl.support.DefaultSyntaxIdentifierGenerator
import io.github.derui.pegen.core.lang.PegExpression
import io.github.derui.pegen.core.parser.ErrorInfo
import io.github.derui.pegen.core.parser.ParserContext
import io.github.derui.pegen.core.parser.ParserSource
import io.github.derui.pegen.core.parser.ParsingResult
import io.github.derui.pegen.core.parser.PegExpressionMiniParser
import io.github.derui.pegen.core.support.Result

/**
 * Option for [Generator]
 */
class GeneratorOption internal constructor(
    val debug: Boolean,
) {
    companion object {
        val default = GeneratorOption(debug = false)

        operator fun invoke(init: Companion.(GeneratorOption) -> Unit): GeneratorOption {
            val option = default
            this.init(option)
            return option
        }

        fun GeneratorOption.enableDebug() = GeneratorOption(debug = true)
    }
}

/**
 * A parser generator with PEG DSL
 */
object Generator {
    /**
     * invoke [init] with [PegDsl] and return [PegExpression] that is generated by [init].
     */
    operator fun <V, TagType> invoke(init: PegDsl<V, TagType>.() -> PegExpression<V, TagType>): PegExpression<V, TagType> {
        val generator = DefaultSyntaxIdentifierGenerator()

        val expr = PegDsl<V, TagType>(generator).init()

        return expr
    }

    /**
     * generate parser with dsl
     */
    fun <V, TagType> generateParser(
        option: GeneratorOption = GeneratorOption.default,
        init: PegDsl<V, TagType>.() -> PegExpression<V, TagType>,
    ): Parser<V> {
        val expr = invoke(init)

        return GeneratedParser(expr, option)
    }

    private class GeneratedParser<V, TagType>(
        private val syntax: PegExpression<V, TagType>,
        private val option: GeneratorOption,
    ) : Parser<V> {
        override fun parse(input: String): Result<ParsingResult<V>, ErrorInfo> {
            val source = ParserSource.newWith(input)
            val context = ParserContext.new(syntax)

            val recorder = if (option.debug) DebuggingInfoRecorderImpl() else NullDebuggingInfoRecorder()

            return PegExpressionMiniParser(syntax, recorder).parse(source, context)
        }
    }
}
