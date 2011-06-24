package play.db.jpa

import annotation.target.field

// These wrappers are needed because type alias does not work for java enums

private [jpa] object CascadeTypeWrapper {
    final val ALL = javax.persistence.CascadeType.ALL
    final val MERGE = javax.persistence.CascadeType.MERGE
    final val PERSIST = javax.persistence.CascadeType.PERSIST
    final val REFRESH = javax.persistence.CascadeType.REFRESH
    final val REMOVE  = javax.persistence.CascadeType.REMOVE 
}


private [jpa] object LockModeTypeWrapper {
    final val READ = javax.persistence.LockModeType.READ
    final val WRITE = javax.persistence.LockModeType.WRITE 
    final val OPTIMISTIC = javax.persistence.LockModeType.OPTIMISTIC
    final val OPTIMISTIC_FORCE_INCREMENT = javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT
    final val PESSIMISTIC_READ = javax.persistence.LockModeType.PESSIMISTIC_READ
    final val PESSIMISTIC_WRITE = javax.persistence.LockModeType.PESSIMISTIC_WRITE
    final val PESSIMISTIC_FORCE_INCREMENT = javax.persistence.LockModeType.PESSIMISTIC_FORCE_INCREMENT
    final val NONE = javax.persistence.LockModeType.NONE
}

private[jpa] object FetchTypeWrapper {
    final val EAGER = javax.persistence.FetchType.EAGER
    final val LAZY = javax.persistence.FetchType.LAZY
}

trait Annotations {
    
    // enums
    val CascadeType = CascadeTypeWrapper
    val LockModeType = LockModeTypeWrapper
    val FetchType = FetchTypeWrapper
    
    // classes
    type Table = javax.persistence.Table
    type Entity = javax.persistence.Entity
    type Inheritance = javax.persistence.Inheritance

    // javax.persistence field
    type  AttributeOverrides = javax.persistence.AttributeOverrides @field
    type  Basic = javax.persistence.Basic  @field
    type  Column = javax.persistence.Column @field
    type  ColumnResult = javax.persistence.ColumnResult  @field
    type  Embedded = javax.persistence.Embedded  @field
    type  EmbeddedId = javax.persistence.EmbeddedId  @field
    type  EntityResult = javax.persistence.EntityResult @field
    type  Enumerated = javax.persistence.Enumerated  @field
    type  ExcludeDefaultListeners = javax.persistence.ExcludeDefaultListeners  @field
    type  ExcludeSuperclassListeners = javax.persistence.ExcludeSuperclassListeners  @field
    type  FieldResult = javax.persistence.FieldResult  @field
    type  GeneratedValue = javax.persistence.GeneratedValue  @field
    type  Id = javax.persistence.Id  @field
    type  IdClass = javax.persistence.IdClass  @field
    type  JoinColumn = javax.persistence.JoinColumn  @field
    type  JoinColumns = javax.persistence.JoinColumns  @field
    type  JoinTable = javax.persistence.JoinTable  @field
    type  Lob = javax.persistence.Lob @field
    type  ManyToMany = javax.persistence.ManyToMany  @field
    type  ManyToOne = javax.persistence.ManyToOne  @field
    type  MapKey = javax.persistence.MapKey  @field
    type  OneToMany = javax.persistence.OneToMany  @field
    type  OneToOne = javax.persistence.OneToOne  @field
    type  OrderBy = javax.persistence.OrderBy  @field
    type  PostLoad = javax.persistence.PostLoad  @field
    type  PostPersist = javax.persistence.PostPersist  @field
    type  PostRemove = javax.persistence.PostRemove  @field
    type  PostUpdate = javax.persistence.PostUpdate  @field
    type  PrePersist = javax.persistence.PrePersist  @field
    type  PreRemove = javax.persistence.PreRemove   @field
    type  PreUpdate = javax.persistence.PreUpdate   @field
    type  QueryHint = javax.persistence.QueryHint  @field
    type  SequenceGenerator = javax.persistence.SequenceGenerator  @field
    type  TableGenerator = javax.persistence.TableGenerator  @field
    type  Temporal = javax.persistence.Temporal  @field
    type  Transient = javax.persistence.Transient  @field
    type  UniqueConstraint = javax.persistence.UniqueConstraint  @field
    type  Version = javax.persistence.Version  @field
    
}

object Annotations extends Annotations

