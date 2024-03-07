package br.com.sanara.ecommerce;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MessageAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {

    @Override
    public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        //adiciona o tipo da classe/properiedade do obj recebido
        obj.addProperty("type", message.getPayload().getClass().getName());

        //adiciona valor aos objetos para carregar
        obj.add("payload", context.serialize(message.getPayload()));
        obj.add("correlationId", context.serialize(message.getId()));
        return obj;
    }

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        var obj = jsonElement.getAsJsonObject();
        var payloadType = obj.get("type").getAsString(); //pega o tipo da classe
        //deserializa conforme os tipos do objetos
        var correlationId = (CorrelationId) context.deserialize(obj.get("correlationId"), CorrelationId.class);
        try {
            //no lugar de Class.forName(payloadType) é bom usar uma lista de classes que vc vai aceitar
            //para não correr o risco de ataques
            var payload = context.deserialize(obj.get("payload"), Class.forName(payloadType));
            return new Message(correlationId, payload);

        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }
}
