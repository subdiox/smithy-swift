import io.kotest.matchers.string.shouldContainOnlyOnce
import org.junit.jupiter.api.Test
import software.amazon.smithy.swift.codegen.AddOperationShapes

class HttpBodyMiddlewareTests {
    private var model = javaClass.getResource("http-binding-protocol-generator-test.smithy").asSmithy()
    var newTestContext: TestContext
    init {
        newTestContext = newTestContext()
        newTestContext.generator.generateSerializers(newTestContext.generationCtx)
        newTestContext.generator.generateProtocolClient(newTestContext.generationCtx)
        newTestContext.generator.generateDeserializers(newTestContext.generationCtx)
        newTestContext.generationCtx.delegator.flushWriters()
    }
    private fun newTestContext(): TestContext {
        val settings = model.defaultSettings()
        model = AddOperationShapes.execute(model, settings.getService(model), settings.moduleName)
        return model.newTestContext()
    }

    @Test
    fun `it builds body middleware smoke test`() {
        val contents = getModelFileContents("example", "SmokeTestInput+BodyMiddleware.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            public struct SmokeTestInputBodyMiddleware: Middleware {
                public let id: String = "SmokeTestInputBodyMiddleware"
            
                public init() {}
            
                public func handle<H>(context: Context,
                              input: SerializeStepInput<SmokeTestInput>,
                              next: H) -> Result<SerializeStepInput<SmokeTestInput>, Error>
                where H: Handler,
                Self.MInput == H.Input,
                Self.MOutput == H.Output,
                Self.Context == H.Context
                {
                    do {
                        if try !input.operationInput.allPropertiesAreNull() {
                            let encoder = context.getEncoder()
                            let data = try encoder.encode(input.operationInput)
                            let body = HttpBody.data(data)
                            input.builder.withBody(body)
                        }
                    } catch let err {
                        return .failure(err)
                    }
                    return next.handle(context: context, input: input)
                }
            
                public typealias MInput = SerializeStepInput<SmokeTestInput>
                public typealias MOutput = SerializeStepInput<SmokeTestInput>
                public typealias Context = HttpContext
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it builds body middleware for explicit string payloads`() {
        val contents = getModelFileContents("example", "ExplicitStringInput+BodyMiddleware.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            public struct ExplicitStringInputBodyMiddleware: Middleware {
                public let id: String = "ExplicitStringInputBodyMiddleware"
            
                public init() {}
            
                public func handle<H>(context: Context,
                              input: SerializeStepInput<ExplicitStringInput>,
                              next: H) -> Result<SerializeStepInput<ExplicitStringInput>, Error>
                where H: Handler,
                Self.MInput == H.Input,
                Self.MOutput == H.Output,
                Self.Context == H.Context
                {
                    if let payload1 = input.operationInput.payload1 {
                        let data = payload1.data(using: .utf8)
                        let body = HttpBody.data(data)
                        input.builder.withBody(body)
                    }
                    return next.handle(context: context, input: input)
                }
            
                public typealias MInput = SerializeStepInput<ExplicitStringInput>
                public typealias MOutput = SerializeStepInput<ExplicitStringInput>
                public typealias Context = HttpContext
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it builds body middleware for explicit blob payloads`() {
        val contents = getModelFileContents("example", "ExplicitBlobInput+BodyMiddleware.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            public struct ExplicitBlobInputBodyMiddleware: Middleware {
                public let id: String = "ExplicitBlobInputBodyMiddleware"
            
                public init() {}
            
                public func handle<H>(context: Context,
                              input: SerializeStepInput<ExplicitBlobInput>,
                              next: H) -> Result<SerializeStepInput<ExplicitBlobInput>, Error>
                where H: Handler,
                Self.MInput == H.Input,
                Self.MOutput == H.Output,
                Self.Context == H.Context
                {
                    if let payload1 = input.operationInput.payload1 {
                        let data = payload1
                        let body = HttpBody.data(data)
                        input.builder.withBody(body)
                    }
                    return next.handle(context: context, input: input)
                }
            
                public typealias MInput = SerializeStepInput<ExplicitBlobInput>
                public typealias MOutput = SerializeStepInput<ExplicitBlobInput>
                public typealias Context = HttpContext
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it builds body middleware for explicit streaming blob payloads`() {
        val contents = getModelFileContents("example", "ExplicitBlobStreamInput+BodyMiddleware.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            public struct ExplicitBlobStreamInputBodyMiddleware: Middleware {
                public let id: String = "ExplicitBlobStreamInputBodyMiddleware"
            
                public init() {}
            
                public func handle<H>(context: Context,
                              input: SerializeStepInput<ExplicitBlobStreamInput>,
                              next: H) -> Result<SerializeStepInput<ExplicitBlobStreamInput>, Error>
                where H: Handler,
                Self.MInput == H.Input,
                Self.MOutput == H.Output,
                Self.Context == H.Context
                {
                    if let payload1 = input.operationInput.payload1 {
                        let data = payload1
                        let body = HttpBody.data(data)
                        input.builder.withBody(body)
                    }
                    return next.handle(context: context, input: input)
                }
            
                public typealias MInput = SerializeStepInput<ExplicitBlobStreamInput>
                public typealias MOutput = SerializeStepInput<ExplicitBlobStreamInput>
                public typealias Context = HttpContext
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `it builds body middleware for explicit struct payloads`() {
        val contents = getModelFileContents("example", "ExplicitStructInput+BodyMiddleware.swift", newTestContext.manifest)
        contents.shouldSyntacticSanityCheck()
        val expectedContents =
            """
            public struct ExplicitStructInputBodyMiddleware: Middleware {
                public let id: String = "ExplicitStructInputBodyMiddleware"
            
                public init() {}
            
                public func handle<H>(context: Context,
                              input: SerializeStepInput<ExplicitStructInput>,
                              next: H) -> Result<SerializeStepInput<ExplicitStructInput>, Error>
                where H: Handler,
                Self.MInput == H.Input,
                Self.MOutput == H.Output,
                Self.Context == H.Context
                {
                    if let payload1 = input.operationInput.payload1 {
                        do {
                            let encoder = context.getEncoder()
                            let data = try encoder.encode(payload1)
                            let body = HttpBody.data(data)
                            input.builder.withBody(body)
                        } catch let err {
                            return .failure(err)
                        }
                    }
                    return next.handle(context: context, input: input)
                }
            
                public typealias MInput = SerializeStepInput<ExplicitStructInput>
                public typealias MOutput = SerializeStepInput<ExplicitStructInput>
                public typealias Context = HttpContext
            }
            """.trimIndent()
        contents.shouldContainOnlyOnce(expectedContents)
    }
}