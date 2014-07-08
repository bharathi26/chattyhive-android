package com.chattyhive.backend.contentprovider.formats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

  import com.chattyhive.backend.contentprovider.formats.MESSAGE;
    

 /*
  * Automatically generated code by ChattyHive API Manager Code Generator on 30/06/2014.
  * Be careful to not modify this file since your changes will not be included in future
  * versions of this file.
  *
  * ChattyHive API Manager Code Generator was created by Jonathan on 25/06/2014.
 */
public class CHAT_SYNC extends Format {
	String PUSHER_CHANNEL;
    String CHANNEL_UNICODE;
    MESSAGE LAST_MESSAGE;
    

    public CHAT_SYNC() {
        super();
    }

    public CHAT_SYNC(JsonElement data) {
        this();
        this.fromJSON(data);
    }

    @Override
    public JsonElement toJSON() {
        JsonObject jsonObject = new JsonObject();

	    if ((this.PUSHER_CHANNEL != null) && (!this.PUSHER_CHANNEL.isEmpty()))
            jsonObject.addProperty("PUSHER_CHANNEL",this.PUSHER_CHANNEL);
            
        if ((this.CHANNEL_UNICODE != null) && (!this.CHANNEL_UNICODE.isEmpty()))
            jsonObject.addProperty("CHANNEL_UNICODE",this.CHANNEL_UNICODE);
            
        if (this.LAST_MESSAGE != null) {
            JsonElement jsonElement = this.LAST_MESSAGE.toJSON();
            if (!jsonElement.isJsonNull())
                jsonObject.add("LAST_MESSAGE",jsonElement);
        }
        
      

        if (jsonObject.entrySet().isEmpty())
            return JsonNull.INSTANCE;

        JsonObject result = new JsonObject();
        result.add("CHAT_SYNC",jsonObject);

        return result;
    }

    @Override
    public void fromJSON(JsonElement data) {
        JsonObject object = data.getAsJsonObject().getAsJsonObject("CHAT_SYNC");
        if ((object == null) || (!object.isJsonObject())) {
            object = data.getAsJsonObject();
        }
        if ((object == null) || (!object.isJsonObject())) {
            throw new IllegalArgumentException("Data is not an CHAT_SYNC object.");
        }

        JsonElement property;

	    property = object.get("PUSHER_CHANNEL");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.PUSHER_CHANNEL = property.getAsString();
            
        property = object.get("CHANNEL_UNICODE");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.CHANNEL_UNICODE = property.getAsString();
            
        property = object.get("LAST_MESSAGE");
        if ((property != null) && (property.isJsonObject())) {
            this.LAST_MESSAGE = new MESSAGE(property);
        }
        
      
    }
}