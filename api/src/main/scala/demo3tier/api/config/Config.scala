package demo3tier.api.config

import demo3tier.api.http.HttpConfig

/**
  * Maps to the `application.conf` file. Configuration for all modules of the application.
  */
case class Config(
    env: Env,
    api: HttpConfig,
    core: CoreConfig
)
