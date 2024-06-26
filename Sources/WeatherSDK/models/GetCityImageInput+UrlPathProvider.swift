// Code generated by smithy-swift-codegen. DO NOT EDIT!

import ClientRuntime

extension GetCityImageInput {

    static func urlPathProvider(_ value: GetCityImageInput) -> Swift.String? {
        guard let cityId = value.cityId else {
            return nil
        }
        return "/cities/\(cityId.urlPercentEncoding())/image"
    }
}
