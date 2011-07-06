package play.data.validation

import annotation.target.field

/**
 * provides aliases for play's validations
 */
trait Annotations {
    type CheckWith = play.data.validation.CheckWith @field
    type Email = play.data.validation.Email @field
    type Equals = play.data.validation.Equals @field
    type InFuture = play.data.validation.InFuture @field
    type InPast = play.data.validation.InPast @field
    type IPv4Address = play.data.validation.IPv4Address @field
    type IPv6Address = play.data.validation.IPv6Address @field
    type IsTrue = play.data.validation.IsTrue @field
    type Match = play.data.validation.Match  @field
    type Max = play.data.validation.Max @field
    type MaxSize = play.data.validation.MaxSize @field
    type Min = play.data.validation.Min @field
    type MinSize = play.data.validation.MinSize @field
    type Password = play.data.validation.Password @field
    type Phone = play.data.validation.Phone @field
    type Range = play.data.validation.Range @field
    type Required = play.data.validation.Required @field 
    type URL = play.data.validation.URL @field
    type Valid = play.data.validation.Valid @field
}

object Annotations extends Annotations
