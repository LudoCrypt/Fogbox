package net.ludocrypt.fogbox.resource;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

public class CodecUtil {

	public static <R> R get(Decoder<R> decoder, JsonElement jsonElement) throws NullPointerException {
		return Optional.of(decoder.parse(JsonOps.INSTANCE, jsonElement).setLifecycle(Lifecycle.stable())).get().result().get();
	}

	public static <R> R get(Decoder<R> decoder, String jsonFlat) throws NullPointerException {
		return get(decoder, new JsonParser().parse(jsonFlat));
	}

}
