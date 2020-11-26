/*
 *
 *  * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License").
 *  * You may not use this file except in compliance with the License.
 *  * A copy of the License is located at
 *  *
 *  *  http://aws.amazon.com/apache2.0
 *  *
 *  * or in the "license" file accompanying this file. This file is distributed
 *  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  * express or implied. See the License for the specific language governing
 *  * permissions and limitations under the License.
 *
 */

package software.amazon.smithy.swift.codegen

import io.kotest.matchers.string.shouldContainOnlyOnce
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.smithy.build.MockManifest
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.swift.codegen.integration.ProtocolGenerator

class StructEncodeGenerationTests : TestsBase() {
    var model = createModelFromSmithy("http-binding-protocol-generator-test.smithy")

    data class TestContext(val ctx: ProtocolGenerator.GenerationContext, val manifest: MockManifest, val generator: MockHttpProtocolGenerator)

    private fun newTestContext(): TestContext {
        val manifest = MockManifest()
        val provider: SymbolProvider = SwiftCodegenPlugin.createSymbolProvider(model, "Example")
        val serviceShapeIdWithNamespace = "com.test#Example"
        val service = model.getShape(ShapeId.from(serviceShapeIdWithNamespace)).get().asServiceShape().get()
        val settings = SwiftSettings.from(model, buildDefaultSwiftSettingsObjectNode(serviceShapeIdWithNamespace))
        model = AddOperationShapes.execute(model, settings.getService(model), settings.moduleName)
        model = RecursiveShapeBoxer.transform(model)
        val delegator = SwiftDelegator(settings, model, manifest, provider)
        val generator = MockHttpProtocolGenerator()
        val ctx = ProtocolGenerator.GenerationContext(settings, model, service, provider, listOf(), generator.protocol, delegator)
        return TestContext(ctx, manifest, generator)
    }

    val newTestContext = newTestContext()

    init {
        newTestContext.generator.generateSerializers(newTestContext.ctx)
        newTestContext.ctx.delegator.flushWriters()
    }

    @Test
    fun `it creates encodable conformance in correct file`() {
        Assertions.assertTrue(newTestContext.manifest.hasFile("/example/models/SmokeTestRequest+Encodable.swift"))
    }

    @Test
    fun `it creates encodable conformance for nested structures`() {
        // test that a struct member of an input operation shape also gets encodable conformance
        Assertions.assertTrue(newTestContext.manifest.hasFile("/example/models/Nested+Encodable.swift"))
        // these are non-top level shapes reachable from an operation input and thus require encodable conformance
        Assertions.assertTrue(newTestContext.manifest.hasFile("/example/models/Nested2+Encodable.swift"))
        Assertions.assertTrue(newTestContext.manifest.hasFile("/example/models/Nested3+Encodable.swift"))
        Assertions.assertTrue(newTestContext.manifest.hasFile("/example/models/Nested4+Encodable.swift"))
    }

