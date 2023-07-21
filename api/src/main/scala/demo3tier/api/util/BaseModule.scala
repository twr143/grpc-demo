package demo3tier.api.util

import java.time.Clock

import demo3tier.api.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}
