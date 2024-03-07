package br.com.sanara.ecommerce.dispatcher;

import br.com.sanara.ecommerce.Message;
import br.com.sanara.ecommerce.MessageAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

/** � preciso fazer o Gson implementar o serializador do kafka para ele poder fazer
 * a serializa��o dos  valores
 * @param <T>
 */
public class GsonSerializer<T> implements Serializer<T> {

    //alterado o serializer para conforme a classe a ser recebida, corrigindo exceção
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageAdapter()).create();

    @Override
    public byte[] serialize(String s, T object) {
        return gson.toJson(object).getBytes();
    }
}
