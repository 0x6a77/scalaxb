/*
 * Copyright (c) 2010 e.e d3si9n
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 
package org.scalaxb.compiler

trait ScalaNames {
  def isCommonlyUsedWord(s: String) = s match {
    case "Option"
    | "Boolean"
    | "Char"
    | "Byte"
    | "Int"
    | "Double"
    | "Long"
    | "Short"
    | "Unit"
    | "String"
    | "Float"
    | "BigInt"
    | "BigDecimal"
    | "Array"
    | "Map"
    | "Product"
    | "Function"
     => true
    case _ => false
  }
  
  
  def isKeyword(str: String) =
    str match {
      case "abstract"
      | "case"
      | "class"
      | "catch"
      | "def"
      | "do"
      | "else"
      | "extends"
      | "false"
      | "final"
      | "finally"
      | "for"
      | "forSome"
      | "if"
      | "import"
      | "new"
      | "null"
      | "object"
      | "override"
      | "package"
      | "private"
      | "protected"
      | "return"
      | "sealed"
      | "super"
      | "this"
      | "throw"
      | "trait"
      | "true"
      | "try"
      | "type"
      | "val"
      | "var"
      | "with"
      | "while"
      | "yield" => true

      case _ => false
    }
    /* // these cannot appear as XML names
  case "." =>
  case "_" =>
  case ":" =>
  case "=" =>
  case "=>" =>
  case "<-" =>
  case "<:" =>
  case ">:" =>
  case "<%" =>
  case "#" =>
  case "@" =>
    */

}
