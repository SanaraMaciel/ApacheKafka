package br.com.sanara.ecommerce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class GsonDeserializer<T> implements Deserializer<T> {

    public static final String TYPE_CONFIG = "br.com.sanara.ecommerce.type_config";
    private final Gson gson = new GsonBuilder().create();
    private Class<T> type;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        //pega o valor com stringoff para evitar o problema com valor nulo
        String typeName = String.valueOf(configs.get(TYPE_CONFIG));
        try {
            //transforma em uma classe
            this.type = (Class<T>) Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("tipo de deserializa��o n�o existe no classpath" + typeName);
        }

    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        return gson.fromJson(new String(bytes), type);
    }
}
