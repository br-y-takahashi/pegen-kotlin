package io.github.derui.pegen.core.parser

import io.github.derui.pegen.core.Tag
import io.github.derui.pegen.core.lang.PegSyntax
import io.github.derui.pegen.core.support.Err
import io.github.derui.pegen.core.support.Ok
import io.github.derui.pegen.core.support.Result

/**
 * A context for parsing.
 */
class ParserContext<T> private constructor(
    private var currentPosition: Position,
    private val input: String,
    private val startIndex: Int,
) {
    companion object {
        fun <T> startOf(input: String): ParserContext<T> = ParserContext(Position.start(), input, 0)
    }

    /**
     * Map of tag that is build in [PegExpression] and [PegExpressionIntermediate] and its [PegSyntax].
     */
    private val tags = mutableMapOf<Tag, ParsingResult<T>>()

    /**
     * current index of this context
     */
    private var currentIndex: Int = startIndex

    /**
     * Get the current position of the cursor.
     */
    val position get() = currentPosition

    /**
     * Get the next character and move the cursor forward.
     * If index is reached to the end of input, return [Err] with [ErrorInfo].
     */
    fun readChar(): Result<Char, ErrorInfo> {
        if (input.length <= currentIndex) {
            return Err(ErrorInfo.from("Unexpected end of input", currentPosition))
        }
        val c = input[currentIndex++]
        currentPosition = currentPosition.forward(c)

        return Ok(c)
    }

    /**
     * Get parsed string from start index to current index.
     */
    fun parsed(): String = input.substring(startIndex, currentIndex)

    /**
     * Get a new context with new input
     */
    fun newContext(): ParserContext<T> = ParserContext(currentPosition, input, currentIndex)

    /**
     * Register a tag and its syntax.
     */
    fun tagging(
        tag: Tag,
        result: ParsingResult<T>,
    ) {
        require(tag !in tags) {
            "Tag $tag is already registered. Please check your grammar."
        }

        tags[tag] = result
    }

    /**
     * Get tagged result
     */
    fun tagged(tag: Tag): ParsingResult<T>? = tags[tag]

    /**
     * A shortcut function to create error info from current context
     */
    fun errorOf(message: String) = ErrorInfo.from(message, this.position)

    // / Generated by IntelliJ IDEA ///
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParserContext<*>

        if (currentPosition != other.currentPosition) return false
        if (input != other.input) return false
        if (currentIndex != other.currentIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentPosition.hashCode()
        result = 31 * result + input.hashCode()
        result = 31 * result + currentIndex
        return result
    }

    override fun toString(): String {
        return "ParserContext(currentPosition=$currentPosition, input='$input', currentIndex=$currentIndex)"
    }
}
