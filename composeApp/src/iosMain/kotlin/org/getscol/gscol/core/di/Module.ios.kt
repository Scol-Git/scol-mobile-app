package org.getscol.gscol.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * IOS-specific module support

 */

actual val platformModule: Module = module {

    single<NSUserDefaults> { NSUserDefaults.standardUserDefaults() }

    single<HttpClientEngine> {
        Darwin.create()
    }
}
