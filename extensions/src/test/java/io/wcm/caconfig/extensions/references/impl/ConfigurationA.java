package io.wcm.caconfig.extensions.references.impl;

import org.apache.sling.caconfig.annotation.Configuration;

@Configuration(name = "configA", label = "Configuration A")
@interface ConfigurationA {

    String key() default "";
}
