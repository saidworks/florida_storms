/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import static io.cucumber.junit.platform.engine.Constants.*;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.saidworks.florida_storms.it")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ConfigurationParameter(
        key = "cucumber.plugin",
        value =
                "pretty,"
                        + "html:target/cucumber-reports/index.html,"
                        + "json:target/cucumber-reports/cucumber-report.json,"
                        + "junit:target/cucumber-reports/cucumber-report.xml")
@ConfigurationParameter(key = "cucumber.publish", value = "true")
public class CucumberIT {}
