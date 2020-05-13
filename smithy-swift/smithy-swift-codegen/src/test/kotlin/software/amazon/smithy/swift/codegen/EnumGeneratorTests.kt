/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.smithy.swift.codegen

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.model.traits.EnumDefinition
import software.amazon.smithy.model.traits.EnumTrait

class EnumGeneratorTests : TestsBase() {

    @Test
    fun `generates unnamed enums`() {

        val stringShapeWithEnumTrait = createStringWithEnumTrait(
            EnumDefinition.builder().value("FOO").build(),
            EnumDefinition.builder().value("BAR").documentation("Documentation for BAR").build()
        )
        val model = createModelFromShapes(stringShapeWithEnumTrait)

        val provider: SymbolProvider = SwiftCodegenPlugin.createSymbolProvider(model, "test")
        val writer = SwiftWriter("MockPackage")

        val generator = EnumGenerator(model, provider, writer, stringShapeWithEnumTrait)
        generator.render()

        val contents = writer.toString()

        contents.shouldContain(SwiftWriter.staticHeader)

        val expectedGeneratedEnum = "" +
                "/**\n" +
                " * Documentation for the enum\n" +
                " */\n" +
                "enum MyEnum {\n" +
                "    /**\n" +
                "     * Documentation for BAR\n" +
                "     */\n" +
                "    case BAR\n" +
                "    case FOO\n" +
                "    case UNKNOWN(String)\n" +
                "}\n\n" +
                "extension MyEnum : Equatable, RawRepresentable, Codable, CaseIterable {\n" +
                "    static var allCases: [MyEnum] {\n" +
                "        return [\n" +
                "            .BAR,\n" +
                "            .FOO,\n" +
                "            .UNKNOWN(\"\")\n" +
                "        ]\n" +
                "    }\n" +
                "    init?(rawValue: String) {\n" +
                "        let value = Self.allCases.first(where: { \$0.rawValue == rawValue })\n" +
                "        self = value ?? Self.UNKNOWN(rawValue)\n" +
                "    }\n" +
                "    var rawValue: String {\n" +
                "        switch self {\n" +
                "        case .BAR: return \"BAR\"\n" +
                "        case .FOO: return \"FOO\"\n" +
                "        case let .UNKNOWN(s): return s\n" +
                "        }\n" +
                "    }\n" +
                "    init(from decoder: Decoder) throws {\n" +
                "        let container = try decoder.singleValueContainer()\n" +
                "        let rawValue = try container.decode(RawValue.self)\n" +
                "        self = MyEnum(rawValue: rawValue) ?? MyEnum.UNKNOWN(rawValue)\n" +
                "    }\n" +
                "}\n"

        contents.shouldContain(expectedGeneratedEnum)
    }

    @Test
    fun `generates named enums`() {
        val stringShapeWithEnumTrait = createStringWithEnumTrait(
            EnumDefinition.builder().value("t2.nano").name("T2_NANO").build(),
            EnumDefinition.builder().value("t2.micro")
                                    .name("T2_MICRO")
                                    .documentation("\"\"\"\n" +
                                        "T2 instances are Burstable Performance\n" +
                                        "Instances that provide a baseline level of CPU\n" +
                                        "performance with the ability to burst above the\n" +
                                        "baseline.\"\"\"")
                                    .build()
        )
        val model = createModelFromShapes(stringShapeWithEnumTrait)

        val provider: SymbolProvider = SwiftCodegenPlugin.createSymbolProvider(model, "test")
        val writer = SwiftWriter("MockPackage")

        val generator = EnumGenerator(model, provider, writer, stringShapeWithEnumTrait)
        generator.render()

        val contents = writer.toString()

        contents.shouldContain(SwiftWriter.staticHeader)

        val expectedGeneratedEnum = "" +
                "/**\n" +
                " * Documentation for the enum\n" +
                " */\n" +
                "enum MyEnum {\n" +
                "    /**\n" +
                "     * ${"\"\"\""}\n" +
                "     * T2 instances are Burstable Performance\n" +
                "     * Instances that provide a baseline level of CPU\n" +
                "     * performance with the ability to burst above the\n" +
                "     * baseline.${"\"\"\""}\n" +
                "     */\n" +
                "    case T2_MICRO\n" +
                "    case T2_NANO\n" +
                "    case UNKNOWN(String)\n" +
                "}\n\n" +
                "extension MyEnum : Equatable, RawRepresentable, Codable, CaseIterable {\n" +
                "    static var allCases: [MyEnum] {\n" +
                "        return [\n" +
                "            .T2_MICRO,\n" +
                "            .T2_NANO,\n" +
                "            .UNKNOWN(\"\")\n" +
                "        ]\n" +
                "    }\n" +
                "    init?(rawValue: String) {\n" +
                "        let value = Self.allCases.first(where: { \$0.rawValue == rawValue })\n" +
                "        self = value ?? Self.UNKNOWN(rawValue)\n" +
                "    }\n" +
                "    var rawValue: String {\n" +
                "        switch self {\n" +
                "        case .T2_MICRO: return \"t2.micro\"\n" +
                "        case .T2_NANO: return \"t2.nano\"\n" +
                "        case let .UNKNOWN(s): return s\n" +
                "        }\n" +
                "    }\n" +
                "    init(from decoder: Decoder) throws {\n" +
                "        let container = try decoder.singleValueContainer()\n" +
                "        let rawValue = try container.decode(RawValue.self)\n" +
                "        self = MyEnum(rawValue: rawValue) ?? MyEnum.UNKNOWN(rawValue)\n" +
                "    }\n" +
                "}\n"

        contents.shouldContain(expectedGeneratedEnum)
    }


    private fun createStringWithEnumTrait(vararg enumDefinitions: EnumDefinition) : StringShape {
        val enumTraitBuilder = EnumTrait.builder()
        for (enumDefinition in enumDefinitions) {
            enumTraitBuilder.addEnum(enumDefinition)
        }

        val shape = StringShape.builder()
            .id("smithy.example#MyEnum")
            .addTrait(enumTraitBuilder.build())
            .addTrait(DocumentationTrait("Documentation for the enum"))
            .build()

        return shape
    }
}