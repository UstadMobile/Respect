package world.respect.shared.util

expect class RespectURLEncoder {

   companion object{
       /**
        * Encode url string
        */
       fun encodeUTF8(text: String): String

       /**
        * Decode url string
        */
       fun decodeUTF8(text: String): String
   }

}