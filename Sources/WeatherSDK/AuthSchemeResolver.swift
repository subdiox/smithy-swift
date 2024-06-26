// Code generated by smithy-swift-codegen. DO NOT EDIT!

import ClientRuntime

public struct WeatherAuthSchemeResolverParameters: ClientRuntime.AuthSchemeResolverParameters {
    public let operation: String
    // Region is used for SigV4 auth scheme
    public let region: String?
}

public protocol WeatherAuthSchemeResolver: ClientRuntime.AuthSchemeResolver {
    // Intentionally empty.
    // This is the parent protocol that all auth scheme resolver implementations of
    // the service Weather must conform to.
}

public struct DefaultWeatherAuthSchemeResolver: WeatherAuthSchemeResolver {
    public func resolveAuthScheme(params: ClientRuntime.AuthSchemeResolverParameters) throws -> [AuthOption] {
        var validAuthOptions = [AuthOption]()
        guard let serviceParams = params as? WeatherAuthSchemeResolverParameters else {
            throw ClientError.authError("Service specific auth scheme parameters type must be passed to auth scheme resolver.")
        }
        switch serviceParams.operation {
            case "onlyHttpApiKeyAuth":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpApiKeyAuth"))
            case "onlyHttpApiKeyAuthOptional":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpApiKeyAuth"))
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#noAuth"))
            case "onlyHttpBearerAuth":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpBearerAuth"))
            case "onlyHttpBearerAuthOptional":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpBearerAuth"))
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#noAuth"))
            case "onlyHttpApiKeyAndBearerAuth":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpApiKeyAuth"))
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpBearerAuth"))
            case "onlyHttpApiKeyAndBearerAuthReversed":
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpBearerAuth"))
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#httpApiKeyAuth"))
            case "onlySigv4Auth":
                var sigV4Option = AuthOption(schemeID: "aws.auth#sigv4")
                sigV4Option.signingProperties.set(key: AttributeKeys.signingName, value: "weather")
                guard let region = serviceParams.region else {
                    throw ClientError.authError("Missing region in auth scheme parameters for SigV4 auth scheme.")
                }
                sigV4Option.signingProperties.set(key: AttributeKeys.signingRegion, value: region)
                validAuthOptions.append(sigV4Option)
            case "onlySigv4AuthOptional":
                var sigV4Option = AuthOption(schemeID: "aws.auth#sigv4")
                sigV4Option.signingProperties.set(key: AttributeKeys.signingName, value: "weather")
                guard let region = serviceParams.region else {
                    throw ClientError.authError("Missing region in auth scheme parameters for SigV4 auth scheme.")
                }
                sigV4Option.signingProperties.set(key: AttributeKeys.signingRegion, value: region)
                validAuthOptions.append(sigV4Option)
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#noAuth"))
            case "onlyFakeAuth":
                validAuthOptions.append(AuthOption(schemeID: "common#fakeAuth"))
            case "onlyFakeAuthOptional":
                validAuthOptions.append(AuthOption(schemeID: "common#fakeAuth"))
                validAuthOptions.append(AuthOption(schemeID: "smithy.api#noAuth"))
            default:
                var sigV4Option = AuthOption(schemeID: "aws.auth#sigv4")
                sigV4Option.signingProperties.set(key: AttributeKeys.signingName, value: "weather")
                guard let region = serviceParams.region else {
                    throw ClientError.authError("Missing region in auth scheme parameters for SigV4 auth scheme.")
                }
                sigV4Option.signingProperties.set(key: AttributeKeys.signingRegion, value: region)
                validAuthOptions.append(sigV4Option)
        }
        return validAuthOptions
    }

    public func constructParameters(context: HttpContext) throws -> ClientRuntime.AuthSchemeResolverParameters {
        guard let opName = context.getOperation() else {
            throw ClientError.dataNotFound("Operation name not configured in middleware context for auth scheme resolver params construction.")
        }
        let opRegion = context.getRegion()
        return WeatherAuthSchemeResolverParameters(operation: opName, region: opRegion)
    }
}
