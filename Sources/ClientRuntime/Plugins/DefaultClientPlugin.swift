//
// Copyright Amazon.com Inc. or its affiliates.
// All Rights Reserved.
//
// SPDX-License-Identifier: Apache-2.0
//

import struct SmithyRetries.DefaultRetryStrategy

public class DefaultClientPlugin: Plugin {
    public init() {}
    public func configureClient(clientConfiguration: ClientConfiguration) {
        if var config = clientConfiguration as? DefaultClientConfiguration {
            config.retryStrategyOptions =
                DefaultSDKRuntimeConfiguration<DefaultRetryStrategy, DefaultRetryErrorInfoProvider>
                    .defaultRetryStrategyOptions
        }

        if var config = clientConfiguration as? DefaultHttpClientConfiguration {
            let httpClientConfiguration =
                DefaultSDKRuntimeConfiguration<DefaultRetryStrategy, DefaultRetryErrorInfoProvider>
                    .defaultHttpClientConfiguration
            config.httpClientConfiguration = httpClientConfiguration
            config.httpClientEngine =
                DefaultSDKRuntimeConfiguration<DefaultRetryStrategy, DefaultRetryErrorInfoProvider>
                    .makeClient(httpClientConfiguration: httpClientConfiguration)
        }
    }
}
