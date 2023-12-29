package com.rrain.kupidon.util


fun main(){
  run { // small red dot
    val url = DataUrl(
      "data:image/jpeg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD"
    )
    println("uri.props: ${url.props}")
    println("uri.data: ${url.data}")
    println("uri.mimeType: ${url.mimeType}")
    println("uri.isBase64: ${url.isBase64}")
  }
  run { // rainbow circle
    val url = DataUrl(
      "data:image/webp;base64,UklGRqQBAABXRUJQVlA4WAoAAAAQAAAADwAADwAAQUxQSMcAAAABgKRt27Hlzq5FdDTr1yq4gYxRC2jcjDuwbdfItm3bjfxdeQ0RMQH6NdhsCtE/Xf1vRnd1i/M3j3QwSi+BfO8fKUfQccnPIknOnkeogY3m5rEPu9R7MMNN72nlksFJXZaCX6nioMMWV/QETPmZYHLqJVxKbhsFTCagvzVGijDGlsDsNwFc50RF1t1RxXuYsiuPMRbLTzqfGboakOzGeEvjFtTByahbUgE/d/thPVWSvPOB/VogzeuHZK+tGOSt16V/hphMwfoVAFZQOCC2AAAAcAMAnQEqEAAQAAIANCWwAnS3AETVewn5RhMyE/cLRAq84OQAAP7sJjZi2vltQVBtNESasiNNPNYI+PcqyMEU4i1/XVdYjQ4vQp/F40ttvfOzsGPdXqjTNaRZUQHdP3bDduNg0gtbNHTYFYCP/VOdNoX1/+ObtPOcjPnVjeNN/yOPz99Ks7xmS516zD7AO3/4y2OPif/oef1e3R0P4sXmRdv53iiSbRLc7R71eIXE78oaxJruAAA="
    )
    println("uri.props: ${url.props}")
    println("uri.data: ${url.data}")
    println("uri.mimeType: ${url.mimeType}")
    println("uri.isBase64: ${url.isBase64}")
  }
}




class DataUrl(dataUrl: String){
  val props: List<String>
  val data: String
  
  init {
    val schemeSeparatorIdx = dataUrl.indexOf(":")
    if (schemeSeparatorIdx==-1)
      throw RuntimeException("Url must have scheme separator ':'")
    
    val scheme = dataUrl.substring(0,schemeSeparatorIdx)
    if (scheme!="data")
      throw RuntimeException("Data Url scheme must be 'data'")
    
    val path = dataUrl.substring(schemeSeparatorIdx+1)
    val dataSeparatorIdx = path.indexOf(",")
    if (dataSeparatorIdx==-1)
      throw RuntimeException("Data Url must have data separator ','")
    
    data = path.substring(dataSeparatorIdx+1)
    val propsStr = path.substring(0,dataSeparatorIdx)
    
    props = propsStr.split(";")
    
  }
  
  val mimeType = props.getOrElse(0) { "" }
  val isBase64 = props.lastOrNull() == "base64"
  
}




/*
  The minimal data URL is "data:,"
  
  Examples of data URLs:
  
  data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh
  
  data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678
  
  data:image/jpeg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD
  
  <img alt="Red dot" src="data:image/svg+xml;utf8,
    <svg width='10' height='10' xmlns='http://www.w3.org/2000/svg'>
     <circle style='fill:red' cx='5' cy='5' r='5'/>
    </svg>"
  />
  
  Small red dot:
  <img alt="" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
  style="width:36pt;height:36pt"
  />
  
  Rainbow circle webp:
  data:image/webp;base64,UklGRqQBAABXRUJQVlA4WAoAAAAQAAAADwAADwAAQUxQSMcAAAABgKRt27Hlzq5FdDTr1yq4gYxRC2jcjDuwbdfItm3bjfxdeQ0RMQH6NdhsCtE/Xf1vRnd1i/M3j3QwSi+BfO8fKUfQccnPIknOnkeogY3m5rEPu9R7MMNN72nlksFJXZaCX6nioMMWV/QETPmZYHLqJVxKbhsFTCagvzVGijDGlsDsNwFc50RF1t1RxXuYsiuPMRbLTzqfGboakOzGeEvjFtTByahbUgE/d/thPVWSvPOB/VogzeuHZK+tGOSt16V/hphMwfoVAFZQOCC2AAAAcAMAnQEqEAAQAAIANCWwAnS3AETVewn5RhMyE/cLRAq84OQAAP7sJjZi2vltQVBtNESasiNNPNYI+PcqyMEU4i1/XVdYjQ4vQp/F40ttvfOzsGPdXqjTNaRZUQHdP3bDduNg0gtbNHTYFYCP/VOdNoX1/+ObtPOcjPnVjeNN/yOPz99Ks7xmS516zD7AO3/4y2OPif/oef1e3R0P4sXmRdv53iiSbRLc7R71eIXE78oaxJruAAA=
 
 A data URI consists of a 'scheme' and a 'path', with no authority part, query string, or fragment.
 The optional media type, the optional base64 indicator, and the data are all parts of the URI path.

 https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs#syntax
 DataUrl Syntax: data:[<MIME-type>][;charset=<encoding>][;base64],<data>
 
 <MIME-type> is MIME type (without parameter)
 https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
 Can contain optional parameter: type/subtype;parameter=value
 text/plain;charset=UTF-8
 text/plain;charset=US-ASCII
 
 In Data Url if omitted, defaults to text/plain;charset=US-ASCII
 
 
*/