package io.wcm.caconfig.sample.model;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import io.wcm.caconfig.sample.config.ConfigSampleNested;
import io.wcm.caconfig.sample.config.ConfigSampleSub;

/**
 * Demonstrates bug from WCON-54
 */
@Model(adaptables = Resource.class)
public class SampleBugModel {

  @Self
  private Resource resource;

  private ConfigSampleNested configSampleNested;

  @PostConstruct
  private void init() {
    // Initialize the ConfigSampleNested - ConfigurationBuilder
    ConfigurationBuilder configurationBuilder = resource.adaptTo(ConfigurationBuilder.class);
    configSampleNested = configurationBuilder.as(ConfigSampleNested.class);
  }

  public ConfigSampleNested getConfigSampleNested() {
    return configSampleNested;
  }

  public ConfigSampleSub[] getSampleSubConfigs() {
    return configSampleNested.sub();
  }

}
