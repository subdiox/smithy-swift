package software.amazon.smithy.swift.codegen.integration.serde.readwrite

import software.amazon.smithy.model.shapes.ListShape
import software.amazon.smithy.model.shapes.MapShape
import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.TimestampShape
import software.amazon.smithy.model.traits.TimestampFormatTrait
import software.amazon.smithy.model.traits.XmlFlattenedTrait
import software.amazon.smithy.swift.codegen.SwiftWriter
import software.amazon.smithy.swift.codegen.integration.ProtocolGenerator
import software.amazon.smithy.swift.codegen.integration.serde.json.TimestampUtils
import software.amazon.smithy.swift.codegen.model.getTrait
import software.amazon.smithy.swift.codegen.model.hasTrait

class ReadingClosureUtils(
    val ctx: ProtocolGenerator.GenerationContext,
    val writer: SwiftWriter
) {
    private val nodeInfoUtils = NodeInfoUtils(ctx, writer, ctx.service.responseWireProtocol)

    fun readingClosure(member: MemberShape, isSparse: Boolean = false): String {
        val target = ctx.model.expectShape(member.target)
        val memberTimestampFormatTrait = member.getTrait<TimestampFormatTrait>()
        val baseClosure = readingClosure(target, memberTimestampFormatTrait)
        if (isSparse) {
            return writer.format("optionalFormOf(readingClosure: \$L)", baseClosure)
        } else {
            return baseClosure
        }
    }

    private fun readingClosure(shape: Shape, memberTimestampFormatTrait: TimestampFormatTrait? = null): String {
        when (shape) {
            is MapShape -> {
                val valueReadingClosure = readingClosure(shape.value)
                val keyNodeInfo = nodeInfoUtils.nodeInfo(shape.key)
                val valueNodeInfo = nodeInfoUtils.nodeInfo(shape.value)
                val isFlattened = shape.hasTrait<XmlFlattenedTrait>()
                return writer.format(
                    "mapReadingClosure(valueReadingClosure: \$L, keyNodeInfo: \$L, valueNodeInfo: \$L, isFlattened: \$L)",
                    valueReadingClosure,
                    keyNodeInfo,
                    valueNodeInfo,
                    isFlattened
                )
            }
            is ListShape -> {
                val memberReadingClosure = readingClosure(shape.member)
                val memberNodeInfo = nodeInfoUtils.nodeInfo(shape.member)
                val isFlattened = shape.hasTrait<XmlFlattenedTrait>()
                return writer.format(
                    "listReadingClosure(memberReadingClosure: \$L, memberNodeInfo: \$L, isFlattened: \$L)",
                    memberReadingClosure,
                    memberNodeInfo,
                    isFlattened
                )
            }
            is TimestampShape -> {
                return writer.format(
                    "timestampReadingClosure(format: \$L)",
                    TimestampUtils.timestampFormat(ctx, memberTimestampFormatTrait, shape)
                )
            }
            else -> {
                return writer.format("\$N.read(from:)", ctx.symbolProvider.toSymbol(shape))
            }
        }
    }
}
