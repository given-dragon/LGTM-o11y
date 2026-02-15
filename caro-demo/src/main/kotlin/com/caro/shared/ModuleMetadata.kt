package com.caro.shared

import org.jspecify.annotations.NullMarked
import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.ApplicationModule.Type
import org.springframework.modulith.PackageInfo

@PackageInfo
@NullMarked
@ApplicationModule(displayName = "Shared", type = Type.OPEN)
class ModuleMetadata