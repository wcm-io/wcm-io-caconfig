package io.wcm.caconfig.extensions.references.impl;

import org.apache.sling.caconfig.annotation.Configuration;

@Configuration(name = "configB", label = "Configuration B")
@interface ConfigurationB {

    String key() default "";
}
