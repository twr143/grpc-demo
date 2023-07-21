package demo3tier.api.config

case class Sensitive(value: String) extends AnyVal {
  override def toString: String = "***"
}
