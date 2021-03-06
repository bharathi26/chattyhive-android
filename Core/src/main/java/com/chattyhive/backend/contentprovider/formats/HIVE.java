package com.chattyhive.backend.contentprovider.formats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

    import java.util.Date;
import com.chattyhive.backend.util.formatters.TimestampFormatter;
    import com.chattyhive.backend.contentprovider.formats.CHAT;
    import java.util.ArrayList;
    

 /*
  * Automatically generated code by ChattyHive API Manager Code Generator on 28/09/2014.
  * Be careful to not modify this file since your changes will not be included in future
  * versions of this file.
  *
  * ChattyHive API Manager Code Generator was created by Jonathan on 25/06/2014.
 */
public class HIVE extends Format {
	public String NAME_URL;
    public String NAME;
    public String CATEGORY;
    public String DESCRIPTION;
    public String IMAGE_URL;
    public Date CREATION_DATE;
    public CHAT PUBLIC_CHAT;
    public ArrayList<String> TAGS;
    public ArrayList<String> CHAT_LANGUAGES;
    public Integer SUBSCRIBED_USERS;
    

    public HIVE() {
        super();
    }

    public HIVE(JsonElement data) {
        this();
        this.fromJSON(data);
    }

    @Override
    public JsonElement toJSON() {
        JsonObject jsonObject = new JsonObject();

	    if ((this.NAME_URL != null) && (!this.NAME_URL.isEmpty()))
            jsonObject.addProperty("NAME_URL",this.NAME_URL);
      else
            jsonObject.add("NAME_URL", JsonNull.INSTANCE);            
        if ((this.NAME != null) && (!this.NAME.isEmpty()))
            jsonObject.addProperty("NAME",this.NAME);
      else
            jsonObject.add("NAME", JsonNull.INSTANCE);            
        if ((this.CATEGORY != null) && (!this.CATEGORY.isEmpty()))
            jsonObject.addProperty("CATEGORY",this.CATEGORY);
      else
            jsonObject.add("CATEGORY", JsonNull.INSTANCE);            
        if ((this.DESCRIPTION != null) && (!this.DESCRIPTION.isEmpty()))
            jsonObject.addProperty("DESCRIPTION",this.DESCRIPTION);
      else
            jsonObject.add("DESCRIPTION", JsonNull.INSTANCE);
        if ((this.IMAGE_URL != null) && (!this.IMAGE_URL.isEmpty()))
            jsonObject.addProperty("IMAGE_URL",this.IMAGE_URL);
        else
            jsonObject.add("IMAGE_URL", JsonNull.INSTANCE);
        if ((this.CREATION_DATE != null) && (!TimestampFormatter.toString(this.CREATION_DATE).isEmpty()))
            jsonObject.addProperty("CREATION_DATE", TimestampFormatter.toString(this.CREATION_DATE));
      else
            jsonObject.add("CREATION_DATE", JsonNull.INSTANCE);
            
        if (this.PUBLIC_CHAT != null) {
            JsonElement jsonElement = this.PUBLIC_CHAT.toJSON();
            if (!jsonElement.isJsonNull())
                jsonObject.add("PUBLIC_CHAT",jsonElement);
        }
      else
            jsonObject.add("PUBLIC_CHAT", JsonNull.INSTANCE);

      if (this.TAGS != null) {
            JsonArray jsonArray = new JsonArray();
            for (String element : this.TAGS) {
                JsonElement jsonElement =   new JsonPrimitive(element);
                if (!jsonElement.isJsonNull())
                    jsonArray.add(jsonElement);
            }

            if (jsonArray.size() > 0)
                jsonObject.add("TAGS",jsonArray);
            else
                jsonObject.add("TAGS", JsonNull.INSTANCE);
        }
      else
            jsonObject.add("TAGS", JsonNull.INSTANCE);

        if (this.CHAT_LANGUAGES != null) {
            JsonArray jsonArray = new JsonArray();
            for (String element : this.CHAT_LANGUAGES) {
                JsonElement jsonElement =   new JsonPrimitive(element);
                if (!jsonElement.isJsonNull())
                    jsonArray.add(jsonElement);
            }

            if (jsonArray.size() > 0)
                jsonObject.add("CHAT_LANGUAGES",jsonArray);
            else
                jsonObject.add("CHAT_LANGUAGES", JsonNull.INSTANCE);
        }
        else
            jsonObject.add("CHAT_LANGUAGES", JsonNull.INSTANCE);

        if (this.SUBSCRIBED_USERS != null)
            jsonObject.addProperty("SUBSCRIBED_USERS",this.SUBSCRIBED_USERS);
        else
            jsonObject.add("SUBSCRIBED_USERS", JsonNull.INSTANCE);

        if (jsonObject.entrySet().isEmpty())
            return JsonNull.INSTANCE;

        JsonObject result = new JsonObject();
        result.add("HIVE",jsonObject);

        return result;
    }

    @Override
    public void fromJSON(JsonElement data) {
        JsonObject object = data.getAsJsonObject().getAsJsonObject("HIVE");
        if ((object == null) || (!object.isJsonObject())) {
            object = data.getAsJsonObject();
        }
        if ((object == null) || (!object.isJsonObject())) {
            throw new IllegalArgumentException("Data is not an HIVE object.");
        }

        JsonElement property;

	    property = object.get("NAME_URL");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.NAME_URL = property.getAsString();
            
        property = object.get("NAME");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.NAME = property.getAsString();
            
        property = object.get("CATEGORY");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.CATEGORY = property.getAsString();
            
        property = object.get("DESCRIPTION");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.DESCRIPTION = property.getAsString();

        property = object.get("IMAGE_URL");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.IMAGE_URL = property.getAsString();

        property = object.get("CREATION_DATE");
        if ((property != null) && (property.isJsonPrimitive()) && (property.getAsString() != null) && (!property.getAsString().isEmpty()))
            this.CREATION_DATE = TimestampFormatter.toDate(property.getAsString());
            
        property = object.get("PUBLIC_CHAT");
        if ((property != null) && (property.isJsonObject())) {
            this.PUBLIC_CHAT = new CHAT(property);
        }
        
        property = object.get("TAGS");
        if ((property != null) && (property.isJsonArray())) {
            this.TAGS = new ArrayList<String>();
            JsonArray array = property.getAsJsonArray();
            for (JsonElement jsonElement : array)
                this.TAGS.add(  jsonElement.getAsString() );
        }

        property = object.get("CHAT_LANGUAGES");
        if ((property != null) && (property.isJsonArray())) {
            this.CHAT_LANGUAGES = new ArrayList<String>();
            JsonArray array = property.getAsJsonArray();
            for (JsonElement jsonElement : array)
                this.CHAT_LANGUAGES.add(  jsonElement.getAsString() );
        }

        property = object.get("SUBSCRIBED_USERS");
        if ((property != null) && (property.isJsonPrimitive()) && (!property.isJsonNull()))
            this.SUBSCRIBED_USERS = property.getAsInt();
      
    }
}