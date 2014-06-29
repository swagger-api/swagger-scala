package com.wordnik

package object swagger {

  object annotations {
    import scala.annotation.meta.field

    @deprecated("because of swagger spec 1.2 this got renamed to ApiModelProperty", "2.2.2")
    type ApiProperty = com.wordnik.swagger.runtime.annotations.ApiModelProperty @field

    type ApiModelProperty = com.wordnik.swagger.runtime.annotations.ApiModelProperty @field

    type ApiModel = com.wordnik.swagger.runtime.annotations.ApiModel
    type XmlRootElement = javax.xml.bind.annotation.XmlRootElement

    type ApiEnum = com.wordnik.swagger.runtime.annotations.ApiEnum
    @deprecated("In swagger spec 1.2 this was replaced with com.wordnik.swagger.ResponseMessage", "2.2.2")
    type Error = com.wordnik.swagger.ResponseMessage[String]
  }


  private[swagger] implicit class RicherString(s: String) {
    def isBlank = s == null || s.trim.isEmpty
    def nonBlank = !isBlank
    def blankOption = if (isBlank) None else Some(s)
    def toCheckboxBool = s.toUpperCase match {
      case "ON" | "TRUE" | "OK" | "1" | "CHECKED" | "YES" | "ENABLE" | "ENABLED" => true
      case _ => false
    }
  }
}