    @Test
    fun `it creates smoke test request encodable conformance`() {
        val contents = getModelFileContents("example", "SmokeTestRequest+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension SmokeTestRequest: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case payload1
                    case payload2
                    case payload3
                }

                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let payload1 = payload1 {
                        try container.encode(payload1, forKey: .payload1)
                    }
                    if let payload2 = payload2 {
                        try container.encode(payload2, forKey: .payload2)
                    }
                    if let payload3 = payload3 {
                        try container.encode(payload3, forKey: .payload3)
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it encodes nested documents with aggregate shapes`() {
        val contents = getModelFileContents("example", "Nested4+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension Nested4: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case intList
                    case intMap
                    case member1
                }
            
                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let intList = intList {
                        var intListContainer = container.nestedUnkeyedContainer(forKey: .intList)
                        for intlist0 in intList {
                            if let intlist0 = intlist0 {
                                try intListContainer.encode(intlist0)
                            }
                        }
                    }
                    if let intMap = intMap {
                        var intMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .intMap)
                        for (key0, intmap0) in intMap {
                            if let intmap0 = intmap0 {
                                try intMapContainer.encode(intmap0, forKey: Key(stringValue: key0))
                            }
                        }
                    }
                    if let member1 = member1 {
                        try container.encode(member1, forKey: .member1)
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it provides encodable conformance to operation inputs with timestamps`() {
        val contents = getModelFileContents("example", "TimestampInputRequest+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension TimestampInputRequest: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case dateTime
                    case epochSeconds
                    case httpDate
                    case normal
                    case timestampList
                }
            
                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let dateTime = dateTime {
                        try container.encode(dateTime.iso8601WithoutFractionalSeconds(), forKey: .dateTime)
                    }
                    if let epochSeconds = epochSeconds {
                        try container.encode(epochSeconds.timeIntervalSince1970, forKey: .epochSeconds)
                    }
                    if let httpDate = httpDate {
                        try container.encode(httpDate.rfc5322(), forKey: .httpDate)
                    }
                    if let normal = normal {
                        try container.encode(normal.iso8601WithoutFractionalSeconds(), forKey: .normal)
                    }
                    if let timestampList = timestampList {
                        var timestampListContainer = container.nestedUnkeyedContainer(forKey: .timestampList)
                        for timestamplist0 in timestampList {
                            if let timestamplist0 = timestamplist0 {
                                try timestampListContainer.encode(timestamplist0.iso8601WithoutFractionalSeconds())
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it encodes maps correctly`() {
        val contents = getModelFileContents("example", "MapInputRequest+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension MapInputRequest: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case blobMap
                    case dateMap
                    case enumMap
                    case intMap
                    case structMap
                }
            
                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let blobMap = blobMap {
                        var blobMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .blobMap)
                        for (key0, blobmap0) in blobMap {
                            if let blobmap0 = blobmap0 {
                                try blobMapContainer.encode(blobmap0.base64EncodedString(), forKey: Key(stringValue: key0))
                            }
                        }
                    }
                    if let dateMap = dateMap {
                        var dateMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .dateMap)
                        for (key0, datemap0) in dateMap {
                            if let datemap0 = datemap0 {
                                try dateMapContainer.encode(datemap0.rfc5322(), forKey: Key(stringValue: key0))
                            }
                        }
                    }
                    if let enumMap = enumMap {
                        var enumMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .enumMap)
                        for (key0, enummap0) in enumMap {
                            if let enummap0 = enummap0 {
                                try enumMapContainer.encode(enummap0.rawValue, forKey: Key(stringValue: key0))
                            }
                        }
                    }
                    if let intMap = intMap {
                        var intMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .intMap)
                        for (key0, intmap0) in intMap {
                            if let intmap0 = intmap0 {
                                try intMapContainer.encode(intmap0, forKey: Key(stringValue: key0))
                            }
                        }
                    }
                    if let structMap = structMap {
                        var structMapContainer = container.nestedContainer(keyedBy: Key.self, forKey: .structMap)
                        for (key0, structmap0) in structMap {
                            if let structmap0 = structmap0 {
                                try structMapContainer.encode(structmap0, forKey: Key(stringValue: key0))
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it encodes nested enums correctly`() {
        val contents = getModelFileContents("example", "EnumInputRequest+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension EnumInputRequest: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case nestedWithEnum
                }

                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let nestedWithEnum = nestedWithEnum {
                        try container.encode(nestedWithEnum, forKey: .nestedWithEnum)
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)

        val contents2 = getModelFileContents("example", "NestedEnum+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents2 =
            """
            extension NestedEnum: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case myEnum
                }

                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let myEnum = myEnum {
                        try container.encode(myEnum.rawValue, forKey: .myEnum)
                    }
                }
            }
            """.trimIndent()
        contents2.shouldContainOnlyOnce(expectedContents2)
    }

    @Test
    fun `it encodes recursive boxed types correctly`() {
        val contents = getModelFileContents("example", "RecursiveShapesInputOutputNested1+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            extension RecursiveShapesInputOutputNested1: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case foo
                    case nested
                }
            
                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let foo = foo {
                        try container.encode(foo, forKey: .foo)
                    }
                    if let nested = nested {
                        try container.encode(nested.value, forKey: .nested)
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it encodes one side of the recursive shape`() {
        val contents = getModelFileContents("example", "RecursiveShapesInputOutputNested2+Encodable.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
                """
            extension RecursiveShapesInputOutputNested2: Encodable {
                private enum CodingKeys: String, CodingKey {
                    case bar
                    case recursiveMember
                }

                public func encode(to encoder: Encoder) throws {
                    var container = encoder.container(keyedBy: CodingKeys.self)
                    if let bar = bar {
                        try container.encode(bar, forKey: .bar)
                    }
                    if let recursiveMember = recursiveMember {
                        try container.encode(recursiveMember, forKey: .recursiveMember)
                    }
                }
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }
}