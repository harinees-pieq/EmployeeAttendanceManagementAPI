package config

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.core.Configuration
import io.dropwizard.db.DataSourceFactory

class MyConfiguration : Configuration() {
    @JsonProperty("database")
    val database = DataSourceFactory()
}